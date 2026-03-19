package cute.ame.auralithpioneerinitiative.Client.HUD.Widget;

import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class RowLayout extends HoloPanelWidget
{
  private record Entry(HoloPanelWidget widget, int width){}

  private final List<Entry> entries = new ArrayList<>();
  private final int gap;

  public RowLayout() { this(2); }
  public RowLayout(int gap) {this.gap = gap; }

  public RowLayout add(HoloPanelWidget widget, int width)
  {
    entries.add(new Entry(widget, width));
    return this;
  }

  @Override
  public void setBounds(int x, int y, int w, int h)
  {
    super.setBounds(x, y, w, h);
    layout();
  }

  private void layout()
  {
    int cx = x;
    for (Entry e : entries)
    {
      e.widget().setBounds(cx,y,e.width(),h);
      cx += e.width() + gap;
    }
  }

  @Override
  public void render(GuiGraphics g, HoloPanelContext ctx, float pt)
  {
    for (Entry e : entries) e.widget().render(g, ctx, pt);
  }

  @Override
  public boolean onMouseClick(int lx, int ly, int button)
  {
    for (Entry e : entries)
    {
      HoloPanelWidget c = e.widget();
      if (c.contains(lx + x, ly + y) && c.onMouseClick(lx + x - c.x, ly + y - c.y, button)) return true;
    }
    return false;
  }

  @Override
  public boolean onMouseScroll(int lx, int ly, double delta)
  {
    for (Entry e : entries)
    {
      HoloPanelWidget c = e.widget();
      if (c.contains(lx + x, ly + y) && c.onMouseScroll(lx + x - c.x, ly + y - c.y, delta)) return true;
    }
    return false;
  }
}
