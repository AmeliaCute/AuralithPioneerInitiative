package cute.ame.auralithpioneerinitiative.Ship.Input;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class FlightCameraState
{
  private FlightCameraState() {}

  private static boolean freeLook = false;
  private static float rollRad = 0.0f;

  public static boolean isFreeLook() { return freeLook; }
  public static void toggleFreeLook() { freeLook = !freeLook; }
  public static void setFreeLook(boolean v) { freeLook = v; }

  public static float getRoll() { return rollRad; }
  public static void  setRoll(float r) { rollRad = r; }

  private static double pendingDX = 0.0;
  private static double pendingDY = 0.0;

  public static void addMouseDelta(double dx, double dy)
  {
    pendingDX += dx;
    pendingDY += dy;
  }

  public static double consumeDX() { double v = pendingDX; pendingDX = 0.0; return v; }
  public static double consumeDY() { double v = pendingDY; pendingDY = 0.0; return v; }

  public static void reset()
  {
    freeLook = false;
    pendingDX = 0.0;
    pendingDY = 0.0;
    rollRad = 0.0f;
  }
}