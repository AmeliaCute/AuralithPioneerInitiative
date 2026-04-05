package cute.ame.auralithpioneerinitiative.SpaceSuit.GUI;

import cute.ame.auralithpioneerinitiative.Registrie.ModDataComponents;
import cute.ame.auralithpioneerinitiative.Registrie.ModMenuTypes;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Data.SuitInventoryComponent;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Data.ModuleSlotType;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Item.SuitArmorItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class SuitContainerMenu extends AbstractContainerMenu
{
  private static final int SLOT_HEAD = 39;
  private static final int SLOT_CHEST = 38;
  private static final int SLOT_LEGS = 37;

  private static final int PANEL_X = 140;
  private static final int PANEL_HELMET_Y = 24;
  private static final int PANEL_CHEST_Y = 90;
  private static final int PANEL_LEGS_Y = 156;
  private static final int SLOT_OFFSET_Y = 14;
  private static final int SLOT_S = 18;
  private static final int SLOT_GAP = 2;

  private final Inventory playerInv;

  private final List<SuitModuleSlot> moduleSlots = new ArrayList<>();

  public SuitContainerMenu(int windowId, Inventory playerInv, FriendlyByteBuf buf)
  {
    super(ModMenuTypes.SUIT_EQUIPMENT.get(), windowId);
    this.playerInv = playerInv;
    buildSlots();
  }

  public SuitContainerMenu(int windowId, Inventory playerInv)
  {
    super(ModMenuTypes.SUIT_EQUIPMENT.get(), windowId);
    this.playerInv = playerInv;
    buildSlots();
  }

  private void buildSlots()
  {
    addPieceSlots(39, SuitEquipmentScreen.PIECES_X + SuitEquipmentScreen.PANEL_PAD + 1, SuitEquipmentScreen.HELMET_Y + SuitEquipmentScreen.PANEL_LABEL_H + 1);
    addPieceSlots(38, SuitEquipmentScreen.PIECES_X + SuitEquipmentScreen.PANEL_PAD + 1, SuitEquipmentScreen.CHEST_Y + SuitEquipmentScreen.PANEL_LABEL_H + 1);
    addPieceSlots(37, SuitEquipmentScreen.PIECES_X + SuitEquipmentScreen.PANEL_PAD + 1, SuitEquipmentScreen.LEGS_Y + SuitEquipmentScreen.PANEL_LABEL_H + 1);

    int invY = SuitEquipmentScreen.INV_Y;
    int hotbarY = SuitEquipmentScreen.HOTBAR_Y;

    for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, invY + row * 18));
    for (int col = 0; col < 9; col++) addSlot(new Slot(playerInv, col, 8 + col * 18, hotbarY));
  }

  private void addPieceSlots(int armorSlot, int baseX, int baseY)
  {
    ItemStack armor = playerInv.getItem(armorSlot);
    if (!(armor.getItem() instanceof SuitArmorItem suit)) return;

    for (int i = 0; i < suit.getDefinition().slotCount(); i++)
    {
      ModuleSlotType type = suit.getDefinition().slots()[i];
      int sx = baseX + i * (SuitEquipmentScreen.SLOT_S + SuitEquipmentScreen.SLOT_GAP);
      SuitModuleSlot slot = new SuitModuleSlot(playerInv.player, armorSlot, i, type, sx, baseY);
      moduleSlots.add(slot);
      addSlot(slot);
    }
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index)
  {
    Slot slot = slots.get(index);
    if (!slot.hasItem()) return ItemStack.EMPTY;
    ItemStack stack = slot.getItem().copy();

    if (slot instanceof SuitModuleSlot)
      if (!moveItemStackTo(stack, moduleSlots.size(), slots.size(), true)) return ItemStack.EMPTY;
    else
    {
      boolean moved = false;
      for (SuitModuleSlot ms : moduleSlots)
      {
        if (ms.mayPlace(stack))
        {
          if (moveItemStackTo(stack, slots.indexOf(ms), slots.indexOf(ms) + 1, false))
          {
            moved = true;
            break;
          }
        }
      }
      if (!moved) return ItemStack.EMPTY;
    }

    slot.set(stack.isEmpty() ? ItemStack.EMPTY : stack);
    slot.setChanged();
    return stack;
  }

  @Override
  public boolean stillValid(Player player) { return true; }

  public List<SuitModuleSlot> getModuleSlots() { return moduleSlots; }
}