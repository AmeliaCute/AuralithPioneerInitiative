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

  private static final int SYNC_INTERVAL = 20;

  @SubscribeEvent
  public static void onPlayerTickPre(PlayerTickEvent.Pre event)
  {
    if (!(event.getEntity() instanceof ServerPlayer player)) return;

    SuitData data = player.getData(ModAttachments.SUIT_DATA);
    long gt = player.level().getGameTime();

    ItemStack helmetStack = player.getInventory().armor.get(3);
    boolean helmetOn = helmetStack.getItem() instanceof SuitArmorItem;
    if (data.isHelmetOn() != helmetOn) {
      data.setHelmetOn(helmetOn);
      SuitSyncPacket.sendTo(player);
    }

    if (!helmetOn) return;

    SuitInventoryComponent helmetInv = helmetStack.getOrDefault(
        ModDataComponents.SUIT_INVENTORY.get(),
        SuitInventoryComponent.ofSize(2)
    );

    boolean hasGenerator = hasModuleInSlot(helmetInv, ModItems.MODULE_O2_GENERATOR.get());
    if (hasGenerator)
    {
      long drained = drainBatteriesEU(player, O2_GEN_EU_PER_TICK);
      if (drained >= O2_GEN_EU_PER_TICK) refillO2Tank(helmetStack, helmetInv, O2_GEN_PER_TICK);
    }

    if (isUnbreathable(player))
    {
      if (getTotalO2(helmetInv) > 0) drainO2Tank(helmetStack, helmetInv, O2_DRAIN_PER_TICK);
      else player.hurt(player.damageSources().drown(), 1.0f);
    }

    if (data.isFlashlight())
    {
      data.drainEnergy(1);
      if (data.getEnergy() <= 0)
      {
        data.setFlashlight(false);
        player.sendSystemMessage(Component.literal("§7[Flashlight] Battery depleted"));
        SuitSyncPacket.sendTo(player);
      }
    }

    if (gt % SYNC_INTERVAL == 0)
    {
      int o2 = getTotalO2(helmetInv);
      int o2Max = getO2MaxForHud(helmetInv);
      data.setO2(o2);
      long euStored = getBatteryTotal(player);
      data.setEnergy((int) Math.min(euStored / 1000L, Integer.MAX_VALUE));
      SuitSyncPacket.sendTo(player);
    }
  }

  private static boolean hasModuleInSlot(SuitInventoryComponent inv, net.minecraft.world.item.Item moduleItem)
  {
    for (int i = 0; i < inv.size(); i++)
      if (inv.get(i).is(moduleItem)) return true;

    return false;
  }

  private static int getTotalO2(SuitInventoryComponent inv)
  {
    ItemStack tank = findTank(inv);
    return tank.isEmpty() ? 0 : O2TankModule.getStored(tank);
  }

  private static int getO2Capacity(SuitInventoryComponent inv)
  {
    ItemStack tank = findTank(inv);
    return tank.isEmpty() ? 0 : O2TankModule.getCapacity(tank);
  }

  private static void drainO2Tank(ItemStack helmetStack, SuitInventoryComponent inv, int amount)
  {
    mutateTank(helmetStack, inv, -amount);
  }

  private static void refillO2Tank(ItemStack helmetStack, SuitInventoryComponent inv, int amount)
  {
    mutateTank(helmetStack, inv, amount);
  }

  private static void mutateTank(ItemStack helmetStack, SuitInventoryComponent inv, int delta)
  {
    for (int i = 0; i < inv.size(); i++)
    {
      ItemStack s = inv.get(i);
      if (!(s.getItem() instanceof O2TankModule)) continue;

      if (delta < 0) O2TankModule.drain(s, -delta);
      else O2TankModule.refill(s, delta);

      helmetStack.set(ModDataComponents.SUIT_INVENTORY.get(), inv.with(i, s));
      return;
    }
  }

  private static ItemStack findTank(SuitInventoryComponent inv)
  {
    for (int i = 0; i < inv.size(); i++)
      if (inv.get(i).getItem() instanceof O2TankModule) return inv.get(i);

    return ItemStack.EMPTY;
  }

  private static int getO2MaxForHud(SuitInventoryComponent inv)
  {
    ItemStack tank = findTank(inv);
    return tank.isEmpty() ? SuitData.O2_MAX : O2TankModule.getCapacity(tank);
  }

  private static long drainBatteriesEU(ServerPlayer player, long maxEu)
  {
    ItemStack chestStack = player.getInventory().armor.get(2);
    if (!(chestStack.getItem() instanceof SuitArmorItem)) return 0L;

    SuitInventoryComponent chestInv = chestStack.getOrDefault(ModDataComponents.SUIT_INVENTORY.get(), SuitInventoryComponent.ofSize(3));
    long remaining = maxEu;
    boolean mutated = false;

    for (int i = 0; i < chestInv.size() && remaining > 0; i++)
    {
      ItemStack bat = chestInv.get(i);
      if (bat.isEmpty()) continue;

      long drained = SuitEnergyBridge.extractEU(bat, remaining, false);
      if (drained > 0)
      {
        remaining -= drained;
        chestInv = chestInv.with(i, bat);
        mutated = true;
      }
    }

    if (mutated) chestStack.set(ModDataComponents.SUIT_INVENTORY.get(), chestInv);
    return maxEu - remaining;
  }

  private static long getBatteryTotal(ServerPlayer player)
  {
    ItemStack chestStack = player.getInventory().armor.get(2);
    if (!(chestStack.getItem() instanceof SuitArmorItem)) return 0L;

    SuitInventoryComponent inv = chestStack.getOrDefault(ModDataComponents.SUIT_INVENTORY.get(), SuitInventoryComponent.ofSize(3));
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