package cute.ame.auralithpioneerinitiative.vehicle.Seat;

import net.minecraft.world.phys.Vec3;

public record SeatDefinition(
    Vec3 localOffset,
    boolean isPilot,
    CameraConfig camera
)
{
  public static SeatDefinition pilotDefault()
  {
    return new SeatDefinition(new Vec3(0, 1.0, 0), true, CameraConfig.COCKPIT);
  }

  public static SeatDefinition passengerDefault(Vec3 offset)
  {
    return new SeatDefinition(offset, false, CameraConfig.DEFAULT);
  }
}