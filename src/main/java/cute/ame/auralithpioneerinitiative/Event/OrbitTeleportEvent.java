package cute.ame.auralithpioneerinitiative.Event;

import cute.ame.auralithpioneerinitiative.API.AuralithAPI;
import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.SkyPlanet.Data.PlanetDefinition;
import cute.ame.auralithpioneerinitiative.SkyPlanet.Data.SolarSystemDefinition;
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

    private static final double MOON_ORBIT_EXIT_RADIUS = 2_000.0;
    private static final double MOON_ORBIT_EXIT_RADIUS_SQ = MOON_ORBIT_EXIT_RADIUS * MOON_ORBIT_EXIT_RADIUS;

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
        int  cooldown = COOLDOWNS.getOrDefault(uuid, 0);
        if (cooldown > 0) { COOLDOWNS.put(uuid, cooldown - 1); return; }

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

        double px = player.getX(), pz = player.getZ();
        double distSq = px * px + pz * pz;

        boolean isMoonOrbit = binding.planetId() != null && system.findParent(binding.planetId()).isPresent();

        if (isMoonOrbit) handleMoonOrbitTick(player, server, system, binding, uuid, distSq);
        else handlePlanetOrbitTick(player, server, system, binding, uuid, distSq);
    }

    private static void handlePlanetOrbitTick(ServerPlayer player, MinecraftServer server, SolarSystemDefinition system, AuralithAPI.DimensionBinding binding, UUID uuid, double distSq)
    {
      long tick = player.level().getGameTime();

      if (distSq >= ORBIT_RADIUS_SQ)
      {
        exitToSpace(player, server, system, uuid);
        return;
      }

      if (binding.planetId() == null) return;
      PlanetDefinition planet = system.findById(binding.planetId()).orElse(null);
      if (planet == null) return;

      double landingThreshold = (planet.size() / ORBIT_PLANET_SIZE) * ORBIT_RADIUS;
      if (distSq <= landingThreshold * landingThreshold)
      {
        landOnPlanet(player, server, planet, uuid);
        return;
      }

      for (PlanetDefinition moon : planet.moons())
      {
        if (moon.orbitDimension().isEmpty()) continue;

        double angle = moon.orbit().computeAngle(tick, 0f);
        double radius = moon.orbit().computeCurrentRadius(angle);
        float[] moonPos = moon.orbit().compute3DPosition(angle, radius, (float) MOON_WORLD_SCALE);

        double dx = player.getX() - moonPos[0];
        double dz = player.getZ() - moonPos[2];
        if (dx*dx + dz*dz > MOON_APPROACH_THRESHOLD_SQ) continue;

        enterMoonOrbit(player, server, moon, moonPos, uuid);
        return;
      }
    }

    private static void handleMoonOrbitTick(ServerPlayer player, MinecraftServer server, SolarSystemDefinition system, AuralithAPI.DimensionBinding binding, UUID uuid, double distSq)
    {
      if (binding.planetId() == null) return;

      PlanetDefinition moon = system.findById(binding.planetId()).orElse(null);
      PlanetDefinition parent = system.findParent(binding.planetId()).orElse(null);
      if (moon == null || parent == null) return;

      double landingThreshold = (moon.size() / ORBIT_PLANET_SIZE) * MOON_ORBIT_EXIT_RADIUS;
      if (distSq <= landingThreshold * landingThreshold)
      {
        landOnPlanet(player, server, moon, uuid);
        return;
      }

      if (distSq >= MOON_ORBIT_EXIT_RADIUS_SQ) returnToParentOrbit(player, server, moon, parent, uuid);
    }

    private static void exitToSpace(ServerPlayer player, MinecraftServer server,SolarSystemDefinition system, UUID uuid)
    {
      system.spaceDimension().ifPresent(spaceDimId ->
      {
        ServerLevel spaceLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, spaceDimId));
        if (spaceLevel == null) { Auralithpioneerinitiative.LOGGER.warn("[Auralith] Space dim '{}' not found.", spaceDimId); return; }
        COOLDOWNS.put(uuid, COOLDOWN_TICKS);
        player.teleportTo(spaceLevel, 0.5, 64.0, 0.5, Set.of(), player.getYRot(), player.getXRot());
        Auralithpioneerinitiative.LOGGER.debug("[Auralith] {} → space", player.getScoreboardName());
      });
    }

    private static void landOnPlanet(ServerPlayer player, MinecraftServer server,PlanetDefinition planet, UUID uuid)
    {
      planet.dimension().ifPresent(surfaceDimId ->
      {
        ServerLevel surfaceLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, surfaceDimId));
        if (surfaceLevel == null) { Auralithpioneerinitiative.LOGGER.warn("[Auralith] Surface dim '{}' not found.", surfaceDimId); return; }
        BlockPos top   = surfaceLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.ZERO);
        double   landY = Math.max(top.getY() + 1, surfaceLevel.getMinBuildHeight() + 1);
        COOLDOWNS.put(uuid, COOLDOWN_TICKS);
        player.teleportTo(surfaceLevel, 0.5, landY, 0.5, Set.of(), player.getYRot(), player.getXRot());
        Auralithpioneerinitiative.LOGGER.debug("[Auralith] {} landed on '{}'", player.getScoreboardName(), surfaceDimId);
      });
    }

    private static void enterMoonOrbit(ServerPlayer player, MinecraftServer server, PlanetDefinition moon, float[] moonPosInParentOrbit, UUID uuid)
    {
      ResourceLocation moonOrbitId = moon.orbitDimension().get();
      ServerLevel moonOrbitLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, moonOrbitId));
      if (moonOrbitLevel == null)
      {
        Auralithpioneerinitiative.LOGGER.warn("[Auralith] Moon orbit dim '{}' not found.", moonOrbitId);
        return;
      }

      double mLen = Math.sqrt(moonPosInParentOrbit[0] * moonPosInParentOrbit[0] + moonPosInParentOrbit[2] * moonPosInParentOrbit[2]);

      double spawnX, spawnZ;
      if (mLen > 0.01)
      {
        double nx = moonPosInParentOrbit[0] / mLen;
        double nz = moonPosInParentOrbit[2] / mLen;
        double dist = MOON_ORBIT_EXIT_RADIUS * 0.5;
        spawnX = nx * dist;
        spawnZ = nz * dist;
      }
      else
      {
        spawnX = 0;
        spawnZ = MOON_ORBIT_EXIT_RADIUS * 0.5;
      }

      COOLDOWNS.put(uuid, COOLDOWN_TICKS);
      player.teleportTo(moonOrbitLevel, spawnX, SPAWN_Y, spawnZ, Set.of(), player.getYRot(), player.getXRot());
      Auralithpioneerinitiative.LOGGER.debug("[Auralith] {} → moon orbit '{}'", player.getScoreboardName(), moonOrbitId);
    }

    private static void returnToParentOrbit(ServerPlayer player, MinecraftServer server, PlanetDefinition moon, PlanetDefinition parent, UUID uuid)
    {
      if (parent.orbitDimension().isEmpty()) return;

      ResourceLocation parentOrbitId = parent.orbitDimension().get();
      ServerLevel parentOrbitLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, parentOrbitId));
      if (parentOrbitLevel == null)
      {
        Auralithpioneerinitiative.LOGGER.warn("[Auralith] Parent orbit dim '{}' not found.", parentOrbitId);
        return;
      }

      long tick = player.level().getGameTime();
      double angle = moon.orbit().computeAngle(tick, 0f);
      double radius = moon.orbit().computeCurrentRadius(angle);
      float[] moonPos = moon.orbit().compute3DPosition(angle, radius, (float) MOON_WORLD_SCALE);

      double mLen = Math.sqrt(moonPos[0]*moonPos[0] + moonPos[2]*moonPos[2]);
      double spawnX = moonPos[0], spawnZ = moonPos[2];
      if (mLen > 0.01)
      {
        double offset = MOON_APPROACH_THRESHOLD * 1.5;
        spawnX += (moonPos[0] / mLen) * offset;
        spawnZ += (moonPos[2] / mLen) * offset;
      }

      COOLDOWNS.put(uuid, COOLDOWN_TICKS);
      player.teleportTo(parentOrbitLevel, spawnX, SPAWN_Y, spawnZ, Set.of(), player.getYRot(), player.getXRot());
      Auralithpioneerinitiative.LOGGER.debug("[Auralith] {} ← moon '{}' → parent orbit '{}'", player.getScoreboardName(), moon.id(), parentOrbitId);
    }
}
