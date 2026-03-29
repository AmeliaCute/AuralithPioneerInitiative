package cute.ame.auralithpioneerinitiative.HoloPanel.Widget;

import com.mojang.blaze3d.systems.RenderSystem;
import cute.ame.auralithpioneerinitiative.HoloPanel.HoloPanelColors;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ListWidget<T> extends HoloPanelWidget
{
  public interface ItemRenderer<T>
  {
    void render(GuiGraphics g, T item, int x, int y, int w, int h, boolean selected, boolean hovered, HoloPanelContext ctx, float pt);
  }

  private static final int ITEM_H = 14;
  private static final int SCROLLBAR_W = 2;

  private final List<T> items;
  private final ItemRenderer<T> renderer;
  private final Consumer<T> onSelect;

  private int scrollOffset = 0;
  private int selectedIndex = -1;
  private int hoveredIndex = -1;

  public ListWidget(List<T> items, ItemRenderer<T> renderer, Consumer<T> onSelect)
  {
    this.items = new ArrayList<>(items);
    this.renderer = renderer;
    this.onSelect = onSelect;
  }

  public void setItems(List<T> newItems)
  {
    items.clear();
    items.addAll(newItems);
    clampScroll();
  }

  private int contentHeight() { return items.size() * ITEM_H; }

  private void clampScroll()
  {
    int max = Math.max(0, contentHeight() - h);
    scrollOffset = Math.max(0, Math.min(scrollOffset, max));
  }

  @Override
  public void render(GuiGraphics g, HoloPanelContext ctx, float pt)
  {
    int listW = w - SCROLLBAR_W;

    int scissorY = ScrollPanel.fboHeight - y - h;
    RenderSystem.enableScissor(x, scissorY, listW, h);
    try
    {
      int firstIdx = scrollOffset / ITEM_H;
      int lastIdx = Math.min(items.size() - 1, firstIdx + h / ITEM_H + 1);

      for (int i = firstIdx; i <= lastIdx; i++)
      {
        int iy = y + i * ITEM_H - scrollOffset;
        T item = items.get(i);
        boolean sel = (i == selectedIndex);
        boolean hov = (i == hoveredIndex);
        renderer.render(g, item, x, iy, listW, ITEM_H, sel, hov, ctx, pt);
      }
    }
    finally
    {
      RenderSystem.disableScissor();
    }

    if (contentHeight() > h)
    {
      int trackH = h;
      int thumbH = Math.max(8, trackH * h / contentHeight());
      int thumbY = y + (int)((float) scrollOffset / Math.max(1, contentHeight() - h) * (trackH - thumbH));
      int bx = x + listW;
      g.fill(bx, thumbY, bx + SCROLLBAR_W, thumbY + thumbH, HoloPanelColors.opaque(HoloPanelColors.AMBER_PRIMARY));
    }
  }

  @Override
  public boolean onMouseClick(int lx, int ly, int button)
  {
    int iy = ly - y + scrollOffset;
    int idx = iy / ITEM_H;
    if (idx >= 0 && idx < items.size())
    {
      selectedIndex = idx;

      if (onSelect != null) onSelect.accept(items.get(idx));
      return true;
    }
    return false;
  }

  @Override
  public boolean onMouseScroll(int lx, int ly, double delta)
  {
    scrollOffset -= (int)(delta * ITEM_H);
    clampScroll();
    return true;
  }

  public void updateHovered(int lx, int ly)
  {
    int iy  = ly - y + scrollOffset;
    int idx = iy / ITEM_H;
    hoveredIndex = (idx >= 0 && idx < items.size()) ? idx : -1;
  }

  public int getSelectedIndex() { return selectedIndex; }
  public T getSelectedItem() { return (selectedIndex >= 0 && selectedIndex < items.size()) ? items.get(selectedIndex) : null; }
  public List<T> getItems() { return items; }
}
