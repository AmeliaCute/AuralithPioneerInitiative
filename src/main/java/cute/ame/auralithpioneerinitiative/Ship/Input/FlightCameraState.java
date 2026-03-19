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
  public static void setFreeLook(boolean v) { freeLook = v; }

  private static double pendingDX = 0.0;
  private static double pendingDY = 0.0;

  public static void   addMouseDelta(double dx, double dy) { pendingDX += dx; pendingDY += dy; }
  public static double consumeDX() { double v = pendingDX; pendingDX = 0.0; return v; }
  public static double consumeDY() { double v = pendingDY; pendingDY = 0.0; return v; }

  private static boolean panelFocused = false;
  private static float cursorU = 0.5f;
  private static float cursorV = 0.5f;

  public static boolean isPanelFocused() { return panelFocused; }
  public static void setPanelFocused(boolean v) { panelFocused = v; }

  public static float getCursorU() { return cursorU; }
  public static float getCursorV() { return cursorV; }

  public static void addCursorDelta(float du, float dv)
  {
    cursorU = Math.max(0f, Math.min(1f, cursorU + du));
    cursorV = Math.max(0f, Math.min(1f, cursorV + dv));
  }

  public static void resetCursor() { cursorU = 0.5f; cursorV = 0.5f; }

  private static double scrollDeltaY = 0.0;

  public static void   addScrollDelta(double dy)  { scrollDeltaY += dy; }
  public static double consumeScrollDelta() { double v = scrollDeltaY; scrollDeltaY = 0.0; return v; }

  public static void reset()
  {
    freeLook = false;
    pendingDX = 0.0;
    pendingDY = 0.0;
    panelFocused = false;
    cursorU = 0.5f;
    cursorV = 0.5f;
    scrollDeltaY = 0.0;
  }
}