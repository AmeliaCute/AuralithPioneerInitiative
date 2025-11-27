package cute.ame.auralithpioneerinitiative.Client;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Client.Dimension.SpaceDimensionEffect;
import cute.ame.auralithpioneerinitiative.Space.CelestialSystem;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = Auralithpioneerinitiative.MODID, value = Dist.CLIENT)
public class AuralithClient
{
    @SubscribeEvent
    public static void onDimensionEffectRegister(RegisterDimensionSpecialEffectsEvent event)
    {
        event.register(ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "space"), new SpaceDimensionEffect());
    }

    public static CelestialSystem CELESTE;

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event)
    {
        if(CELESTE == null) CELESTE = new CelestialSystem();
        if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES && event.getCamera().getEntity().level().dimension().location().getPath().equals("space"))
            CELESTE.render(event.getPoseStack(), event.getCamera());
    }
}
