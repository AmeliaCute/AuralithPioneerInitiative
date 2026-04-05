package cute.ame.auralithpioneerinitiative.SpaceSuit.Module;

import cute.ame.auralithpioneerinitiative.Registrie.ModDataComponents;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Data.ModuleSlotType;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Data.O2TankTier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public final class O2TankModule extends SuitModule {

  private final O2TankTier tier;

  public O2TankModule(O2TankTier tier)
  {
    super(ModuleSlotType.O2_TANK, new Properties().stacksTo(1));
    this.tier = tier;
  }

  public O2TankTier getTier() { return tier; }

  public static int getStored(ItemStack stack)
  {
    return stack.getOrDefault(ModDataComponents.O2_STORED.get(), 0);
  }

  public static int getCapacity(ItemStack stack) {
    if (!(stack.getItem() instanceof O2TankModule m)) return 0;
    return m.tier.capacity;
  }

  public static int drain(ItemStack stack, int amount)
  {
    int cur = getStored(stack);
    int drained = Math.min(cur, amount);
    if (drained > 0) stack.set(ModDataComponents.O2_STORED.get(), cur - drained);
    return drained;
  }

  public static int refill(ItemStack stack, int amount)
  {
    int cur = getStored(stack);
    int cap = getCapacity(stack);
    int added = Math.min(amount, cap - cur);
    if (added > 0) stack.set(ModDataComponents.O2_STORED.get(), cur + added);
    return added;
  }

  public ItemStack fullStack()
  {
    ItemStack s = new ItemStack(this);
    s.set(ModDataComponents.O2_STORED.get(), tier.capacity);
    return s;
  }

  @Override
  public boolean isBarVisible(ItemStack stack) { return true; }

  @Override
  public int getBarWidth(ItemStack stack)
  {
    int cap = getCapacity(stack);
    return cap == 0 ? 0 : Math.round(13f * getStored(stack) / cap);
  }

  @Override
  public int getBarColor(ItemStack stack)
  {
    float frac = getCapacity(stack) == 0 ? 0f : (float) getStored(stack) / getCapacity(stack);
    if (frac > 0.5f) return 0x00D150;
    if (frac > 0.2f) return 0xFFC000;
    return 0xFF2200;
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> lines, TooltipFlag flag)
  {
    int stored = getStored(stack);
    int cap = getCapacity(stack);
    int pct = cap == 0 ? 0 : stored * 100 / cap;
    int minFull = stored / 1200;
    int secRem = (stored % 1200) / 20;
    lines.add(Component.literal(String.format("§7O2: §f%d§7/§f%d §7(%d%%) - §f%dm%02ds", stored, cap, pct, minFull, secRem)));
    lines.add(Component.literal("§8Tier: §7" + tier.name()));
  }
}