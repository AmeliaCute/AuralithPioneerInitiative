package cute.ame.auralithpioneerinitiative.HoloPanel.Widget;

import cute.ame.auralithpioneerinitiative.HoloPanel.HoloPanelColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Consumer;

class LabelWidget extends HoloPanelWidget
{
  private String text;
  private int color;
  private float  scale;
  private int align;

  public LabelWidget(String text, int color) { this(text, color, 1.0f, -1); }
  public LabelWidget(String text, int color, float scale, int align)
  {
    this.text = text; this.color = color; this.scale = scale; this.align = align;
    this.h = (int)(Minecraft.getInstance().font.lineHeight * scale + 1);
  }

  public void setText(String t) { this.text = t; }

  @Override public int preferredHeight() { return (int)(Minecraft.getInstance().font.lineHeight * scale) + 2; }
  @Override public void render(GuiGraphics g, HoloPanelContext ctx, float pt)
  {
    var font = Minecraft.getInstance().font;
    float tw = font.width(text) * scale;
    float drawX = switch (align)
    {
      case 0 -> x + (w - tw) * 0.5f;
      case 1 -> x + w - tw - 2;
      default -> x + 2;
    };

    g.pose().pushPose();
    g.pose().translate(drawX, y + 1, 0);
    g.pose().scale(scale, scale, 1.0f);
    g.drawString(font, text, 0, 0, HoloPanelColors.opaque(color), false);
    g.pose().popPose();
  }
}

class SpacerWidget extends HoloPanelWidget
{
  private final int size;
  public SpacerWidget(int size) { this.size = size; this.h = size; }
  @Override public int preferredHeight() { return size; }
  @Override public void render(GuiGraphics g, HoloPanelContext ctx, float pt) {}
}

class SeparatorWidget extends HoloPanelWidget
{
  private final int thickness;
  private final int color;
  public SeparatorWidget() { this(1, HoloPanelColors.AMBER_BORDER); }
  public SeparatorWidget(int t, int color) { this.thickness = t; this.color = color; }
  @Override public int preferredHeight() { return thickness + 2; }
  @Override public void render(GuiGraphics g, HoloPanelContext ctx, float pt)
  {
    g.fill(x, y + 1, x + w, y + 1 + thickness, HoloPanelColors.opaque(color));
  }
}

class InfoRowWidget extends HoloPanelWidget
{
  private String label;
  private String value;
  private final int labelColor, valueColor;

  public InfoRowWidget(String label, String value, int labelColor, int valueColor)
  {
    this.label = label; this.value = value;
    this.labelColor = labelColor; this.valueColor = valueColor;
    this.h = Minecraft.getInstance().font.lineHeight + 3;
  }

  public void setValue(String v) { value = v; }
  @Override public int preferredHeight() { return Minecraft.getInstance().font.lineHeight + 3; }

  @Override public void render(GuiGraphics g, HoloPanelContext ctx, float pt)
  {
    var font = Minecraft.getInstance().font;
    g.drawString(font, label, x + 3, y + 2, HoloPanelColors.opaque(labelColor), false);
    int vx = x + w - font.width(value) - 3;
    g.drawString(font, value, vx, y + 2, HoloPanelColors.opaque(valueColor), false);
  }
}

class ProgressBarWidget extends HoloPanelWidget
{
  private float value;
  private final int bgColor, fillColor;
  private final boolean vertical;

  public ProgressBarWidget(float value, int bgColor, int fillColor, boolean vertical)
  {
    this.value = value; this.bgColor = bgColor; this.fillColor = fillColor; this.vertical = vertical;
  }

  public void setValue(float v) { value = Math.max(0, Math.min(1, v)); }

  @Override public void render(GuiGraphics g, HoloPanelContext ctx, float pt)
  {
    g.fill(x, y, x + w, y + h, HoloPanelColors.opaque(bgColor));
    if (!vertical)
    {
      int fw = (int)(w * value);
      g.fill(x, y, x + fw, y + h, HoloPanelColors.opaque(fillColor));
    } else
    {
      int fh = (int)(h * value);
      g.fill(x, y + h - fh, x + w, y + h, HoloPanelColors.opaque(fillColor));
    }
  }
}

class ButtonWidget extends HoloPanelWidget
{
  private final String label;
  private final String serverAction;
  private final Consumer<String> clickHandler;
  private boolean pressed = false;

  public ButtonWidget(String label, String serverAction, Consumer<String> clickHandler)
  {
    this.label = label; this.serverAction = serverAction; this.clickHandler = clickHandler;
  }

  @Override public int preferredHeight() { return Minecraft.getInstance().font.lineHeight + 6; }

  @Override public void render(GuiGraphics g, HoloPanelContext ctx, float pt)
  {
    int bg;
    if (!enabled) bg = HoloPanelColors.AMBER_DIM;
    else if (pressed) bg = HoloPanelColors.AMBER_ACTIVE;
    else if (hovered) bg = HoloPanelColors.AMBER_DIM;
    else bg = 0x1A0000;

    g.fill(x, y, x + w, y + h, HoloPanelColors.opaque(bg));
    g.fill(x, y, x + w, y + 1, HoloPanelColors.opaque(HoloPanelColors.AMBER_BORDER));
    g.fill(x, y + h - 1, x + w, y + h, HoloPanelColors.opaque(HoloPanelColors.AMBER_BORDER));
    g.fill(x, y, x + 1, y + h, HoloPanelColors.opaque(HoloPanelColors.AMBER_BORDER));
    g.fill(x + w - 1, y, x + w, y + h, HoloPanelColors.opaque(HoloPanelColors.AMBER_BORDER));

    var font = Minecraft.getInstance().font;
    int labelC = enabled ? HoloPanelColors.opaque(HoloPanelColors.AMBER_PRIMARY) : HoloPanelColors.opaque(HoloPanelColors.MUTED_TEXT);
    int lx = x + (w - font.width(label)) / 2;
    int ly = y + (h - font.lineHeight) / 2;
    g.drawString(font, label, lx, ly, labelC, false);
  }

  @Override public boolean onMouseClick(int lx, int ly, int button)
  {
    if (!enabled) return false;
    pressed = true;
    if (clickHandler != null) clickHandler.accept(serverAction);
    return true;
  }
}

class IconWidget extends HoloPanelWidget
{
  private final ResourceLocation texture;
  private final int u, v, texW, texH;

  public IconWidget(ResourceLocation texture, int u, int v, int texW, int texH)
  {
    this.texture = texture; this.u = u; this.v = v; this.texW = texW; this.texH = texH;
  }

  @Override public void render(GuiGraphics g, HoloPanelContext ctx, float pt)
  {
    g.blit(texture, x, y, u, v, w, h, texW, texH);
  }
}

class SegmentedBarWidget extends HoloPanelWidget
{
  public record Segment(float fraction, int color) {}
  private List<Segment> segments = List.of();

  public SegmentedBarWidget() {}
  public void setSegments(List<Segment> s) { segments = s; }

  @Override public void render(GuiGraphics g, HoloPanelContext ctx, float pt)
  {
    int cx = x;
    for (var seg : segments)
    {
      int sw = (int)(w * seg.fraction());
      g.fill(cx, y, cx + sw, y + h, HoloPanelColors.opaque(seg.color()));
      cx += sw;
    }
  }
}