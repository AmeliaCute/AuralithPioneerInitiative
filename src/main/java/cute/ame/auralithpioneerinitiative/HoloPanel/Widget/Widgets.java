package cute.ame.auralithpioneerinitiative.HoloPanel.Widget;

import cute.ame.auralithpioneerinitiative.HoloPanel.HoloPanelColors;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Consumer;

public final class Widgets
{
  private Widgets() {}

  public static LabelWidget label(String text, int color) { return new LabelWidget(text, color); }
  public static LabelWidget label(String text, int color, float scale, int align) { return new LabelWidget(text, color, scale, align); }

  public static LabelWidget labelCenter(String text, int color) { return new LabelWidget(text, color, 1.0f, 0); }
  public static LabelWidget labelCenter(String text, int color, float scale, int align) { return new LabelWidget(text, color, scale, 0); }

  public static LabelWidget labelRight(String text, int color) { return new LabelWidget(text, color, 1.0f, 1); }

  public static SpacerWidget spacer(int height) { return new SpacerWidget(height); }
  public static SeparatorWidget separator() { return new SeparatorWidget(); }
  public static SeparatorWidget separator(int thickness, int color) { return new SeparatorWidget(thickness, color); }

  public static InfoRowWidget infoRow(String label, String value) { return new InfoRowWidget(label, value, HoloPanelColors.AMBER_PRIMARY, HoloPanelColors.WHITE_VALUE); }
  public static InfoRowWidget infoRow(String label, String value, int labelColor, int valueColor)  { return new InfoRowWidget(label, value, labelColor, valueColor); }

  public static ProgressBarWidget progressBar(float value, int bgColor, int fillColor)  { return new ProgressBarWidget(value, bgColor, fillColor, false); }
  public static ProgressBarWidget progressBarV(float value, int bgColor, int fillColor) { return new ProgressBarWidget(value, bgColor, fillColor, true);  }

  public static ButtonWidget button(String label, String action, Consumer<String> onClick) { return new ButtonWidget(label, action, onClick); }

  public static IconWidget icon(ResourceLocation texture, int u, int v, int texW, int texH) { return new IconWidget(texture, u, v, texW, texH); }
  public static SegmentedBarWidget segmentedBar() { return new SegmentedBarWidget(); }

  public static ListWidget<String> stringList(List<String> items, Consumer<String> onSelect)
  {
    return new ListWidget<>(items, (g, item, ix, iy, iw, ih, selected, hov, ctx, pt) ->
    {
      int bg = selected ? HoloPanelColors.SELECT_ACTIVE : (hov ? HoloPanelColors.SELECT_HOVER : (iy % 2 == 0 ? HoloPanelColors.PANEL_BG_ALT : HoloPanelColors.PANEL_BG));
      g.fill(ix, iy, ix + iw, iy + ih, bg | 0xFF000000);
      g.drawString(net.minecraft.client.Minecraft.getInstance().font, item, ix + 3, iy + 2, HoloPanelColors.opaque(HoloPanelColors.LIGHT_TEXT), false);
    }, onSelect);
  }
}