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
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class SubmarineEntity extends BlockVehicleEntity
{
  private final DynamicPhysics physics = new DynamicPhysics();
  private VehicleCapabilities capabilities = null;

  private float buoyancyTarget = 0.5f;
  private static final float BUOYANCY_FORCE = 0.006f;
  private static final float WATER_DRAG_LINEAR = 0.08f;
  private static final float WATER_DRAG_ANGULAR= 0.20f;
  private static final float AIR_DRAG = 0.02f;

  private UUID pilotUUID = null;
  private FlightInput lastInput = FlightInput.IDLE;
  private int inputAge = 0;
  private static final int INPUT_TIMEOUT = 60;

  public SubmarineEntity(EntityType<?> type, Level level)
  {
    super(type, level);
    this.noPhysics = true;
  }

  @Override public VehicleType getVehicleType() { return VehicleType.SUBMARINE; }

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

  @Override
  public void serverTick()
  {
    if (hasPilot())
    {
      inputAge++;
      if (inputAge > INPUT_TIMEOUT) lastInput = FlightInput.IDLE;
      if (lastInput.dismounting())
      {
        Player p = level().getPlayerByUUID(pilotUUID);
        if (p != null) p.stopRiding();

        lastInput = FlightInput.IDLE;
      }
    }

    VehicleCapabilities caps = getCaps();
    float g = AuralithAPI.getGravityFor(level().dimension());
    float submerged = computeSubmergedFraction();
    boolean inWater = submerged > 0.05f;

    if (hasPilot()) buoyancyTarget = clamp01(buoyancyTarget + lastInput.thrustY() * 0.01f);

    FlightInput subInput = hasPilot()
    ? new FlightInput(lastInput.thrustX(), 0, lastInput.thrustZ(),
    lastInput.rotPitch(), lastInput.rotYaw(), lastInput.rotRoll(),
    lastInput.boosting(), false)
    : FlightInput.IDLE;

    physics.integrate(this, caps, subInput, inWater ? 0f : g);
    Vec3 vel = physics.getLinearVelocity();
    double newVelY;
    if (inWater)
    {
      float netBuoyancy = (buoyancyTarget - 0.5f) * 2f;
      float buoyancyY = netBuoyancy * BUOYANCY_FORCE * submerged;
      newVelY = (vel.y + buoyancyY - g * 0.001) * (1.0 - WATER_DRAG_LINEAR);
    }
    else
    {
      newVelY = (vel.y - g * 0.003) * (1.0 - AIR_DRAG);
      if (newVelY < -1.0) newVelY = -1.0;
    }

    double hDrag = inWater ? WATER_DRAG_LINEAR : AIR_DRAG;
    double speedCap = caps.estimatedMaxSpeed * (lastInput.boosting() ? 2.0 : 1.0);
    Vec3 newVel = new Vec3(vel.x, newVelY, vel.z).scale(1.0 - hDrag);
    if (newVel.horizontalDistanceSqr() > speedCap * speedCap)
    {
      double hLen = newVel.horizontalDistance();
      newVel = new Vec3(newVel.x / hLen * speedCap, newVel.y, newVel.z / hLen * speedCap);
    }

    physics.setLinearVelocity(newVel);
    setPos(getX() + newVel.x, getY() + newVel.y, getZ() + newVel.z);
    setDeltaMovement(newVel);

    carryPassengers();
    super.serverTick();
  }

  private float computeSubmergedFraction()
  {
    var bounds = getLocalBounds();
    int total = 0, sub = 0;
    float stepY = (float)((bounds.maxY - bounds.minY) / 3.0);
    for (int i = 0; i <= 3; i++)
    {
      double cy = getY() + bounds.minY + stepY * i;
      FluidState fluid = level().getFluidState(BlockPos.containing(getX(), cy, getZ()));
      total++;

      if (fluid.is(FluidTags.WATER)) sub++;
    }
    return total == 0 ? 0f : (float) sub / total;
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

  @Override protected boolean isMoving() { return physics.isMoving(); }
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

    buoyancyTarget = tag.contains("buoyancy") ? tag.getFloat("buoyancy") : 0.5f;
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

    tag.putFloat("buoyancy", buoyancyTarget);
    physics.save(tag);

    if (capabilities != null) tag.putFloat("currentFuel", capabilities.currentFuel);
  }

  private static float clamp01(float v) { return Math.max(0f, Math.min(1f, v)); }
}
