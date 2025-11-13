package cute.ame.auralithpioneerinitiative.Client;

import com.mojang.blaze3d.systems.RenderSystem;
import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Client.Dimension.SpaceDimensionEffect;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = Auralithpioneerinitiative.MODID, value = Dist.CLIENT)
public class AuralithClient
{
    public static final ResourceKey<Registry<DimensionSpecialEffects>> DIMENSION_EFFECT_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "dim_effect"));


    @SubscribeEvent
    public static void onClientSetup(RegisterDimensionSpecialEffectsEvent event) {
        event.register(
                ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "space"),
                new SpaceDimensionEffect()
        );
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event)
    {
        if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER && event.getCamera().getEntity().level().dimension().location().getPath().equals("space"))
            SpaceDimensionEffect.TEST_CUBE.renderCustomCube(event.getPoseStack(), event.getCamera());




    }
}
