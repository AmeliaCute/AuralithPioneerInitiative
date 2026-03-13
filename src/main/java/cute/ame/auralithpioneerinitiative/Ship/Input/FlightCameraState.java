package cute.ame.auralithpioneerinitiative.Ship.Input;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class FlightCameraState
{
  private FlightCameraState() {}
  private static boolean freeLook = false;

  public static boolean isFreeLook() { return freeLook; }
  public static void toggleFreeLook() { freeLook = !freeLook; }
  public static void setFreeLook(boolean v){ freeLook = v; }

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
    freeLook  = false;
    pendingDX = 0.0;
    pendingDY = 0.0;
  }
}