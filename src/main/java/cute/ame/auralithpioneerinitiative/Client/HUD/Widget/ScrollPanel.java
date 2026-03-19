package cute.ame.auralithpioneerinitiative.Client.HUD.Widget;

import com.mojang.blaze3d.systems.RenderSystem;
import cute.ame.auralithpioneerinitiative.Client.HUD.HoloPanelColors;
import net.minecraft.client.gui.GuiGraphics;

public class ScrollPanel extends HoloPanelWidget
{
  private static final int SCROLLBAR_W = 2;
  private static final int SCROLLBAR_MIN_H = 8;

  private final HoloPanelWidget content;
  private int scrollOffset = 0;

  public static int fboHeight = 200;

  public ScrollPanel(HoloPanelWidget content)
  {
    this.content = content;
  }

  @Override
  public void setBounds(int x, int y, int w, int h) {
    super.setBounds(x, y, w, h);
    content.setBounds(x, y - scrollOffset, w - SCROLLBAR_W, content.preferredHeight());
  }

  private int contentH() { return content.preferredHeight(); }

  private void clampScroll() {
    int max = Math.max(0, contentH() - h);
    scrollOffset = Math.max(0, Math.min(scrollOffset, max));
  }

  @Override
  public void render(GuiGraphics g, HoloPanelContext ctx, float pt)
  {
    int scissorX = x;
    int scissorY = fboHeight - y - h;
    int scissorW = w - SCROLLBAR_W;
    int scissorH = h;

    RenderSystem.enableScissor(scissorX, scissorY, scissorW, scissorH);
    try
    {
      g.pose().pushPose();
      g.pose().translate(0, -scrollOffset, 0);
      content.setBounds(x, y, w - SCROLLBAR_W, content.preferredHeight());
      content.render(g, ctx, pt);
      g.pose().popPose();
    }
    finally
    {
      RenderSystem.disableScissor();
    }

    renderScrollbar(g);
  }

  private void renderScrollbar(GuiGraphics g)
  {
    if (contentH() <= h) return;
    int trackH = h;
    int thumbH = Math.max(SCROLLBAR_MIN_H, trackH * h / contentH());
    int thumbY = y + (int)((float) scrollOffset / (contentH() - h) * (trackH - thumbH));
    int bx = x + w - SCROLLBAR_W;
    g.fill(bx, thumbY, bx + SCROLLBAR_W, thumbY + thumbH, HoloPanelColors.AMBER_PRIMARY | 0xFF000000);
  }

  @Override
  public boolean onMouseScroll(int lx, int ly, double delta)
  {
    scrollOffset -= (int)(delta * 8);
    clampScroll();
    content.setBounds(x, y, w - SCROLLBAR_W, content.preferredHeight());
    return true;
  }

  @Override
  public boolean onMouseClick(int lx, int ly, int button)
  {
    int contentLX = lx;
    int contentLY = ly + scrollOffset;
    return content.onMouseClick(contentLX - (content.x - x), contentLY - (content.y - y), button);
  }

  public void scrollTo(int offset)
  {
    scrollOffset = offset;
    clampScroll();
  }

  public void scrollBy(int delta)
  {
    scrollOffset += delta;
    clampScroll();
  }
}
