package cute.ame.auralithpioneerinitiative.vehicle.Seat;

import net.minecraft.world.phys.Vec3;

public record CameraConfig(
    Vec3 eyeOffset,
    float defaultFov,
    boolean forceThirdPerson,
    float maxPitch,
    float minPitch
)
{
  public static final CameraConfig DEFAULT = new CameraConfig(new Vec3(0, 1.5, 0), 0f, false, 89f, -89f);

  public static final CameraConfig COCKPIT = new CameraConfig(new Vec3(0, 1.5, 0), 85f, false, 75f, -60f);
}