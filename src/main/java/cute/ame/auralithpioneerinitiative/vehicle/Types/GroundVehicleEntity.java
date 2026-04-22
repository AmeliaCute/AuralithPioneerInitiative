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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public class GroundVehicleEntity extends BlockVehicleEntity
{
  private final DynamicPhysics physics = new DynamicPhysics();
  private VehicleCapabilities capabilities = null;

  private float yawVelocity = 0f;
  private boolean onGround  = false;

  private UUID pilotUUID = null;
  private FlightInput lastInput = FlightInput.IDLE;
  private int inputAge  = 0;
  private static final int INPUT_TIMEOUT = 60;

  public GroundVehicleEntity(EntityType<?> type, Level level)
  {
    super(type, level);
    this.noPhysics = true;
  }

  @Override public VehicleType getVehicleType() { return VehicleType.GROUND; }

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

    float suspensionY = computeSuspensionForce(caps);
    onGround = suspensionY > 0;

    if (hasPilot() && onGround)
    {
      float steerAccel = caps.hasGyroscopes() ? caps.totalGyroTorque / Math.max(1f, caps.totalMass) * 0.001f : 0.03f;
      yawVelocity = (yawVelocity + lastInput.thrustX() * steerAccel) * 0.7f;
      Quaternionf dq = new Quaternionf().rotateY(yawVelocity);
      setVehicleRotation(new Quaternionf(getVehicleRotation()).mul(dq).normalize());
    } else {
      yawVelocity *= 0.7f;
    }

    FlightInput groundInput = hasPilot() ? new FlightInput(0, 0, lastInput.thrustZ(), 0, 0, 0, lastInput.boosting(), false) : FlightInput.IDLE;
    physics.integrate(this, caps, groundInput, g);

    Vec3 vel = physics.getLinearVelocity();
    double newVelY = onGround ? Math.max(0, suspensionY - g * 0.001) : vel.y;
    physics.setLinearVelocity(new Vec3(vel.x, newVelY, vel.z));

    Vec3 finalVel = physics.getLinearVelocity();
    double hSq = finalVel.x * finalVel.x + finalVel.z * finalVel.z;
    double cap  = caps.estimatedMaxSpeed * (lastInput.boosting() ? 2.0 : 1.0);
    if (hSq > cap * cap)
    {
      double s = cap / Math.sqrt(hSq);
      physics.setLinearVelocity(new Vec3(finalVel.x * s, finalVel.y, finalVel.z * s));
    }

    Vec3 v = physics.getLinearVelocity();
    setPos(getX() + v.x, getY() + v.y, getZ() + v.z);
    setDeltaMovement(v);

    carryPassengers();
    super.serverTick();
  }

  private float computeSuspensionForce(VehicleCapabilities caps)
  {
    List<VehicleCapabilities.SuspensionMount> points;

    if (caps.hasSuspensions())
      points = caps.suspensions;
    else
    {
      var b = getLocalBounds();
      points = List.of(
          new VehicleCapabilities.SuspensionMount(new Vec3(b.minX, b.minY, b.minZ), 1.0f, 0.08f, 0.05f),
          new VehicleCapabilities.SuspensionMount(new Vec3(b.maxX, b.minY, b.minZ), 1.0f, 0.08f, 0.05f),
          new VehicleCapabilities.SuspensionMount(new Vec3(b.minX, b.minY, b.maxZ), 1.0f, 0.08f, 0.05f),
          new VehicleCapabilities.SuspensionMount(new Vec3(b.maxX, b.minY, b.maxZ), 1.0f, 0.08f, 0.05f)
      );
    }

    float total = 0f;
    int hits = 0;
    Vec3 vel = physics.getLinearVelocity();

    for (VehicleCapabilities.SuspensionMount mount : points)
    {
      Vector3f lp = new Vector3f((float)mount.localPos().x, (float)mount.localPos().y, (float)mount.localPos().z);
      getVehicleRotation().transform(lp);
      Vec3 worldPt = new Vec3(getX() + lp.x, getY() + lp.y, getZ() + lp.z);
      Vec3 rayEnd = worldPt.add(0, -(mount.restHeight() + 0.5), 0);
      BlockHitResult hit = level().clip(new ClipContext(worldPt, rayEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

      if (hit.getType() != HitResult.Type.MISS)
      {
        double compression = mount.restHeight() - (worldPt.y - hit.getLocation().y);
        if (compression > 0)
        {
          float force = (float)(compression * mount.stiffness() - vel.y * mount.damping());
          total += Math.max(0, force);
          hits++;
        }
      }
    }

    return hits > 0 ? total / hits : 0f;
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

  @Override protected boolean isMoving() { return physics.isMoving() || Math.abs(yawVelocity) > 1e-4; }
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

    yawVelocity = tag.getFloat("yawVel");
    physics.load(tag);
    rescanCapabilities();
  }

  @Override
  protected void addAdditionalSaveData(CompoundTag tag)
  {
    super.addAdditionalSaveData(tag);
    if (pilotUUID != null) tag.putUUID("pilotUUID", pilotUUID);

    tag.putFloat("yawVel", yawVelocity);
    physics.save(tag);
  }
}