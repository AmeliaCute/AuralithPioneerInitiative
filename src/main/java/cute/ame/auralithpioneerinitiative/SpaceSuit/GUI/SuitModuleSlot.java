package cute.ame.auralithpioneerinitiative.SpaceSuit.GUI;

import cute.ame.auralithpioneerinitiative.Registrie.ModDataComponents;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Data.ModuleSlotType;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Data.SuitInventoryComponent;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Item.SuitArmorItem;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Module.SuitModule;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class SuitModuleSlot extends Slot
{

  private final Player player;
  private final int armorSlot;
  private final int moduleIndex;
  private final ModuleSlotType acceptedType;

  private static final Container DUMMY = new net.minecraft.world.SimpleContainer(1);

  public SuitModuleSlot(Player player, int armorSlot, int moduleIndex, ModuleSlotType acceptedType, int x, int y) {
    super(DUMMY, moduleIndex, x, y);
    this.player = player;
    this.armorSlot = armorSlot;
    this.moduleIndex = moduleIndex;
    this.acceptedType = acceptedType;
  }

  @Override
  public boolean mayPlace(ItemStack stack)
  {
    if (!(stack.getItem() instanceof SuitModule module)) return false;
    return module.getSlotType() == acceptedType || acceptedType == ModuleSlotType.FREE;
  }

  @Override
  public ItemStack getItem()
  {
    return readFromArmor();
  }

  @Override
  public void set(ItemStack stack)
  {
    writeToArmor(stack);
    setChanged();
  }

  @Override
  public void setChanged() {}

  @Override
  public int getMaxStackSize() { return 1; }

  @Override
  public boolean hasItem() { return !getItem().isEmpty(); }

  @Override
  public ItemStack remove(int amount)
  {
    ItemStack current = readFromArmor();
    if (current.isEmpty()) return ItemStack.EMPTY;

    writeToArmor(ItemStack.EMPTY);
    return current;
  }

  private ItemStack readFromArmor()
  {
    ItemStack armor = player.getInventory().getItem(armorSlot);
    if (armor.isEmpty()) return ItemStack.EMPTY;
    SuitInventoryComponent inv = armor.getOrDefault(ModDataComponents.SUIT_INVENTORY.get(), SuitInventoryComponent.ofSize(0));
    return inv.get(moduleIndex);
  }

  private void writeToArmor(ItemStack stack)
  {
    ItemStack armor = player.getInventory().getItem(armorSlot);
    if (armor.isEmpty()) return;

    SuitInventoryComponent inv = armor.getOrDefault(ModDataComponents.SUIT_INVENTORY.get(), SuitInventoryComponent.ofSize(moduleIndex + 1));
    armor.set(ModDataComponents.SUIT_INVENTORY.get(), inv.with(moduleIndex, stack));
  }

  public ModuleSlotType getAcceptedType() { return acceptedType; }
  public int getArmorSlot() { return armorSlot; }
}