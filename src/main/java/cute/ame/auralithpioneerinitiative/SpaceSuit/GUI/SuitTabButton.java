package cute.ame.auralithpioneerinitiative.SpaceSuit.GUI;

import cute.ame.auralithpioneerinitiative.SpaceSuit.Network.OpenSuitMenuPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public final class SuitTabButton extends AbstractButton {

  private static final int TAB_W = 28;
  private static final int TAB_H = 28;

  private static final int C_BG = 0xFF050F0D;
  private static final int C_BORDER = 0xFF0DB49C;
  private static final int C_BORDER_DIM = 0xFF064A40;
  private static final int C_HOVER = 0x220DB49C;
  private static final int C_TEXT = 0xFF0DB49C;

  public SuitTabButton(int x, int y)
  {
    super(x, y, TAB_W, TAB_H, Component.empty());
  }

  @Override
  public void onPress()
  {
    PacketDistributor.sendToServer(new OpenSuitMenuPacket());
  }

  @Override
  protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick)
  {
    int x = getX(), y = getY();
    boolean hovered = isHovered();

    gui.fill(x, y, x + TAB_W, y + TAB_H, C_BG);
    if (hovered) gui.fill(x, y, x + TAB_W, y + TAB_H, C_HOVER);

    int c = hovered ? C_BORDER : C_BORDER_DIM;
    gui.fill(x, y, x + TAB_W , y + 1, c);
    gui.fill(x, y+TAB_H-1, x + TAB_W, y + TAB_H, c);
    gui.fill(x, y, x + 1, y + TAB_H, c);
    gui.fill(x+TAB_W-1, y, x + TAB_W, y + TAB_H , c);
    gui.fill(x-1, y+1, x, y, c);
    gui.fill(x+TAB_W, y+1, x+TAB_W+1, y, c);
    gui.fill(x-1, y+TAB_H, x, y+TAB_H-1, c);
    gui.fill(x+TAB_W, y+TAB_H, x+TAB_W+1, y+TAB_H-1, c);

    var font = Minecraft.getInstance().font;
    gui.drawString(font, "O2", x + (TAB_W - font.width("O2")) / 2, y + 6, C_TEXT, false);
    gui.drawString(font, "\u25A0\u25A0", x + (TAB_W - font.width("\u25A0\u25A0")) / 2, y + 16, hovered ? C_BORDER : C_BORDER_DIM, false);

    if (hovered) gui.renderTooltip(font, Component.literal("Suit Equipment [G]"), mouseX, mouseY);
  }

  @Override
  protected void updateWidgetNarration(NarrationElementOutput output)
  {
    defaultButtonNarrationText(output);
  }
}