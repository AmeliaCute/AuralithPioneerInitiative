package cute.ame.auralithpioneerinitiative.SpaceSuit.HUD.Components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import cute.ame.auralithpioneerinitiative.API.AuralithAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

public final class CompassRenderer
{
  private CompassRenderer() {}

  private static final int BAR_W = 500;
  private static final int BAR_H = 34;
  private static final int TOP_Y = 10;
  private static final int INSET = 15;
  private static final int SIDE_W = 95;
  private static final float FOV = 60f;

  private static final int C_BG = 0xAA010908;
  private static final int C_BORDER = 0xFF0D7A6A;
  private static final int C_LABEL = 0xFF08645A;
  private static final int C_VALUE = 0xFF1ADFC0;
  private static final int C_TICK_SM = 0xFF0A4A3E;
  private static final int C_TICK_MED = 0xFF0F8070;
  private static final int C_CARDINAL = 0xFF1ADFC0;

  public static void render(GuiGraphics gui, int screenW)
  {
    Minecraft mc = Minecraft.getInstance();
    if (mc.player == null) return;
    Font font = mc.font;

    float bearing = ((mc.player.getYRot() + 180f) % 360f + 360f) % 360f;
    float gravity = AuralithAPI.getGravityFor(mc.player.level().dimension());
    double lat = mc.player.getZ() * 0.0001;
    double lon = mc.player.getX() * 0.0001;

    int bx = (screenW - BAR_W) / 2;
    int by = TOP_Y;
    Matrix4f matrix = gui.pose().last().pose();

    drawBackground(matrix, bx, by, BAR_W, BAR_H, INSET, C_BG, C_BORDER);

    int lx = bx + INSET + 8;
    gui.drawString(font, "TEMP", lx, by + 6,  C_LABEL, false);
    gui.drawString(font, "---",  lx + 34, by + 6,  C_VALUE, false);
    lx += 3;
    gui.drawString(font, "GRAV", lx, by + 18, C_LABEL, false);
    gui.drawString(font, String.format("%.2fG", gravity), lx + 34, by + 18, C_VALUE, false);

    int rx = bx + BAR_W - SIDE_W + 8;
    gui.drawString(font, "LAT",  rx, by + 6,  C_LABEL, false);
    gui.drawString(font, fmtCoord(lat), rx + 28, by + 6,  C_VALUE, false);
    rx -= 3;
    gui.drawString(font, "LONG", rx, by + 18, C_LABEL, false);
    gui.drawString(font, fmtCoord(lon), rx + 28, by + 18, C_VALUE, false);

    renderTicks(gui, font, matrix, bx + SIDE_W, by, BAR_W - (SIDE_W * 2), BAR_H, bearing);

    int cx = bx + BAR_W / 2;
    int ty = by + BAR_H + 2;
    gui.fill(cx - 3, ty, cx + 4, ty + 1, C_CARDINAL);
    gui.fill(cx - 1, ty + 1, cx + 2, ty + 3, C_CARDINAL);
  }

  private static void renderTicks(GuiGraphics gui, Font font, Matrix4f matrix, int tx, int ty, int tw, int th, float bearing)
  {
    float ppd = tw / FOV;
    int centerX = tx + tw / 2;

    int dStart = (int) Math.floor((bearing - FOV * 0.5f - 10f) / 10f) * 10;
    int dEnd   = (int) Math.ceil ((bearing + FOV * 0.5f + 10f) / 10f) * 10;
    String[] CARD = { "N", "E", "S", "W" };

    for (int d = dStart; d <= dEnd; d += 10) {
      int norm = ((d % 360) + 360) % 360;
      float off = d - bearing;
      if (off >  180f) off -= 360f;
      if (off < -180f) off += 360f;

      int px = centerX + Math.round(off * ppd);
      if (px < tx + 2 || px > tx + tw - 3) continue;

      boolean isCard  = (norm % 90 == 0);
      boolean isMajor = (norm % 30 == 0);
      int tickH = isCard ? th - 20 : isMajor ? 8 : 5;
      int tCol  = isCard ? C_CARDINAL : isMajor ? C_TICK_MED : C_TICK_SM;

      gui.fill(px, ty + th - 4 - tickH, px + 1, ty + th - 4, tCol);

      if (isMajor)
      {
        String label = isCard ? CARD[(norm / 90) % 4] : String.valueOf(norm);
        int lw = font.width(label);
        gui.drawString(font, label, 1 + px - lw / 2, ty + 4, isCard ? C_CARDINAL : C_TICK_MED, false);
      }
    }
  }

  private static void drawBackground(Matrix4f matrix, float x, float y, float w, float h, float inset, int color, int borderColor)
  {
    RenderSystem.enableBlend();
    RenderSystem.setShader(GameRenderer::getPositionColorShader);
    Tesselator tesselator = Tesselator.getInstance();

    BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    buffer.addVertex(matrix, x, y, 0).setColor(color);
    buffer.addVertex(matrix, x + inset, y + h, 0).setColor(color);
    buffer.addVertex(matrix, x + w - inset, y + h, 0).setColor(color);
    buffer.addVertex(matrix, x + w, y, 0).setColor(color);
    MeshData data = buffer.build();
    if (data != null) BufferUploader.drawWithShader(data);

    BufferBuilder border = tesselator.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
    border.addVertex(matrix, x, y, 0).setColor(borderColor);
    border.addVertex(matrix, x + inset, y + h, 0).setColor(borderColor);
    border.addVertex(matrix, x + w - inset, y + h, 0).setColor(borderColor);
    border.addVertex(matrix, x + w, y, 0).setColor(borderColor);
    border.addVertex(matrix, x, y, 0).setColor(borderColor);
    MeshData bData = border.build();
    if (bData != null) BufferUploader.drawWithShader(bData);

    BufferBuilder lines = tesselator.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
    lines.addVertex(matrix, x + SIDE_W, y, 0).setColor(borderColor);
    lines.addVertex(matrix, x + SIDE_W + 5, y + h, 0).setColor(borderColor);
    lines.addVertex(matrix, x + w - SIDE_W, y, 0).setColor(borderColor);
    lines.addVertex(matrix, x + w - SIDE_W - 5, y + h, 0).setColor(borderColor);
    MeshData lData = lines.build();
    if (lData != null) BufferUploader.drawWithShader(lData);
  }

  private static String fmtCoord(double v)
  {
    return String.format("%+.4f°", v);
  }
}