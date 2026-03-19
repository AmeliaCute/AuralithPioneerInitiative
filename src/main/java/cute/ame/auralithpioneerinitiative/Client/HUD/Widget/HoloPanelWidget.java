package cute.ame.auralithpioneerinitiative.Client.HUD.Widget;

import net.minecraft.client.gui.GuiGraphics;

public abstract class HoloPanelWidget
{
  protected int x;
  protected int y;
  protected int w;
  public int h;
  protected boolean hovered = false;
  protected boolean enabled = true;

  public abstract void render(GuiGraphics g, HoloPanelContext ctx, float pt);

  public boolean onMouseClick(int lx, int ly, int button) { return false; }
  public boolean onMouseScroll(int lx, int ly, double delta) { return false; }

  public void setBounds(int x, int y, int w, int h)
  {
    this.x = x; this.y = y; this.w = w; this.h = h;
  }
  public int preferredHeight() { return h; }
  public int preferredWidth() { return w; }

  public void setHovered(boolean v) { this.hovered = v; }
  public void setEnabled(boolean v) { this.enabled = v; }
  public boolean isEnabled() { return enabled; }

  public boolean contains(int px, int py)
  {
    return px >= x && px < x + w && py >= y && py < y + h;
  }
}
