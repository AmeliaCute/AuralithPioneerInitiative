package cute.ame.auralithpioneerinitiative.Client;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Client.Dimension.SpaceDimensionEffect;
import cute.ame.auralithpioneerinitiative.Space.CelestialSystem;
import cute.ame.auralithpioneerinitiative.Space.Body.CelestialPlanet;
import cute.ame.auralithpioneerinitiative.Space.DimensionPlanetRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

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
    public static void onLevelTick(LevelTickEvent.Post event)
    {
        if (event.getLevel().isClientSide() && CELESTE != null)
            CELESTE.tick();
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event)
    {
        if (CELESTE == null) CELESTE = new CelestialSystem();

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)
        {
            Level level = event.getCamera().getEntity().level();
            String dimensionPath = level.dimension().location().getPath();

            switch (dimensionPath)
            {
                case "space":
                    CELESTE.render(event.getPoseStack(), event.getCamera());
                    break;

                case "overworld":
                case "the_nether":
                case "the_end":
                    CelestialPlanet viewPlanet = DimensionPlanetRegistry.getPlanetForDimension(level.dimension());

                    if (viewPlanet != null)
                    {
                        CELESTE.render(event.getPoseStack(), event.getCamera(), viewPlanet);
                    }
                    else if (dimensionPath.equals("overworld"))
                    {
                        CELESTE.render(event.getPoseStack(), event.getCamera(), CELESTE.getOverworldPlanet());
                    }
                    break;
            }
        }
    }
}