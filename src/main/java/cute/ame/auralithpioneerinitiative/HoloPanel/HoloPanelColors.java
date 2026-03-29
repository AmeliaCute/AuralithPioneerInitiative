package cute.ame.auralithpioneerinitiative.HoloPanel;

// TODO: Maybe use a config file instead
public class HoloPanelColors
{
  private HoloPanelColors() {}

  public static final int AMBER_PRIMARY = 0xFF6A00;
  public static final int AMBER_ACTIVE = 0xFFA500;
  public static final int AMBER_DIM = 0x7A3100;
  public static final int AMBER_BORDER = 0x994400;

  public static final int CYAN_SHIELD = 0x00C8FF;

  public static final int WHITE_VALUE = 0xFFFFFF;
  public static final int LIGHT_TEXT = 0xE0E0E0;
  public static final int MUTED_TEXT = 0xAAAAAA;

  public static final int RED_DANGER = 0xFF4444;
  public static final int YELLOW_WARN = 0xFFCC00;
  public static final int GREEN_OK = 0x44FF88;

  public static final int PANEL_BG = 0x0A0A14;
  public static final int PANEL_BG_ALT = 0x111120;

  public static final int SELECT_HOVER = 0x44FF6A00;
  public static final int SELECT_ACTIVE = 0xFFFF6A00;

  public static int opaque(int rgb) { return 0xFF000000 | rgb; }

  public static int hullColor(float pct)
  {
    if (pct < 0.20f) return RED_DANGER;
    if (pct < 0.50f) return YELLOW_WARN;
    return GREEN_OK;
  }
}
