package cute.ame.auralithpioneerinitiative.Helper;

import aztech.modern_industrialization.api.energy.EnergyApi;
import dev.technici4n.grandpower.api.ILongEnergyStorage;
import net.minecraft.world.item.ItemStack;

public final class MiEnergyHelper
{
  private MiEnergyHelper() {}

  public static long extract(ItemStack stack, long maxEu, boolean simulate)
  {
    ILongEnergyStorage storage = stack.getCapability(EnergyApi.ITEM);
    if (storage == null || !storage.canExtract()) return 0L;
    return storage.extract(maxEu, simulate);
  }

  public static long getStored(ItemStack stack)
  {
    ILongEnergyStorage storage = stack.getCapability(EnergyApi.ITEM);
    return storage != null ? storage.getAmount() : 0L;
  }

  public static long getCapacity(ItemStack stack)
  {
    ILongEnergyStorage storage = stack.getCapability(EnergyApi.ITEM);
    return storage != null ? storage.getCapacity() : 0L;
  }
}