package cute.ame.auralithpioneerinitiative.Event;

import cute.ame.auralithpioneerinitiative.API.AuralithAPI;
import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Data.PlanetDefinition;
import cute.ame.auralithpioneerinitiative.Data.SolarSystemDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = Auralithpioneerinitiative.MODID)
public class OrbitTeleportEvent
{
  private static final double ORBIT_RADIUS = 10_000.0;
  private static final double ORBIT_RADIUS_SQ = ORBIT_RADIUS * ORBIT_RADIUS;
  private static final double ORBIT_PLANET_SIZE = 60.0;
  private static final double MOON_WORLD_SCALE = ORBIT_RADIUS * 0.2;

  private static final double MOON_APPROACH_THRESHOLD = 150.0;
  private static final double MOON_APPROACH_THRESHOLD_SQ = MOON_APPROACH_THRESHOLD * MOON_APPROACH_THRESHOLD;
  private static final int COOLDOWN_TICKS = 100;

  private static final double SPAWN_Y = 256.0;
  private static final Map<UUID, Integer> COOLDOWNS = new HashMap<>();

  @SubscribeEvent
  public static void onPlayerTick(PlayerTickEvent.Post event)
  {
      if (!(event.getEntity() instanceof ServerPlayer player)) return;

      UUID uuid = player.getUUID();
      int cooldown = COOLDOWNS.getOrDefault(uuid, 0);
      if (cooldown > 0)
      {
        COOLDOWNS.put(uuid, cooldown - 1);
        return;
      }

      ResourceKey<Level> dim = player.level().dimension();
      Optional<AuralithAPI.DimensionBinding> bindingOpt = AuralithAPI.getBindingForDimension(dim);
      if (bindingOpt.isEmpty()) return;

      AuralithAPI.DimensionBinding binding = bindingOpt.get();
      if (binding.type() != AuralithAPI.BindingType.ORBIT) return;

      MinecraftServer server = player.getServer();
      if (server == null) return;

      Optional<SolarSystemDefinition> systemOpt = AuralithAPI.getSolarSystem(binding.systemId());
      if (systemOpt.isEmpty()) return;
      SolarSystemDefinition system = systemOpt.get();

      double x = player.getX();
      double z = player.getZ();
      double distSq = x * x + z * z;

      if (distSq >= ORBIT_RADIUS_SQ)
      {
        handleSpaceExit(player, server, system, uuid);
        return;
      }

      if (binding.planetId() != null)
      {
        system.planets().stream().filter(p -> p.id().equals(binding.planetId())).findFirst().ifPresent(planet ->
        {
          double landingThreshold = (planet.size() / ORBIT_PLANET_SIZE) * ORBIT_RADIUS;
          double landingThresholdSq = landingThreshold * landingThreshold;

          if (distSq <= landingThresholdSq)
          {
            handlePlanetApproach(player, server, planet, uuid);
            return;
          }

          handleMoonProximity(player, server, planet, uuid, distSq);
        });
      }
  }

  private static void handleSpaceExit(ServerPlayer player, MinecraftServer server, SolarSystemDefinition system, UUID uuid)
  {
    system.spaceDimension().ifPresent(spaceDimId ->
    {
      ResourceKey<Level> spaceKey = ResourceKey.create(Registries.DIMENSION, spaceDimId);
      ServerLevel spaceLevel = server.getLevel(spaceKey);
      if (spaceLevel == null)
      {
        Auralithpioneerinitiative.LOGGER.warn("[Auralith] Space dimension '{}' not found for teleport.", spaceDimId);
        return;
      }

      COOLDOWNS.put(uuid, COOLDOWN_TICKS);
      player.teleportTo(spaceLevel, 0.5, 64.0, 0.5, Set.of(), player.getYRot(), player.getXRot());
      Auralithpioneerinitiative.LOGGER.debug("[Auralith] {} left orbit → space '{}'", player.getScoreboardName(), spaceDimId);
    });
  }

  private static void handlePlanetApproach(ServerPlayer player, MinecraftServer server, PlanetDefinition planet, UUID uuid)
  {
    planet.dimension().ifPresent(surfaceDimId ->
    {
      ResourceKey<Level> surfaceKey = ResourceKey.create(Registries.DIMENSION, surfaceDimId);
      ServerLevel surfaceLevel = server.getLevel(surfaceKey);
      if (surfaceLevel == null)
      {
        Auralithpioneerinitiative.LOGGER.debug("[Auralith] Surface dimension '{}' not found for teleport.", surfaceDimId);
        return;
      }

      BlockPos top = surfaceLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.ZERO);
      double landY = Math.max(top.getY() + 1, surfaceLevel.getMinBuildHeight() + 1);

      COOLDOWNS.put(uuid, COOLDOWN_TICKS);
      player.teleportTo(surfaceLevel, 0.5, landY, 0.5, Set.of(), player.getYRot(), player.getXRot());
      Auralithpioneerinitiative.LOGGER.debug("[Auralith] {} landed on '{}' → surface '{}'", player.getScoreboardName(), planet.id(), surfaceDimId);
    });
  }

  private static void handleMoonProximity(ServerPlayer player, MinecraftServer server, PlanetDefinition parentPlanet, UUID uuid, double playerDistSqFromCenter)
  {
    if (parentPlanet.moons().isEmpty()) return;
    long tick = player.level().getGameTime();

    for (PlanetDefinition moon : parentPlanet.moons())
    {
      if (moon.orbitDimension().isEmpty()) continue;

      double angle = moon.orbit().computeAngle(tick, 0f);
      double radius = moon.orbit().computeCurrentRadius(angle);
      float[] moonPos = moon.orbit().compute3DPosition(angle, radius, (float) MOON_WORLD_SCALE);

      double dx = player.getX() - moonPos[0];
      double dz = player.getZ() - moonPos[2];
      double moonDistSq = dx * dx + dz * dz;
      if (moonDistSq > MOON_APPROACH_THRESHOLD_SQ) continue;

      ResourceLocation moonOrbitId  = moon.orbitDimension().get();
      ResourceKey<Level> moonOrbitKey = ResourceKey.create(Registries.DIMENSION, moonOrbitId);
      ServerLevel moonOrbitLevel = server.getLevel(moonOrbitKey);
      if (moonOrbitLevel == null)
      {
        Auralithpioneerinitiative.LOGGER.debug("[Auralith] Moon orbit dimension '{}' not found.", moonOrbitId);
        continue;
      }

      COOLDOWNS.put(uuid, COOLDOWN_TICKS);
      player.teleportTo(moonOrbitLevel, 0.5, SPAWN_Y, 0.5, Set.of(), player.getYRot(), player.getXRot());
      Auralithpioneerinitiative.LOGGER.debug("[Auralith] {} entered orbit of moon '{}' → '{}'", player.getScoreboardName(), moon.id(), moonOrbitId);
      return;
    }
  }
}
