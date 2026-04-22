package cute.ame.auralithpioneerinitiative.vehicle.Types;

import cute.ame.auralithpioneerinitiative.API.AuralithAPI;
import cute.ame.auralithpioneerinitiative.vehicle.Baking.VehicleSnapshot;
import cute.ame.auralithpioneerinitiative.vehicle.Input.FlightInput;
import cute.ame.auralithpioneerinitiative.vehicle.Physics.CapabilityScanner;
import cute.ame.auralithpioneerinitiative.vehicle.Physics.DynamicPhysics;
import cute.ame.auralithpioneerinitiative.vehicle.Physics.VehicleCapabilities;
import cute.ame.auralithpioneerinitiative.vehicle.Entity.BlockVehicleEntity;
import cute.ame.auralithpioneerinitiative.vehicle.Entity.VehicleSeat;
import cute.ame.auralithpioneerinitiative.vehicle.Entity.VehicleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public class RocketEntity extends BlockVehicleEntity
{
  private final DynamicPhysics physics = new DynamicPhysics();
  private VehicleCapabilities capabilities = null;

  private int currentStageIndex = 0;
  private boolean engineActive = false;

  private float stageFuelRemaining = 0f;

  private UUID pilotUUID = null;
  private FlightInput lastInput = FlightInput.IDLE;
  private int inputAge  = 0;
  private static final int INPUT_TIMEOUT = 60;

  public RocketEntity(EntityType<?> type, Level level)
  {
    super(type, level);
    this.noPhysics = true;
  }

  @Override public VehicleType getVehicleType() { return VehicleType.ROCKET; }

  @Override
  public void setBlockSnapshot(List<BlockEntry> entries)
  {
    super.setBlockSnapshot(entries);
    if (!level().isClientSide()) rescanCapabilities();
  }

  private void rescanCapabilities()
  {
    List<BlockEntry> snap = getBlockSnapshot();
    List<VehicleSnapshot.BlockEntry> se = new ArrayList<>(snap.size());
    for (BlockEntry e : snap) se.add(new VehicleSnapshot.BlockEntry(e.relX(), e.relY(), e.relZ(), e.stateId()));
    capabilities = CapabilityScanner.scan(se);

    if (!capabilities.rocketStages.isEmpty())
      stageFuelRemaining = capabilities.rocketStages.get(0).totalFuelCapacity();
  }

  private VehicleCapabilities getCaps()
  {
    if (capabilities == null) rescanCapabilities();
    return capabilities;
  }

  public Optional<UUID> getPilotUUID() { return Optional.ofNullable(pilotUUID); }
  public void setPilotUUID(UUID id) { this.pilotUUID = id; }
  public boolean hasPilot() { return pilotUUID != null; }
  public void applyFlightInput(FlightInput input) { this.lastInput = input; this.inputAge = 0; }

  public void ignite()
  {
    VehicleCapabilities caps = getCaps();
    if (currentStageIndex >= caps.rocketStages.size()) return;

    engineActive = true;
    stageFuelRemaining = caps.rocketStages.get(currentStageIndex).totalFuelCapacity();
  }

  public void stage()
  {
    currentStageIndex++;
    engineActive = false;
    stageFuelRemaining = 0f;
    VehicleCapabilities caps = getCaps();

    if (currentStageIndex < caps.rocketStages.size())
      stageFuelRemaining = caps.rocketStages.get(currentStageIndex).totalFuelCapacity();
  }

  public int getCurrentStageIndex() { return currentStageIndex; }
  public boolean isEngineActive() { return engineActive; }
  public float getStageFuelRemaining(){ return stageFuelRemaining; }

  @Override
  public void serverTick()
  {
    if (hasPilot())
    {
      inputAge++;
      if (inputAge > INPUT_TIMEOUT) lastInput = FlightInput.IDLE;

      if (lastInput.thrustZ() > 0.5f && !engineActive) ignite();
      if (lastInput.boosting() && engineActive) stage();

      if (lastInput.dismounting())
      {
        Player p = level().getPlayerByUUID(pilotUUID);
        if (p != null) p.stopRiding();
        lastInput = FlightInput.IDLE;
      }
    }

    VehicleCapabilities caps = getCaps();
    float g = AuralithAPI.getGravityFor(level().dimension());

    if (engineActive)
    {
      VehicleCapabilities.RocketStage stage = currentStageIndex < caps.rocketStages.size() ? caps.rocketStages.get(currentStageIndex) : null;

      if (stage != null && stageFuelRemaining > 0)
      {
        float dt = stage.totalFuelRate();
        stageFuelRemaining = Math.max(0, stageFuelRemaining - dt);
        caps.currentFuel = Math.max(0, caps.currentFuel - dt);

        Vector3f localUp = new Vector3f(0, 1, 0);
        getVehicleRotation().transform(localUp);

        float thrustAccel = stage.totalThrust() / Math.max(1f, caps.totalMass);
        physics.setLinearVelocity(physics.getLinearVelocity().add(localUp.x * thrustAccel * 0.0001, localUp.y * thrustAccel * 0.0001, localUp.z * thrustAccel * 0.0001));

        if (stageFuelRemaining <= 0) engineActive = false;
      }
      else engineActive = false;
    }

    if (engineActive) applyGravityTurn(g);

    FlightInput gyroOnly = hasPilot() ? new FlightInput(0, 0, 0, lastInput.rotPitch(), lastInput.rotYaw(), 0, false, false) : FlightInput.IDLE;
    physics.integrate(this, caps, gyroOnly, g);

    carryPassengers();
    super.serverTick();
  }

  private void applyGravityTurn(float gravity)
  {
    if (gravity <= 0) return;
    Vec3 vel = physics.getLinearVelocity();
    if (vel.lengthSqr() < 0.001) return;

    Vec3 velDir = vel.normalize();
    Vector3f up = new Vector3f(0, 1, 0);
    getVehicleRotation().transform(up);
    Vec3 rocketUp = new Vec3(up.x, up.y, up.z);

    double dot = rocketUp.dot(velDir);
    if (dot >= 0.999) return;

    Vec3 correction = rocketUp.lerp(velDir, 0.005);
    Vector3f corrNorm = new Vector3f((float)correction.x, (float)correction.y, (float)correction.z).normalize();
    Quaternionf newRot = new Quaternionf().rotateTo(up, corrNorm).mul(getVehicleRotation()).normalize();
    setVehicleRotation(newRot);
  }

  private void carryPassengers()
  {
    for (var rawSeat : getSeats())
    {
      if (!(rawSeat instanceof VehicleSeat seat)) continue;

      UUID occ = seat.getOccupantUUID();
      if (occ == null) continue;

      Player p = level().getPlayerByUUID(occ);
      if (p == null || p.getVehicle() != this) continue;

      var pos = seat.getWorldPosition(this);
      p.setPos(pos.x, pos.y, pos.z);
    }
  }

  @Override protected boolean isMoving() { return physics.isMoving() || engineActive; }
  @Override public boolean isPickable() { return true; }
  @Override public boolean canBeCollidedWith() { return true; }

  @Override
  public void positionRider(Entity passenger, Entity.MoveFunction fn)
  {
    if (!hasPassenger(passenger)) return;
    Vec3 pos = null;
    if (passenger instanceof Player pl)
    {
      var raw = getSeatOf(pl);
      if (raw instanceof VehicleSeat seat) pos = seat.getWorldPosition(this);
    }

    if (pos == null) pos = position().add(0, 1.0, 0);
    fn.accept(passenger, pos.x, pos.y, pos.z);
  }

  @Override
  public void removePassenger(Entity passenger)
  {
    super.removePassenger(passenger);
    if (passenger instanceof Player p && p.getUUID().equals(pilotUUID))
    {
      pilotUUID = null;
      lastInput = FlightInput.IDLE;
      inputAge = 0;
    }
  }

  @Override
  protected void readAdditionalSaveData(CompoundTag tag)
  {
    super.readAdditionalSaveData(tag);
    if (tag.hasUUID("pilotUUID")) pilotUUID = tag.getUUID("pilotUUID");

    currentStageIndex = tag.getInt("stage");
    engineActive = tag.getBoolean("engineActive");
    stageFuelRemaining = tag.getFloat("stageFuel");
    physics.load(tag);
    rescanCapabilities();
    float saved = tag.contains("currentFuel") ? tag.getFloat("currentFuel") : -1f;
    if (saved >= 0 && capabilities != null) capabilities.currentFuel = saved;
  }

  @Override
  protected void addAdditionalSaveData(CompoundTag tag)
  {
    super.addAdditionalSaveData(tag);
    if (pilotUUID != null) tag.putUUID("pilotUUID", pilotUUID);

    tag.putInt("stage", currentStageIndex);
    tag.putBoolean("engineActive", engineActive);
    tag.putFloat("stageFuel", stageFuelRemaining);
    physics.save(tag);
    if (capabilities != null) tag.putFloat("currentFuel", capabilities.currentFuel);
  }
}