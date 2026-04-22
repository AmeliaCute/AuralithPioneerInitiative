package cute.ame.auralithpioneerinitiative.vehicle.Entity;

import cute.ame.auralithpioneerinitiative.vehicle.Network.VehicleSnapshotPacket;
import cute.ame.auralithpioneerinitiative.vehicle.Network.VehicleTransformPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class BlockVehicleEntity extends AbstractVehicleEntity
{
  public record BlockEntry(int relX, int relY, int relZ, int stateId) {}
  private final List<BlockEntry> blockSnapshot = new ArrayList<>();
  private @Nullable AABB cachedLocalBounds;

  private int staticBroadcastTimer = 0;
  private static final int STATIC_BROADCAST_INTERVAL = 20;

  protected BlockVehicleEntity(EntityType<?> type, Level level) {
    super(type, level);
  }

  public List<BlockEntry> getBlockSnapshot() {
    return blockSnapshot;
  }

  public void setBlockSnapshot(List<BlockEntry> entries)
  {
    blockSnapshot.clear();
    blockSnapshot.addAll(entries);
    cachedLocalBounds = null;
    setBoundsDirtyFlag();
  }

  private void setBoundsDirtyFlag()
  {
    setPos(getX(), getY(), getZ());
  }

  @Override
  public AABB getLocalBounds()
  {
    if (cachedLocalBounds != null) return cachedLocalBounds;
    if (blockSnapshot.isEmpty())
    {
      cachedLocalBounds = new AABB(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5);
      return cachedLocalBounds;
    }

    int mnX = Integer.MAX_VALUE, mnY = Integer.MAX_VALUE, mnZ = Integer.MAX_VALUE;
    int mxX = Integer.MIN_VALUE, mxY = Integer.MIN_VALUE, mxZ = Integer.MIN_VALUE;
    for (BlockEntry b : blockSnapshot)
    {
      if (b.relX() < mnX) mnX = b.relX(); if (b.relX() > mxX) mxX = b.relX();
      if (b.relY() < mnY) mnY = b.relY(); if (b.relY() > mxY) mxY = b.relY();
      if (b.relZ() < mnZ) mnZ = b.relZ(); if (b.relZ() > mxZ) mxZ = b.relZ();
    }

    cachedLocalBounds = new AABB(mnX - 0.5, mnY - 0.5, mnZ - 0.5, mxX + 0.5, mxY + 0.5, mxZ + 0.5);
    return cachedLocalBounds;
  }

  @Override
  public void startSeenByPlayer(ServerPlayer player)
  {
    super.startSeenByPlayer(player);
    if (!blockSnapshot.isEmpty()) PacketDistributor.sendToPlayer(player, VehicleSnapshotPacket.from(getUUID(), blockSnapshot));
    PacketDistributor.sendToPlayer(player, VehicleTransformPacket.from(this));
  }

  protected void broadcastTransform()
  {
    PacketDistributor.sendToPlayersTrackingEntityAndSelf(this, VehicleTransformPacket.from(this));
  }

  @Override
  public void serverTick()
  {
    if (isMoving())
    {
      broadcastTransform();
      staticBroadcastTimer = 0;
    }
    else if (++staticBroadcastTimer >= STATIC_BROADCAST_INTERVAL)
    {
      broadcastTransform();
      staticBroadcastTimer = 0;
    }
  }

  protected boolean isMoving() {
    return getDeltaMovement().lengthSqr() > 1e-6;
  }

  @Override
  protected void readAdditionalSaveData(CompoundTag tag)
  {
    super.readAdditionalSaveData(tag);

    blockSnapshot.clear();
    cachedLocalBounds = null;
    if (tag.contains("BlockSnapshot"))
    {
      ListTag list = tag.getList("BlockSnapshot", 10);
      for (int i = 0; i < list.size(); i++)
      {
        CompoundTag entry = list.getCompound(i);
        blockSnapshot.add(new BlockEntry(entry.getInt("x"), entry.getInt("y"), entry.getInt("z"), entry.getInt("s")));
      }
    }
  }

  @Override
  protected void addAdditionalSaveData(CompoundTag tag)
  {
    super.addAdditionalSaveData(tag);

    ListTag list = new ListTag();
    for (BlockEntry b : blockSnapshot)
    {
      CompoundTag entry = new CompoundTag();
      entry.putInt("x", b.relX());
      entry.putInt("y", b.relY());
      entry.putInt("z", b.relZ());
      entry.putInt("s", b.stateId());
      list.add(entry);
    }
    tag.put("BlockSnapshot", list);
  }
}
