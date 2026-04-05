package cute.ame.auralithpioneerinitiative.SpaceSuit.Event;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.SpaceSuit.GUI.SuitEquipmentScreen;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Input.SuitKeybinds;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Network.FlashlightTogglePacket;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Network.OpenSuitMenuPacket;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Auralithpioneerinitiative.MODID, value = Dist.CLIENT)
public final class SuitInputEvent {
  private SuitInputEvent() {}

  @SubscribeEvent
  public static void onClientTick(ClientTickEvent.Post event) {
    Minecraft mc = Minecraft.getInstance();
    if (mc.player == null || mc.screen != null) return;

    while (SuitKeybinds.SUIT_MENU.consumeClick())
      PacketDistributor.sendToServer(new OpenSuitMenuPacket());

    while (SuitKeybinds.FLASHLIGHT.consumeClick())
      PacketDistributor.sendToServer(new FlashlightTogglePacket());
  }
}