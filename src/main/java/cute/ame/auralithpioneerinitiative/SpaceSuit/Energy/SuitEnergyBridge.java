package cute.ame.auralithpioneerinitiative.SpaceSuit.Energy;

import cute.ame.auralithpioneerinitiative.Helper.MiEnergyHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public final class SuitEnergyBridge
{
  private SuitEnergyBridge() {}

  private static final boolean MI_LOADED =
      ModList.get().isLoaded("modern_industrialization");

  public static long extractEU(ItemStack stack, long maxEu, boolean simulate) {
    if (!MI_LOADED || stack.isEmpty()) return 0L;
    return MiEnergyHelper.extract(stack, maxEu, simulate);
  }

  public static long getStoredEU(ItemStack stack) {
    if (!MI_LOADED || stack.isEmpty()) return 0L;
    return MiEnergyHelper.getStored(stack);
  }

  public static long getCapacityEU(ItemStack stack) {
    if (!MI_LOADED || stack.isEmpty()) return 0L;
    return MiEnergyHelper.getCapacity(stack);
  }
}
