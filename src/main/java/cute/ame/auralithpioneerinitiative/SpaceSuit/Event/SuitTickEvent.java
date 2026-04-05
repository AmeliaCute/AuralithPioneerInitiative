package cute.ame.auralithpioneerinitiative.SpaceSuit.Event;

import cute.ame.auralithpioneerinitiative.API.AuralithAPI;
import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Registrie.ModAttachments;
import cute.ame.auralithpioneerinitiative.Registrie.ModDataComponents;
import cute.ame.auralithpioneerinitiative.Registrie.ModItems;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Data.SuitData;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Data.SuitInventoryComponent;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Energy.SuitEnergyBridge;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Item.SuitArmorItem;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Module.O2TankModule;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Network.SuitSyncPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Auralithpioneerinitiative.MODID)
public final class SuitTickEvent {
  private SuitTickEvent() {}

  private static final long O2_GEN_EU_PER_TICK = 32L;
  private static final int O2_GEN_PER_TICK = 2;
  private static final int O2_DRAIN_PER_TICK = 1;
  private static final int REGULATOR_DRAIN_INTERVAL = 10;
  private static final int REBREATHER_DRAIN_INTERVAL = 20;
  private static final int SYNC_INTERVAL = 20;

  @SubscribeEvent
  public static void onPlayerTickPre(PlayerTickEvent.Pre event)
  {
    if (!(event.getEntity() instanceof ServerPlayer player)) return;

    SuitData data = player.getData(ModAttachments.SUIT_DATA);
    long gt = player.level().getGameTime();

    ItemStack helmetStack = player.getInventory().armor.get(3);
    boolean helmetOn = helmetStack.getItem() instanceof SuitArmorItem;
    if (data.isHelmetOn() != helmetOn)
    {
      data.setHelmetOn(helmetOn);
      SuitSyncPacket.sendTo(player);
    }
    if (!helmetOn) return;

    ItemStack chestStack = player.getInventory().armor.get(2);
    boolean chestOn = chestStack.getItem() instanceof SuitArmorItem;

    SuitInventoryComponent helmetInv = getInv(helmetStack, 2);
    SuitInventoryComponent chestInv = chestOn ? getInv(chestStack, 5) : SuitInventoryComponent.ofSize(0);

    boolean hasRegulator = hasModule(helmetInv, ModItems.MODULE_REGULATOR.get());
    boolean hasRebreather = chestOn && hasModule(chestInv, ModItems.MODULE_REBREATHER.get());
    boolean canBreathe = hasRegulator && hasRebreather;

    if (isUnbreathable(player))
    {
      if (!canBreathe)
        player.hurt(player.damageSources().drown(), 1.0f);
      else
      {
        if (gt % REGULATOR_DRAIN_INTERVAL == 0) drainFromChest(chestStack, chestInv, 1);
        if (gt % REBREATHER_DRAIN_INTERVAL == 0) drainFromChest(chestStack, chestInv, 1);

        boolean hasGenerator = hasModule(chestInv, ModItems.MODULE_O2_GENERATOR.get());
        if (hasGenerator)
        {
          long drained = drainBatteriesEU(chestStack, chestInv, O2_GEN_EU_PER_TICK);
          if (drained >= O2_GEN_EU_PER_TICK) refillTanks(chestStack, chestInv, O2_GEN_PER_TICK);
        }

        int o2 = getTotalO2(chestInv);
        if (o2 > 0) drainFromChest(chestStack, chestInv, O2_DRAIN_PER_TICK);
        else player.hurt(player.damageSources().drown(), 1.0f);
      }
    }

    if (data.isFlashlight())
    {
      data.drainEnergy(1);
      if (data.getEnergy() <= 0)
      {
        data.setFlashlight(false);
        player.sendSystemMessage(Component.literal("§7[Suit] Flashlight battery depleted"));
        SuitSyncPacket.sendTo(player);
      }
    }

    if (gt % SYNC_INTERVAL == 0)
    {
      int o2 = getTotalO2(chestInv);
      int o2Max = getMaxO2(chestInv);
      data.setO2(o2);
      data.setO2Max(o2Max);
      data.setEnergy((int) Math.min(getBatteryTotal(chestInv) / 1000L, Integer.MAX_VALUE));
      SuitSyncPacket.sendTo(player);
    }
  }

  private static SuitInventoryComponent getInv(ItemStack stack, int defaultSize)
  {
    return stack.getOrDefault(ModDataComponents.SUIT_INVENTORY.get(), SuitInventoryComponent.ofSize(defaultSize));
  }

  private static boolean hasModule(SuitInventoryComponent inv, net.minecraft.world.item.Item item)
  {
    for (int i = 0; i < inv.size(); i++)
      if (inv.get(i).is(item)) return true;

    return false;
  }

  private static int getTotalO2(SuitInventoryComponent chestInv)
  {
    int total = 0;
    for (int i = 0; i < chestInv.size(); i++)
    {
      ItemStack s = chestInv.get(i);
      if (s.getItem() instanceof O2TankModule) total += O2TankModule.getStored(s);
    }
    return total;
  }

  private static int getMaxO2(SuitInventoryComponent chestInv)
  {
    int total = 0;
    for (int i = 0; i < chestInv.size(); i++)
    {
      ItemStack s = chestInv.get(i);
      if (s.getItem() instanceof O2TankModule) total += O2TankModule.getCapacity(s);
    }

    return total == 0 ? SuitData.O2_MAX : total;
  }

  private static void drainFromChest(ItemStack chestStack, SuitInventoryComponent inv, int amount)
  {
    int remaining = amount;
    SuitInventoryComponent current = inv;

    for (int i = 0; i < current.size() && remaining > 0; i++)
    {
      ItemStack s = current.get(i);
      if (!(s.getItem() instanceof O2TankModule)) continue;

      int drained = O2TankModule.drain(s, remaining);
      remaining -= drained;
      current = current.with(i, s);
    }

    chestStack.set(ModDataComponents.SUIT_INVENTORY.get(), current);
  }

  private static void refillTanks(ItemStack chestStack, SuitInventoryComponent inv, int amount)
  {
    int remaining = amount;
    SuitInventoryComponent current = inv;

    for (int i = 0; i < current.size() && remaining > 0; i++)
    {
      ItemStack s = current.get(i);
      if (!(s.getItem() instanceof O2TankModule)) continue;

      int added = O2TankModule.refill(s, remaining);
      remaining -= added;
      current = current.with(i, s);
    }

    chestStack.set(ModDataComponents.SUIT_INVENTORY.get(), current);
  }

  private static long drainBatteriesEU(ItemStack chestStack, SuitInventoryComponent inv, long maxEu)
  {
    long remaining = maxEu;
    SuitInventoryComponent current = inv;
    boolean mutated = false;

    for (int i = 0; i < current.size() && remaining > 0; i++)
    {
      ItemStack bat = current.get(i);
      if (bat.isEmpty()) continue;

      long drained = SuitEnergyBridge.extractEU(bat, remaining, false);
      if (drained > 0) {
        remaining -= drained;
        current = current.with(i, bat);
        mutated = true;
      }
    }

    if (mutated) chestStack.set(ModDataComponents.SUIT_INVENTORY.get(), current);
    return maxEu - remaining;
  }

  private static long getBatteryTotal(SuitInventoryComponent inv)
  {
    long total = 0L;
    for (int i = 0; i < inv.size(); i++) total += SuitEnergyBridge.getStoredEU(inv.get(i));
    return total;
  }

  private static boolean isUnbreathable(ServerPlayer player)
  {
    return AuralithAPI.getBindingForDimension(player.level().dimension())
        .map(b -> b.isSurfaceDimension() && AuralithAPI.getGravityFor(player.level().dimension()) != 1.0f)
        .orElse(false);
  }
}