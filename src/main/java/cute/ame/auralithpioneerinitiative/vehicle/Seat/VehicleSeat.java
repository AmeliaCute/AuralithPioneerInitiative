package cute.ame.auralithpioneerinitiative.vehicle.Seat;

import cute.ame.auralithpioneerinitiative.vehicle.Entity.AbstractVehicleEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.UUID;

public class VehicleSeat
{
  private final SeatDefinition definition;
  private @Nullable UUID occupantUUID;

  public VehicleSeat(SeatDefinition definition) {
    this.definition = definition;
  }

  public SeatDefinition getDefinition() { return definition; }
  public boolean isPilot() { return definition.isPilot(); }

  public @Nullable UUID getOccupantUUID() { return occupantUUID; }
  public void setOccupantUUID(@Nullable UUID uuid) { this.occupantUUID = uuid; }
  public boolean isOccupied() { return occupantUUID != null; }

  public Vec3 getWorldPosition(AbstractVehicleEntity vehicle)
  {
    Vector3f local = toVector3f(definition.localOffset());
    vehicle.getVehicleRotation().transform(local);
    return vehicle.position().add(local.x, local.y, local.z);
  }

  public Vec3 getInterpolatedWorldPosition(AbstractVehicleEntity vehicle, float partialTick)
  {
    if (vehicle.renderPrevPos == null || vehicle.renderTargetPos == null || vehicle.renderPrevRot == null || vehicle.renderTargetRot == null)
      return getWorldPosition(vehicle);

    Vec3 interpPos = vehicle.renderPrevPos.lerp(vehicle.renderTargetPos, partialTick);
    Quaternionf interpRot = new Quaternionf(vehicle.renderPrevRot).slerp(vehicle.renderTargetRot, partialTick);

    Vector3f local = toVector3f(definition.localOffset());
    interpRot.transform(local);

    return interpPos.add(local.x, local.y, local.z);
  }

  public Vec3 getInterpolatedEyePosition(AbstractVehicleEntity vehicle, float partialTick)
  {
    Vec3 seatPos = getInterpolatedWorldPosition(vehicle, partialTick);
    Vec3 eyeOff = definition.camera().eyeOffset();
    return seatPos.add(eyeOff.x, eyeOff.y, eyeOff.z);
  }

  private static Vector3f toVector3f(Vec3 v) { return new Vector3f((float) v.x, (float) v.y, (float) v.z); }
}