package cute.ame.auralithpioneerinitiative.vehicle.Input;

public record FlightInput(
  float thrustX,
  float thrustY,
  float thrustZ,
  float rotPitch,
  float rotYaw,
  float rotRoll,
  boolean boosting,
  boolean dismounting
)
{
  public static final FlightInput IDLE = new FlightInput(0, 0, 0, 0, 0, 0, false, false);
}
