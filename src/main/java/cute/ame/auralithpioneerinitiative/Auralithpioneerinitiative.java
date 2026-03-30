package cute.ame.auralithpioneerinitiative;

import com.mojang.logging.LogUtils;
import cute.ame.auralithpioneerinitiative.Command.AuralithCommand;
import cute.ame.auralithpioneerinitiative.Registrie.*;
import cute.ame.auralithpioneerinitiative.Ship.Data.ShipDefinitionLoader;
import cute.ame.auralithpioneerinitiative.Ship.Entity.ModEntities;
import cute.ame.auralithpioneerinitiative.Ship.Network.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

@Mod(Auralithpioneerinitiative.MODID)
public class Auralithpioneerinitiative
{
  public static final String MODID  = "auralithpioneerinitiative";
  public static final Logger LOGGER = LogUtils.getLogger();

  public Auralithpioneerinitiative(IEventBus modEventBus, ModContainer modContainer)
  {
    ModWorldgen.FEATURES.register(modEventBus);
    ModWorldgen.registerCodecs();
    ModParticles.PARTICLE_TYPES.register(modEventBus);
    ModItems.ITEMS.register(modEventBus);
    ModBlocks.BLOCKS.register(modEventBus);
    ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
    ModEntities.ENTITIES.register(modEventBus);
    ModEntities.DATA_SERIALIZERS.register(modEventBus);
    modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);

    modEventBus.addListener(Auralithpioneerinitiative::onRegisterPayloadHandlers);
    NeoForge.EVENT_BUS.addListener(Auralithpioneerinitiative::onAddReloadListeners);
    NeoForge.EVENT_BUS.addListener(Auralithpioneerinitiative::onRegisterCommands);
  }

  private static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event)
  {
    final PayloadRegistrar registrar = event.registrar(MODID);

    registrar.playToClient(
        ShipSnapshotPacket.TYPE,
        ShipSnapshotPacket.STREAM_CODEC,
        ShipSnapshotPacket::handle
    );
    registrar.playToClient(
        ShipTransformPacket.TYPE,
        ShipTransformPacket.STREAM_CODEC,
        ShipTransformPacket::handle
    );
    registrar.playToServer(
        FlightInputPacket.TYPE,
        FlightInputPacket.STREAM_CODEC,
        FlightInputPacket::handle
    );

    registrar.playToServer(
        HoloPanelClickPacket.TYPE,
        HoloPanelClickPacket.STREAM_CODEC,
        HoloPanelClickPacket::handle
    );
    registrar.playToClient(
        HoloPanelSyncPacket.TYPE,
        HoloPanelSyncPacket.STREAM_CODEC,
        HoloPanelSyncPacket::handle
    );

    LOGGER.debug("[Auralith] Registered network payloads");
  }

  private static void onAddReloadListeners(AddReloadListenerEvent event)
  {
    event.addListener(ShipDefinitionLoader.INSTANCE);
    LOGGER.debug("[Auralith] Registered ShipDefinitionLoader");
  }

  private static void onRegisterCommands(net.neoforged.neoforge.event.RegisterCommandsEvent event)
  {
    AuralithCommand.register(event.getDispatcher());
    LOGGER.debug("[Auralith] Registered /pioneer commands");
  }
}