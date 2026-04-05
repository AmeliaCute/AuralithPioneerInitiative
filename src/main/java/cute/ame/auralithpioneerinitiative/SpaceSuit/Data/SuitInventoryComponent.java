package cute.ame.auralithpioneerinitiative.SpaceSuit.Data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record SuitInventoryComponent(List<ItemStack> modules)
{
  public static final SuitInventoryComponent EMPTY = new SuitInventoryComponent(List.of());

  public static final Codec<SuitInventoryComponent> CODEC = RecordCodecBuilder.create(i -> i.group(
      ItemStack.OPTIONAL_CODEC.listOf().fieldOf("modules").forGetter(SuitInventoryComponent::modules)
  ).apply(i, SuitInventoryComponent::new));

  public static SuitInventoryComponent ofSize(int slotCount)
  {
    List<ItemStack> list = new ArrayList<>(slotCount);
    for (int i = 0; i < slotCount; i++) list.add(ItemStack.EMPTY);
    return new SuitInventoryComponent(Collections.unmodifiableList(list));
  }

  public ItemStack get(int index)
  {
    return index >= 0 && index < modules.size() ? modules.get(index) : ItemStack.EMPTY;
  }

  public SuitInventoryComponent with(int index, ItemStack stack)
  {
    List<ItemStack> copy = new ArrayList<>(modules);
    while (copy.size() <= index) copy.add(ItemStack.EMPTY);
    copy.set(index, stack.copy());
    return new SuitInventoryComponent(Collections.unmodifiableList(copy));
  }

  public int size() { return modules.size(); }
}