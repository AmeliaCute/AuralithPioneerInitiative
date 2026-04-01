package cute.ame.auralithpioneerinitiative.SpaceSuit.HUD;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

@EventBusSubscriber(modid = Auralithpioneerinitiative.MODID, value = Dist.CLIENT)
public class SpaceSuitHudRenderer
{

  @SubscribeEvent
  public static void onPreRenderGuiLayer(RenderGuiLayerEvent.Pre event)
  {
    SuitHudOverlay.onPreRenderLayer(event);
  }
}
