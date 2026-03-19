package cute.ame.auralithpioneerinitiative.Client.HUD.Widget;

import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class ColumnLayout extends HoloPanelWidget
{
  private final List<HoloPanelWidget> children = new ArrayList<>();
  private final int gap;

  public ColumnLayout() { this(2); } // SHOULD NEVER BE USE
  public ColumnLayout(int gap) { this.gap = gap; }

  public ColumnLayout add(HoloPanelWidget widget)
  {
    children.add(widget);
    return this;
  }

  @Override
  public void setBounds(int x, int y, int w, int h)
  {
    super.setBounds(x,y,w,h);
    layout();
  }

  private void layout()
  {
    int cy = y;
    for(HoloPanelWidget child : children)
    {
      int ch = child.preferredHeight();
      child.setBounds(x,cy,w,ch);
      cy += ch + gap;
    }
  }

  @Override
  public int preferredHeight() {
    if (children.isEmpty()) return 0;
    int total = 0;
    for (HoloPanelWidget c : children) total += c.preferredHeight();
    total += gap * (children.size() - 1);
    return total;
  }

  @Override
  public void render(GuiGraphics g, HoloPanelContext ctx, float pt)
  {
    for (HoloPanelWidget c : children) c.render(g, ctx, pt);
  }

  @Override
  public boolean onMouseClick(int lx, int ly, int button)
  {
    for (HoloPanelWidget c : children)
      if (c.contains(lx + x, ly + y) && c.onMouseClick(lx + x - c.x, ly + y - c.y, button))
        return true;

    return false;
  }

  @Override
  public boolean onMouseScroll(int lx, int ly, double delta)
  {
    for (HoloPanelWidget c : children)
      if (c.contains(lx + x, ly + y) && c.onMouseScroll(lx + x - c.x, ly + y - c.y, delta))
        return true;
    return false;
  }

  public List<HoloPanelWidget> getChildren() { return children; }
}
