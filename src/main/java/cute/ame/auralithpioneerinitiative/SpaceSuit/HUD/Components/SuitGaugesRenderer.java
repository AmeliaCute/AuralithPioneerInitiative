package cute.ame.auralithpioneerinitiative.SpaceSuit.HUD.Components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import cute.ame.auralithpioneerinitiative.Registrie.ModAttachments;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Data.SuitData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

public final class SuitGaugesRenderer {
  private SuitGaugesRenderer() {}

  private static final int PANEL_W = 120;
  private static final int PANEL_H = 35;
  private static final int GAP_Y = 5;
  private static final int CUT_X = 15;

  private static final int CIRCLE_R = 40;
  private static final int CIRCLE_GAP = 20;
  private static final int C_PANEL_BG  = 0xCC1A2626;
  private static final int C_BORDER = 0xFF0DB49C;

  private static final int C_CYAN = 0xFF0DB49C;
  private static final int C_YELLOW = 0xFFFFC000;
  private static final int C_GREEN = 0xFF00D150;
  private static final int C_BAR_BG = 0x44000000;

  public static void render(GuiGraphics gui, int screenH)
  {
    Minecraft mc = Minecraft.getInstance();
    if (mc.player == null) return;
    Font font = mc.font;
    SuitData data = mc.player.getData(ModAttachments.SUIT_DATA);

    float o2f = data.o2Frac();
    float enf = data.energyFrac();
    boolean fl = data.isFlashlight();

    Matrix4f matrix = gui.pose().last().pose();

    int startX = 20;
    int startY = screenH - (PANEL_H * 2) - GAP_Y - 20;
    int py1 = startY;
    drawCutPanel(matrix, startX, py1, PANEL_W, PANEL_H, CUT_X, C_PANEL_BG, C_BORDER);
    gui.drawString(font, "⚡", startX + 25, py1 + 20, C_YELLOW, false);
    gui.drawString(font, Math.round(enf * 100) + "%", startX + PANEL_W - 35, py1 + 20, C_YELLOW, false);
    drawSegmentedBar(matrix, startX + 10, py1 + 5, PANEL_W - 20, 8, 30, enf, C_YELLOW);

    int py2 = startY + PANEL_H + GAP_Y;
    drawCutPanel(matrix, startX, py2, PANEL_W, PANEL_H, CUT_X, C_PANEL_BG, C_BORDER);
    gui.drawString(font, "O2", startX + 25, py2 + 20, C_CYAN, false);
    gui.drawString(font, Math.round(o2f * 100) + "%", startX + PANEL_W - 35, py2 + 20, C_CYAN, false);
    drawSegmentedBar(matrix, startX + 10, py2 + 5, PANEL_W - 20, 8, 30, o2f, C_CYAN);

    int cx = startX + PANEL_W + CIRCLE_GAP + CIRCLE_R;
    int cy = startY + (PANEL_H * 2 + GAP_Y) / 2;
    drawCircle(matrix, cx, cy, CIRCLE_R, C_PANEL_BG);
    drawCircleRing(matrix, cx, cy, CIRCLE_R - 5, fl ? C_GREEN : 0x55005520);

    if (fl)
    {
      gui.fill(cx - 10, cy - 2, cx + 10, cy + 2, C_GREEN);
      gui.fill(cx - 2, cy - 10, cx + 2, cy + 10, C_GREEN);
    }

    String flText = fl ? "ON" : "OFF";
    int tw = font.width(flText);
    gui.drawString(font, flText, cx - (tw / 2), cy + CIRCLE_R - 15, fl ? C_GREEN : 0xFF888888, false);
  }

  private static void drawCutPanel(Matrix4f matrix, float x, float y, float w, float h, float cut, int bgCol, int borderCol)
  {
    RenderSystem.enableBlend();
    RenderSystem.setShader(GameRenderer::getPositionColorShader);
    Tesselator tesselator = Tesselator.getInstance();

    BufferBuilder bg = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    bg.addVertex(matrix, x, y, 0).setColor(bgCol);
    bg.addVertex(matrix, x + cut, y + h, 0).setColor(bgCol);
    bg.addVertex(matrix, x + w, y + h, 0).setColor(bgCol);
    bg.addVertex(matrix, x + w, y, 0).setColor(bgCol);
    MeshData bgData = bg.build();
    if (bgData != null) BufferUploader.drawWithShader(bgData);

    BufferBuilder border = tesselator.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
    border.addVertex(matrix, x, y, 0).setColor(borderCol);
    border.addVertex(matrix, x + w, y, 0).setColor(borderCol);
    border.addVertex(matrix, x + cut, y + h, 0).setColor(borderCol);
    border.addVertex(matrix, x + w, y + h, 0).setColor(borderCol);
    border.addVertex(matrix, x, y, 0).setColor(borderCol);
    border.addVertex(matrix, x + cut, y + h, 0).setColor(borderCol);
    border.addVertex(matrix, x + w, y, 0).setColor(borderCol);
    border.addVertex(matrix, x + w, y + h, 0).setColor(borderCol);

    MeshData borderData = border.build();
    if (borderData != null) BufferUploader.drawWithShader(borderData);
  }

  private static void drawSegmentedBar(Matrix4f matrix, float x, float y, float w, float h, int segments, float frac, int color) {
    RenderSystem.enableBlend();
    RenderSystem.setShader(GameRenderer::getPositionColorShader);
    Tesselator tesselator = Tesselator.getInstance();
    BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

    int activeSegments = Math.round(segments * frac);
    float segW = (w / segments) - 1f;

    for (int i = 0; i < segments; i++)
    {
      float sx = x + (i * (w / segments));
      int segColor = (i < activeSegments) ? color : C_BAR_BG;

      buffer.addVertex(matrix, sx, y, 0).setColor(segColor);
      buffer.addVertex(matrix, sx, y + h, 0).setColor(segColor);
      buffer.addVertex(matrix, sx + segW, y + h, 0).setColor(segColor);
      buffer.addVertex(matrix, sx + segW, y, 0).setColor(segColor);
    }

    MeshData data = buffer.build();
    if (data != null) BufferUploader.drawWithShader(data);
  }

  private static void drawCircle(Matrix4f matrix, float cx, float cy, float r, int color)
  {
    RenderSystem.enableBlend();
    RenderSystem.setShader(GameRenderer::getPositionColorShader);
    Tesselator tesselator = Tesselator.getInstance();
    BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

    buffer.addVertex(matrix, cx, cy, 0).setColor(color);
    for (int i = 0; i <= 32; i++)
    {
      double angle = Math.PI * 2 * i / 32.0;
      float vx = cx + (float) Math.cos(angle) * r;
      float vy = cy + (float) Math.sin(angle) * r;
      buffer.addVertex(matrix, vx, vy, 0).setColor(color);
    }

    MeshData data = buffer.build();
    if (data != null) BufferUploader.drawWithShader(data);
  }

  private static void drawCircleRing(Matrix4f matrix, float cx, float cy, float r, int color)
  {
    RenderSystem.enableBlend();
    RenderSystem.setShader(GameRenderer::getPositionColorShader);
    Tesselator tesselator = Tesselator.getInstance();
    BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

    for (int i = 0; i <= 32; i++)
    {
      double angle = Math.PI * 2 * i / 32.0;
      float vx = cx + (float) Math.cos(angle) * r;
      float vy = cy + (float) Math.sin(angle) * r;
      buffer.addVertex(matrix, vx, vy, 0).setColor(color);
    }

    MeshData data = buffer.build();
    if (data != null) BufferUploader.drawWithShader(data);
  }
}