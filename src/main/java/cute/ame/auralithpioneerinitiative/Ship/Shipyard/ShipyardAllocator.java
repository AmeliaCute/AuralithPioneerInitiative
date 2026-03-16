package cute.ame.auralithpioneerinitiative.Ship.Shipyard;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ShipyardAllocator extends SavedData
{
  public static final int SLOT_WIDTH = 2048;
  public static final int SLOT_HEIGHT = 384;
  public static final int SLOT_Y_OFFSET = 64;
  public static final ResourceKey<Level> SHIPYARD_DIM_KEY = ShipyardManager.SHIPYARD_KEY;
  private static final String DATA_KEY = "auralith_shipyard_allocator";

  private int nextSlot = 0;
  private final Map<UUID, Integer> shipSlots = new HashMap<>();

  private ShipyardAllocator() {}

  public static ShipyardAllocator getOrCreate(ServerLevel shipyardLevel)
  {
    return shipyardLevel.getDataStorage().computeIfAbsent(
        new SavedData.Factory<ShipyardAllocator>(
            ShipyardAllocator::new,
            (tag, registries) -> ShipyardAllocator.load(tag)
        ),
        DATA_KEY
    );
  }

  public static ShipyardAllocator getOrCreate(MinecraftServer server)
  {
    return getOrCreate(ShipyardManager.getShipyardLevel(server));
  }

  public int allocate(UUID shipId)
  {
    return shipSlots.computeIfAbsent(shipId, id ->
    {
      int slot = nextSlot++;
      setDirty();
      Auralithpioneerinitiative.LOGGER.debug("[Auralith] Shipyard slot {} alloué pour {}", slot, id);
      return slot;
    });
  }

  public void free(UUID shipId)
  {
    if (shipSlots.remove(shipId) != null)
    {
      setDirty();
      Auralithpioneerinitiative.LOGGER.debug("[Auralith] Shipyard slot libéré pour {}", shipId);
    }
  }

  public void freeSlot(UUID shipId) { free(shipId); }

  public boolean has(UUID shipId)     { return shipSlots.containsKey(shipId); }

  public int getSlot(UUID shipId)
  {
    Integer slot = shipSlots.get(shipId);
    if (slot == null) throw new IllegalStateException("Ship " + shipId + " has no shipyard slot");
    return slot;
  }

  public static BlockPos slotOrigin(int slot)
  {
    return new BlockPos(slot * SLOT_WIDTH, 0, 0);
  }

  public static BlockPos shipToShipyard(BlockPos shipLocalPos, int slot)
  {
    BlockPos origin = slotOrigin(slot);
    return new BlockPos(
        origin.getX() + SLOT_WIDTH / 2 + shipLocalPos.getX(),
        origin.getY() + SLOT_Y_OFFSET  + shipLocalPos.getY(),
        origin.getZ() + SLOT_WIDTH / 2 + shipLocalPos.getZ()
    );
  }

  public static BlockPos shipyardToShip(BlockPos syPos, int slot)
  {
    BlockPos origin = slotOrigin(slot);
    return new BlockPos(
        syPos.getX() - origin.getX() - SLOT_WIDTH / 2,
        syPos.getY() - origin.getY() - SLOT_Y_OFFSET,
        syPos.getZ() - origin.getZ() - SLOT_WIDTH / 2
    );
  }

  public static void forceLoadSlot(ServerLevel shipyardLevel, int slot, boolean load)
  {
    BlockPos origin = slotOrigin(slot);
    int cxMin = origin.getX() >> 4;
    int czMin = origin.getZ() >> 4;
    int cxMax = (origin.getX() + SLOT_WIDTH - 1) >> 4;
    int czMax = (origin.getZ() + SLOT_WIDTH - 1) >> 4;

    for (int cx = cxMin; cx <= cxMax; cx++)
      for (int cz = czMin; cz <= czMax; cz++)
        shipyardLevel.setChunkForced(cx, cz, load);
  }

  public static void setChunksForced(ServerLevel shipyardLevel, int slot, AABB localBounds, boolean load)
  {
    if (localBounds == null)
    {
      forceLoadSlot(shipyardLevel, slot, load);
      return;
    }

    BlockPos origin = slotOrigin(slot);
    int cxMin = (origin.getX() + SLOT_WIDTH / 2 + (int) Math.floor(localBounds.minX) - 1) >> 4;
    int czMin = (origin.getZ() + SLOT_WIDTH / 2 + (int) Math.floor(localBounds.minZ) - 1) >> 4;
    int cxMax = (origin.getX() + SLOT_WIDTH / 2 + (int) Math.ceil (localBounds.maxX) + 1) >> 4;
    int czMax = (origin.getZ() + SLOT_WIDTH / 2 + (int) Math.ceil (localBounds.maxZ) + 1) >> 4;

    for (int cx = cxMin; cx <= cxMax; cx++)
      for (int cz = czMin; cz <= czMax; cz++)
        shipyardLevel.setChunkForced(cx, cz, load);
  }

  private static ShipyardAllocator load(CompoundTag tag)
  {
    ShipyardAllocator alloc = new ShipyardAllocator();
    alloc.nextSlot = tag.getInt("nextSlot");
    CompoundTag slots = tag.getCompound("slots");
    for (String key : slots.getAllKeys())
      alloc.shipSlots.put(UUID.fromString(key), slots.getInt(key));
    return alloc;
  }

  @Override
  public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries)
  {
    tag.putInt("nextSlot", nextSlot);
    CompoundTag slots = new CompoundTag();
    shipSlots.forEach((uuid, slot) -> slots.putInt(uuid.toString(), slot));
    tag.put("slots", slots);
    return tag;
  }
}