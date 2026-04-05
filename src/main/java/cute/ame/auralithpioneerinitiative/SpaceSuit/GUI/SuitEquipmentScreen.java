package cute.ame.auralithpioneerinitiative.SpaceSuit.GUI;

import cute.ame.auralithpioneerinitiative.SpaceSuit.Data.ModuleSlotType;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Module.O2TankModule;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class SuitEquipmentScreen extends AbstractContainerScreen<SuitContainerMenu> {

  private static final int C_BG = 0xFF0A1210;
  private static final int C_PANEL = 0xFF0D1A17;
  private static final int C_BORDER = 0xFF0DB49C;
  private static final int C_BORDER_DIM = 0xFF064A40;
  private static final int C_TEAL = 0xFF0DB49C;
  private static final int C_TEXT_DIM = 0xFF3A8070;
  private static final int C_SLOT_BG = 0xFF141F1C;
  private static final int C_SLOT_HOVER = 0x440DB49C;

  static final int SLOT_S = 18;
  static final int SLOT_GAP = 2;
  static final int PANEL_LABEL_H = 14;
  static final int PANEL_PAD = 6;
  static final int PANEL_PAD_B = 6;

  static final int PANEL_H = PANEL_LABEL_H + SLOT_S + PANEL_PAD_B;

  static final int MODEL_X = 8;
  static final int MODEL_Y = 22;
  static final int MODEL_W = 118;
  static final int PIECES_X = 134;
  static final int PIECES_Y = 22;
  static final int PIECE_GAP = 6;
  static final int HELMET_Y = PIECES_Y;
  static final int CHEST_Y = HELMET_Y + PANEL_H + PIECE_GAP;
  static final int LEGS_Y = CHEST_Y  + PANEL_H + PIECE_GAP;

  static final int INV_Y = LEGS_Y + PANEL_H + 10;
  static final int HOTBAR_Y = INV_Y + 3*18 + 4;

  public SuitEquipmentScreen(SuitContainerMenu menu, Inventory inv, Component title)
  {
    super(menu, inv, title);
    this.imageWidth  = 300;
    this.imageHeight = HOTBAR_Y + 18 + 8;
  }

  @Override
  protected void init()
  {
    super.init();
    this.titleLabelX = -9999;
    this.inventoryLabelX = -9999;
  }

  @Override
  public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick)
  {
    renderBackground(gui, mouseX, mouseY, partialTick);
    super.render(gui, mouseX, mouseY, partialTick);
    renderTooltip(gui, mouseX, mouseY);
  }

  @Override
  protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY)
  {
    int ox = leftPos, oy = topPos;

    gui.fill(ox, oy, ox + imageWidth, oy + imageHeight, C_BG);
    drawBorder(gui, ox, oy, imageWidth, imageHeight, C_BORDER);

    gui.drawString(font, "SUIT EQUIPMENT", ox + 8, oy + 6, C_TEAL, false);
    gui.fill(ox + 1, oy + 16, ox + imageWidth - 1, oy + 17, C_BORDER_DIM);

    int modelH = LEGS_Y + PANEL_H - MODEL_Y;
    renderModel(gui, ox + MODEL_X, oy + MODEL_Y, MODEL_W, modelH, mouseX, mouseY);

    renderPiece(gui, ox + PIECES_X, oy + HELMET_Y, "HELMET",     39, mouseX, mouseY);
    renderPiece(gui, ox + PIECES_X, oy + CHEST_Y,  "CHESTPLATE", 38, mouseX, mouseY);
    renderPiece(gui, ox + PIECES_X, oy + LEGS_Y,   "LEGGINGS",   37, mouseX, mouseY);

    gui.fill(ox + 1, oy + INV_Y - 5, ox + imageWidth - 1, oy + INV_Y - 4, C_BORDER_DIM);
  }

  private void renderModel(GuiGraphics gui, int px, int py, int pw, int ph, int mouseX, int mouseY)
  {
    gui.fill(px, py, px + pw, py + ph, C_PANEL);
    drawBorder(gui, px, py, pw, ph, C_BORDER_DIM);

    InventoryScreen.renderEntityInInventoryFollowsMouse(gui, px + 2, py + 2, px + pw - 2, py + ph - 2, 42, 0.0625f, mouseX, mouseY, minecraft.player);
    String name = minecraft.player.getScoreboardName();
    gui.drawString(font, name, px + (pw - font.width(name)) / 2, py + 5, C_TEXT_DIM, false);
  }

  private void renderPiece(GuiGraphics gui, int px, int py, String label, int armorSlot, int mouseX, int mouseY)
  {
    var slots = menu.getModuleSlots().stream().filter(s -> s.getArmorSlot() == armorSlot).toList();

    int slotCount = slots.size();
    int panelW = PANEL_PAD + slotCount * (SLOT_S + SLOT_GAP) - SLOT_GAP + 4 + 18 + PANEL_PAD;

    gui.fill(px, py, px + panelW, py + PANEL_H, C_PANEL);
    drawBorder(gui, px, py, panelW, PANEL_H, C_BORDER_DIM);

    gui.drawString(font, label, px + PANEL_PAD, py + 3, C_TEAL, false);

    ItemStack armor = minecraft.player.getInventory().getItem(armorSlot);
    if (!armor.isEmpty()) gui.renderItem(armor, px + panelW - 18 - 2, py + (PANEL_H - 16) / 2);

    int sx = px + PANEL_PAD;
    int sy = py + PANEL_LABEL_H;
    for (SuitModuleSlot slot : slots)
    {
      renderSlotBg(gui, sx, sy, slot, mouseX, mouseY);
      sx += SLOT_S + SLOT_GAP;
    }
  }

  private void renderSlotBg(GuiGraphics gui, int sx, int sy, SuitModuleSlot slot, int mouseX, int mouseY)
  {
    ModuleSlotType type = slot.getAcceptedType();
    int col = slotColor(type);
    boolean hovered = mouseX >= sx && mouseX < sx + SLOT_S && mouseY >= sy && mouseY < sy + SLOT_S;

    gui.fill(sx, sy, sx + SLOT_S, sy + SLOT_S, C_SLOT_BG);
    gui.fill(sx, sy, sx+SLOT_S, sy+1, col);
    gui.fill(sx, sy+SLOT_S-1, sx+SLOT_S, sy+SLOT_S, col);
    gui.fill(sx, sy, sx+1, sy+SLOT_S,   col);
    gui.fill(sx+SLOT_S-1, sy, sx+SLOT_S, sy+SLOT_S, col);

    if (hovered) gui.fill(sx+1, sy+1, sx+SLOT_S-1, sy+SLOT_S-1, C_SLOT_HOVER);

    if (!slot.hasItem())
    {
      String lbl = slotLabel(type);
      gui.drawString(font, lbl, sx + (SLOT_S - font.width(lbl)) / 2, sy + (SLOT_S - font.lineHeight) / 2, col & 0x00FFFFFF | 0x66000000, false);
    }
    else
    {
      ItemStack content = slot.getItem();
      if (content.getItem() instanceof O2TankModule)
      {
        int stored = O2TankModule.getStored(content);
        int cap = O2TankModule.getCapacity(content);
        if (cap > 0) {
          int bw = Math.round((SLOT_S - 4) * (float) stored / cap);
          gui.fill(sx+2, sy+SLOT_S-3, sx+SLOT_S-2, sy+SLOT_S-1, 0xAA000000);
          int bc = stored > cap*0.5f ? 0xFF00D150 : stored > cap*0.2f ? 0xFFFFC000 : 0xFFFF2200;
          if (bw > 0) gui.fill(sx+2, sy+SLOT_S-3, sx+2+bw, sy+SLOT_S-1, bc);
        }
      }
    }
  }

  private void drawBorder(GuiGraphics gui, int x, int y, int w, int h, int c)
  {
    gui.fill(x, y, x+w, y+1, c);
    gui.fill(x, y+h-1, x+w, y+h, c);
    gui.fill(x, y, x+1, y+h, c);
    gui.fill(x+w-1, y, x+w, y+h, c);
  }

  private static int slotColor(ModuleSlotType t)
  {
    return switch (t)
    {
      case REGULATOR -> 0xFF4FC3F7;
      case REBREATHER -> 0xFF4DB6AC;
      case O2_TANK -> 0xFF00E5FF;
      case FLASHLIGHT -> 0xFFFFD54F;
      case BATTERY -> 0xFFAED581;
      case JETPACK -> 0xFFFF8A65;
      case FREE -> 0xFF607D8B;
    };
  }

  private static String slotLabel(ModuleSlotType t)
  {
    return switch (t)
    {
      case REGULATOR -> "REG";
      case REBREATHER -> "RBR";
      case O2_TANK -> "O2";
      case FLASHLIGHT -> "FLT";
      case BATTERY -> "BAT";
      case JETPACK -> "JET";
      case FREE -> " ";
    };
  }

  @Override
  protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {}
}