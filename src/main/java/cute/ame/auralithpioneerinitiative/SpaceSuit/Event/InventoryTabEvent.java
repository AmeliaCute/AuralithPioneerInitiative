package cute.ame.auralithpioneerinitiative.SpaceSuit.Event;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.SpaceSuit.GUI.SuitTabButton;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = Auralithpioneerinitiative.MODID, value = Dist.CLIENT)
public final class InventoryTabEvent
{
  private InventoryTabEvent() {}

  @SubscribeEvent
  public static void onInventoryInit(ScreenEvent.Init.Post event)
  {
    if (!(event.getScreen() instanceof InventoryScreen screen)) return;
    event.addListener(new SuitTabButton(
        screen.getGuiLeft() + 176,
        screen.getGuiTop() - 1
    ));
  }
}