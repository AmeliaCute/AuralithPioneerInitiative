package cute.ame.auralithpioneerinitiative.Client.HUD;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = Auralithpioneerinitiative.MODID, value = Dist.CLIENT)
public final class HoloPanelRenderHook
{
  private HoloPanelRenderHook() {}

  @SubscribeEvent
  public static void onRenderLevelStage(RenderLevelStageEvent event)
  {
    if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

    ClientLevel level = Minecraft.getInstance().level;
    if (level == null) return;

    HoloPanelRenderer.getInstance().renderPanels(
        event.getModelViewMatrix(),
        event.getProjectionMatrix(),
        event.getPartialTick().getGameTimeDeltaPartialTick(true),
        event.getCamera(),
        level
    );
  }
}