package cute.ame.auralithpioneerinitiative.Client.HUD.Widget;

import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Fixed-column grid. Cells are equal-sized and wrap automatically.
 */
public class GridWidget extends HoloPanelWidget
{

    private final int columns;
    private final int cellH;
    private final int gap;
    private final List<HoloPanelWidget> cells = new ArrayList<>();

    public GridWidget(int columns, int cellH) { this(columns, cellH, 2); }
    public GridWidget(int columns, int cellH, int gap) { this.columns = columns; this.cellH = cellH; this.gap = gap;}

    public GridWidget add(HoloPanelWidget cell) { cells.add(cell); return this; }

    @Override
    public void setBounds(int x, int y, int w, int h)
    {
        super.setBounds(x, y, w, h);
        int cellW = (w - gap * (columns - 1)) / columns;
        for (int i = 0; i < cells.size(); i++)
        {
            int col = i % columns;
            int row = i / columns;
            int cx = x + col * (cellW + gap);
            int cy = y + row * (cellH + gap);
            cells.get(i).setBounds(cx, cy, cellW, cellH);
        }
    }

    @Override
    public int preferredHeight()
    {
        int rows = (cells.size() + columns - 1) / columns;
        return rows * cellH + (rows - 1) * gap;
    }

    @Override
    public void render(GuiGraphics g, HoloPanelContext ctx, float pt)
    {
        for (HoloPanelWidget cell : cells) cell.render(g, ctx, pt);
    }

    @Override
    public boolean onMouseClick(int lx, int ly, int button)
    {
        for (HoloPanelWidget c : cells) if (c.contains(lx, ly) && c.onMouseClick(lx - c.x, ly - c.y, button)) return true;

        return false;
    }

    @Override
    public boolean onMouseScroll(int lx, int ly, double delta)
    {
        for (HoloPanelWidget c : cells) if (c.contains(lx, ly) && c.onMouseScroll(lx - c.x, ly - c.y, delta)) return true;

        return false;
    }
}
