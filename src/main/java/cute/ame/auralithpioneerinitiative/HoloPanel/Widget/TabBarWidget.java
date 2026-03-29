package cute.ame.auralithpioneerinitiative.HoloPanel.Widget;

import cute.ame.auralithpioneerinitiative.HoloPanel.HoloPanelColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class TabBarWidget extends HoloPanelWidget
{
  private record Tab(String label, HoloPanelWidget content) {}

  private  final List<Tab> tabs = new ArrayList<>();
  private int activeTab = 0;;
  private final int tabBarH;
  private final float textScale;

  public TabBarWidget() { this(16,1.0f); }
  private TabBarWidget(int h, float s) { this.tabBarH = h; this.textScale = s; }

  public TabBarWidget add(String label, HoloPanelWidget content)
  {
    tabs.add(new Tab(label, content));
    return this;
  }

  @Override
  public void setBounds(int x, int y, int w, int h)
  {
    super.setBounds(x, y, w, h);
    layoutContent();
  }

  private void layoutContent()
  {
    int contentY = y + tabBarH;
    int contentH = Math.max(0, h - tabBarH);
    for (Tab tab : tabs) tab.content().setBounds(x, contentY, w, contentH);
  }

  @Override
  public int preferredHeight()
  {
    int maxContent = 0;
    for (Tab t : tabs) maxContent = Math.max(maxContent, t.content().preferredHeight());
    return tabBarH + maxContent;
  }

  @Override
  public void render(GuiGraphics g, HoloPanelContext ctx, float pt)
  {
    if (tabs.isEmpty()) return;

    renderTabBar(g);
    if (activeTab < tabs.size()) tabs.get(activeTab).content().render(g, ctx, pt);
  }

  private void renderTabBar(GuiGraphics g)
  {
    int n = tabs.size();
    int tabW = w / n;
    var font = Minecraft.getInstance().font;

    for (int i = 0; i < n; i++)
    {
      boolean active = (i == activeTab);
      int tx = x + i * tabW;
      int ty = y;

      int bg = active ? HoloPanelColors.AMBER_DIM : HoloPanelColors.PANEL_BG;
      g.fill(tx, ty, tx + tabW, ty + tabBarH, bg | 0xFF000000);
      if (active) g.fill(tx, ty, tx + tabW, ty + 2, HoloPanelColors.AMBER_PRIMARY | 0xFF000000);

      String lbl = tabs.get(i).label();
      int col = active ? (HoloPanelColors.AMBER_PRIMARY | 0xFF000000) : (HoloPanelColors.MUTED_TEXT | 0xFF000000);
      float scale = textScale;
      float tw = font.width(lbl) * scale;
      float lx = tx + (tabW - tw) * 0.5f;
      float ly = ty + (tabBarH - font.lineHeight * scale) * 0.5f;

      g.pose().pushPose();
      g.pose().translate(lx, ly, 0);
      g.pose().scale(scale, scale, 1.0f);
      g.drawString(font, lbl, 0, 0, col, false);
      g.pose().popPose();
      if (i < n - 1) g.fill(tx + tabW - 1, ty, tx + tabW, ty + tabBarH, HoloPanelColors.AMBER_BORDER | 0xFF000000);
    }

    g.fill(x, y + tabBarH - 1, x + w, y + tabBarH, HoloPanelColors.AMBER_BORDER | 0xFF000000);
  }

  @Override
  public boolean onMouseClick(int lx, int ly, int button)
  {
    if (ly >= y && ly < y + tabBarH)
    {
      int n = tabs.size();
      int tabW = w / n;
      int idx = (lx - x) / tabW;
      if (idx >= 0 && idx < n) { activeTab = idx; return true; }
    }
    if (activeTab < tabs.size())
    {
      HoloPanelWidget c = tabs.get(activeTab).content();
      if (c.contains(lx, ly)) return c.onMouseClick(lx - c.x, ly - c.y, button);
    }
    return false;
  }

  @Override
  public boolean onMouseScroll(int lx, int ly, double delta)
  {
    if (activeTab < tabs.size())
    {
      HoloPanelWidget c = tabs.get(activeTab).content();
      if (c.contains(lx, ly)) return c.onMouseScroll(lx - c.x, ly - c.y, delta);
    }
    return false;
  }

  public int getActiveTab() { return activeTab; }
  public void setActiveTab(int i) { if (i >= 0 && i < tabs.size()) activeTab = i; }
  public int tabCount() { return tabs.size(); }
}
