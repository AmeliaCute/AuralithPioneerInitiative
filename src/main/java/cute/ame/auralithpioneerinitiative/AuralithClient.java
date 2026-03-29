package cute.ame.auralithpioneerinitiative;

import cute.ame.auralithpioneerinitiative.SkyPlanet.Dimension.SpaceDimensionEffect;
import cute.ame.auralithpioneerinitiative.HoloPanel.HoloPanelClientState;
import cute.ame.auralithpioneerinitiative.HoloPanel.HoloPanelRegistry;
import cute.ame.auralithpioneerinitiative.HoloPanel.Panel.*;
import cute.ame.auralithpioneerinitiative.Planet.Arid.Particle.AridGroundScatterParticle;
import cute.ame.auralithpioneerinitiative.SkyPlanet.Texture.PlanetTextureGenerator;
import cute.ame.auralithpioneerinitiative.SkyPlanet.Loader.SolarSystemLoader;
import cute.ame.auralithpioneerinitiative.Registrie.ModParticles;
import cute.ame.auralithpioneerinitiative.Ship.Entity.ModEntities;
import cute.ame.auralithpioneerinitiative.Ship.Input.FlightKeys;
import cute.ame.auralithpioneerinitiative.Ship.Renderer.ShipClientCache;
import cute.ame.auralithpioneerinitiative.Ship.Renderer.ShipEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.level.LevelEvent;

@EventBusSubscriber(modid = Auralithpioneerinitiative.MODID, value = Dist.CLIENT)
public final class AuralithClient
{
  private AuralithClient() {}

  @SubscribeEvent
  public static void onClientSetup(FMLClientSetupEvent event)
  {
    event.enqueueWork(() ->
    {
      HoloPanelRegistry reg = HoloPanelRegistry.getInstance();
      reg.register(new ContactsPanel());
      reg.register(new CargoModulesPanel());
      reg.register(new LandingPadPanel());
      reg.register(new StationServicesPanel());
      reg.register(new EquipmentKioskPanel());

      Auralithpioneerinitiative.LOGGER.info("[Auralith] Registered {} HoloPanel types", reg.all().size());
    });
  }

  @SubscribeEvent
  public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event)
  {
    event.registerReloadListener(SolarSystemLoader.INSTANCE);
    event.registerReloadListener((prepBarrier, resourceManager, prepProfiler, applyProfiler, prepExec, applyExec) -> prepBarrier.wait(null).thenRunAsync(PlanetTextureGenerator::invalidateAll, applyExec));
  }

  @SubscribeEvent
  public static void onRegisterDimensionEffects(RegisterDimensionSpecialEffectsEvent event)
  {
    event.register(ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "space"), new SpaceDimensionEffect());
  }

  @SubscribeEvent
  public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event)
  {
    event.registerEntityRenderer(ModEntities.SHIP.get(), ShipEntityRenderer::new);
  }

  @SubscribeEvent
  public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event)
  {
    for (var key : FlightKeys.ALL) event.register(key);
  }

  @SubscribeEvent
  public static void onLevelUnload(LevelEvent.Unload event)
  {
    if (event.getLevel().isClientSide())
    {
      ShipClientCache.evictAll();
      HoloPanelRegistry.getInstance().destroyAllFBOs();
      HoloPanelClientState.getInstance().clear();
    }
  }

  @SubscribeEvent
  public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event)
  {
    event.registerSprite(
        ModParticles.ARID_GROUND_SCATTER.get(),
        new AridGroundScatterParticle.Provider()
    );
    Auralithpioneerinitiative.LOGGER.debug("[Auralith] Registered particle providers");
  }
}