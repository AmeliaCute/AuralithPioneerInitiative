package cute.ame.auralithpioneerinitiative.Ship.Shipyard;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Ship.Network.ShipSnapshotPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;

public final class ShipyardManager
{
  private ShipyardManager() {}

  public static final ResourceLocation SHIPYARD_DIM_ID =
      ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "shipyard");

  public static final ResourceKey<Level> SHIPYARD_KEY =
      ResourceKey.create(Registries.DIMENSION, SHIPYARD_DIM_ID);

  public static ServerLevel getShipyardLevel(MinecraftServer server)
  {
    ServerLevel level = server.getLevel(SHIPYARD_KEY);
    if (level == null) throw new IllegalStateException("[Auralith] Shipyard dimension '" + SHIPYARD_DIM_ID + "' not loaded.");
    return level;
  }

  public static void writeSnapshotToShipyard(MinecraftServer server, UUID shipId, List<ShipSnapshotPacket.BlockEntry> snapshot)
  {
    if (snapshot.isEmpty()) return;

    ServerLevel sy = getShipyardLevel(server);
    ShipyardAllocator alloc = ShipyardAllocator.getOrCreate(sy);
    int slot = alloc.allocate(shipId);

    AABB localBounds = boundsFromSnapshot(snapshot);
    ShipyardAllocator.setChunksForced(sy, slot, localBounds, true);

    for (ShipSnapshotPacket.BlockEntry entry : snapshot)
    {
      BlockState state = entry.resolveState();
      if (state.isAir()) continue;
      BlockPos syPos = ShipyardAllocator.shipToShipyard(new BlockPos(entry.relX(), entry.relY(), entry.relZ()), slot);
      sy.setBlock(syPos, state, 18);
    }

    BlockPos center = ShipyardAllocator.shipToShipyard(BlockPos.ZERO, slot);
    Auralithpioneerinitiative.LOGGER.info("[Auralith] {} blocs écrits dans le slot {} pour {} (centre shipyard: {},{},{})", snapshot.size(), slot, shipId, center.getX(), center.getY(), center.getZ());
  }

  public static void removeFromShipyard(
      MinecraftServer server,
      UUID shipId,
      List<ShipSnapshotPacket.BlockEntry> snapshot)
  {
    if (server == null) return;

    ServerLevel sy    = getShipyardLevel(server);
    ShipyardAllocator alloc = ShipyardAllocator.getOrCreate(sy);
    if (!alloc.has(shipId)) return;

    int slot = alloc.getSlot(shipId);

    for (ShipSnapshotPacket.BlockEntry entry : snapshot)
    {
      BlockPos syPos = ShipyardAllocator.shipToShipyard(
          new BlockPos(entry.relX(), entry.relY(), entry.relZ()), slot);
      sy.setBlock(syPos, Blocks.AIR.defaultBlockState(), 18);
    }

    AABB localBounds = boundsFromSnapshot(snapshot);
    ShipyardAllocator.setChunksForced(sy, slot, localBounds, false);
    alloc.free(shipId);

    Auralithpioneerinitiative.LOGGER.debug("[Auralith] Slot {} nettoyé et libéré pour {}", slot, shipId);
  }

  public static AABB boundsFromSnapshot(List<ShipSnapshotPacket.BlockEntry> snapshot)
  {
    if (snapshot.isEmpty()) return new AABB(-1, -1, -1, 1, 1, 1);
    int x0 = Integer.MAX_VALUE, y0 = Integer.MAX_VALUE, z0 = Integer.MAX_VALUE;
    int x1 = Integer.MIN_VALUE, y1 = Integer.MIN_VALUE, z1 = Integer.MIN_VALUE;
    for (var e : snapshot)
    {
      if (e.relX() < x0) x0 = e.relX(); if (e.relX() + 1 > x1) x1 = e.relX() + 1;
      if (e.relY() < y0) y0 = e.relY(); if (e.relY() + 1 > y1) y1 = e.relY() + 1;
      if (e.relZ() < z0) z0 = e.relZ(); if (e.relZ() + 1 > z1) z1 = e.relZ() + 1;
    }
    return new AABB(x0, y0, z0, x1, y1, z1);
  }
}