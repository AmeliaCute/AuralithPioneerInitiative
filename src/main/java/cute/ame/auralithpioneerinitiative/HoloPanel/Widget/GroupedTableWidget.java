package cute.ame.auralithpioneerinitiative.HoloPanel.Widget;

import com.mojang.blaze3d.systems.RenderSystem;
import cute.ame.auralithpioneerinitiative.HoloPanel.HoloPanelColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GroupedTableWidget extends HoloPanelWidget
{
  private static final int HEADER_H = 12;
  private static final int GROUP_H = 10;
  private static final int ROW_H = 12;
  private static final int SCROLLBAR_W = 2;

  public record Column(String name, int widthPct){}

  public sealed interface TableRow permits GroupedTableWidget.GroupRow, GroupedTableWidget.DataRow {}
  public record GroupRow(String label) implements TableRow {}
  public record DataRow(List<String> cells, Object tag) implements TableRow {}

  private final List<Column> columns;
  private final List<TableRow> rows = new ArrayList<>();
  private Consumer<DataRow> onRowSelect;
  private int selectedDataIdx = -1;
  private int scrollOffset = 0;

  public GroupedTableWidget(List<Column> columns) { this.columns = columns; }

  public GroupedTableWidget setOnRowSelect(Consumer<DataRow> sel) { this.onRowSelect = sel; return this; }
  public GroupedTableWidget addGroup(String label) { rows.add(new GroupRow(label)); return this; }
  public GroupedTableWidget addRow(List<String> cells, Object tag) { rows.add(new DataRow(cells, tag)); return this; }
  public void clearRows() { rows.clear(); selectedDataIdx = -1; scrollOffset = 0; }

  private int contentHeight()
  {
    int total = 0;

    for (TableRow r : rows) total += (r instanceof GroupRow) ? GROUP_H : ROW_H;
    return total;
  }

  private void clampScroll()
  {
    int max = Math.max(0, contentHeight() - (h - HEADER_H));
    scrollOffset = Math.max(0, Math.min(scrollOffset, max));
  }

  @Override
  public void render(GuiGraphics g, HoloPanelContext ctx, float pt)
  {
    var font = Minecraft.getInstance().font;

    g.fill(x, y, x + w, y + HEADER_H, HoloPanelColors.opaque(HoloPanelColors.PANEL_BG));
    g.fill(x, y + HEADER_H - 1, x + w, y + HEADER_H, HoloPanelColors.opaque(HoloPanelColors.AMBER_BORDER));
    int cx = x;
    for (Column col : columns)
    {
      int cw = col.widthPct() * (w - SCROLLBAR_W) / 100;
      g.pose().pushPose();
      g.pose().translate(cx + 2, y + 2, 0);
      g.pose().scale(0.8f, 0.8f, 1.0f);
      g.drawString(font, col.name().toUpperCase(), 0, 0, HoloPanelColors.opaque(HoloPanelColors.MUTED_TEXT), false);
      g.pose().popPose();
      cx += cw;
    }

    int bodyY = y + HEADER_H;
    int bodyH = h - HEADER_H;
    int scissorY = ScrollPanel.fboHeight - bodyY - bodyH;
    RenderSystem.enableScissor(x, scissorY, w - SCROLLBAR_W, bodyH);

    try
    {
      int ry = bodyY - scrollOffset;
      int dataIdx = 0;
      for (TableRow row : rows)
      {
        if (row instanceof GroupRow gr)
        {
          g.fill(x, ry, x + w - SCROLLBAR_W, ry + GROUP_H, HoloPanelColors.opaque(0x1A1A00));
          g.drawString(font, gr.label(), x + 2, ry + 1, HoloPanelColors.opaque(HoloPanelColors.AMBER_DIM), false);
          ry += GROUP_H;
        }
        else if (row instanceof DataRow dr)
        {
          boolean sel = (dataIdx == selectedDataIdx);
          int bg = sel ? HoloPanelColors.SELECT_ACTIVE : (dataIdx % 2 == 0 ? HoloPanelColors.opaque(HoloPanelColors.PANEL_BG_ALT) : HoloPanelColors.opaque(HoloPanelColors.PANEL_BG));
          g.fill(x, ry, x + w - SCROLLBAR_W, ry + ROW_H, bg);
          if (sel) g.fill(x, ry, x + 2, ry + ROW_H, HoloPanelColors.opaque(HoloPanelColors.AMBER_PRIMARY));

          cx = x;
          for (int ci = 0; ci < Math.min(columns.size(), dr.cells().size()); ci++)
          {
            int cw = columns.get(ci).widthPct() * (w - SCROLLBAR_W) / 100;
            int textCol = sel ? HoloPanelColors.opaque(HoloPanelColors.WHITE_VALUE) : HoloPanelColors.opaque(HoloPanelColors.LIGHT_TEXT);
            g.drawString(font, dr.cells().get(ci), cx + 3, ry + 2, textCol, false);
            cx += cw;
          }
          ry += ROW_H;
          dataIdx++;
        }
      }
    }
    finally
    {
      RenderSystem.disableScissor();
    }

    if (contentHeight() > bodyH) {
      int trackH = bodyH;
      int thumbH = Math.max(8, trackH * bodyH / contentHeight());
      int thumbY = bodyY + (int)((float) scrollOffset / Math.max(1, contentHeight() - bodyH) * (trackH - thumbH));
      int bx = x + w - SCROLLBAR_W;
      g.fill(bx, thumbY, bx + SCROLLBAR_W, thumbY + thumbH, HoloPanelColors.opaque(HoloPanelColors.AMBER_PRIMARY));
    }
  }

  @Override
  public boolean onMouseClick(int lx, int ly, int button)
  {
    if (ly < y + HEADER_H) return false;
    int ry = y + HEADER_H - scrollOffset;
    int dataIdx = 0;

    for (TableRow row : rows)
    {
      int rowH = (row instanceof GroupRow) ? GROUP_H : ROW_H;
      if (ly >= ry && ly < ry + rowH && row instanceof DataRow dr)
      {
        selectedDataIdx = dataIdx;
        if (onRowSelect != null) onRowSelect.accept(dr);
        return true;
      }

      if (row instanceof DataRow) dataIdx++;
      ry += rowH;
    }
    return false;
  }

  @Override
  public boolean onMouseScroll(int lx, int ly, double delta)
  {
    scrollOffset -= (int)(delta * ROW_H);
    clampScroll();
    return true;
  }
}
