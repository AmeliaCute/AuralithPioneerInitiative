package cute.ame.auralithpioneerinitiative.vehicle.Baking;

import cute.ame.auralithpioneerinitiative.vehicle.Entity.BlockVehicleEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class VehicleSnapshot
{
  public record BlockEntry(int relX, int relY, int relZ, int stateId)
  {
    public BlockState resolveState()
    {
      return Block.stateById(stateId);
    }
    public long packPos()
    {
      return VehicleSnapshot.packPos(relX, relY, relZ);
    }
  }

  private final List<BlockEntry> blocks;

  public final int minX, minY, minZ;
  public final int maxX, maxY, maxZ;

  public VehicleSnapshot(List<BlockEntry> blocks)
  {
    this.blocks = List.copyOf(blocks);

    int mnX = Integer.MAX_VALUE, mnY = Integer.MAX_VALUE, mnZ = Integer.MAX_VALUE;
    int mxX = Integer.MIN_VALUE, mxY = Integer.MIN_VALUE, mxZ = Integer.MIN_VALUE;
    for (BlockEntry b : blocks)
    {
      if (b.relX() < mnX) mnX = b.relX(); if (b.relX() > mxX) mxX = b.relX();
      if (b.relY() < mnY) mnY = b.relY(); if (b.relY() > mxY) mxY = b.relY();
      if (b.relZ() < mnZ) mnZ = b.relZ(); if (b.relZ() > mxZ) mxZ = b.relZ();
    }
    this.minX = blocks.isEmpty() ? 0 : mnX; this.maxX = blocks.isEmpty() ? 0 : mxX;
    this.minY = blocks.isEmpty() ? 0 : mnY; this.maxY = blocks.isEmpty() ? 0 : mxY;
    this.minZ = blocks.isEmpty() ? 0 : mnZ; this.maxZ = blocks.isEmpty() ? 0 : mxZ;
  }

  public static VehicleSnapshot fromEntity(BlockVehicleEntity entity)
  {
    List<VehicleSnapshot.BlockEntry> entries = new ArrayList<>();
    for (BlockVehicleEntity.BlockEntry e : entity.getBlockSnapshot())
      entries.add(new VehicleSnapshot.BlockEntry(e.relX(), e.relY(), e.relZ(), e.stateId()));

    return new VehicleSnapshot(entries);
  }

  public List<BlockEntry> blocks() { return blocks; }
  public boolean isEmpty() { return blocks.isEmpty(); }
  public int blockCount() { return blocks.size(); }

  public static long packPos(int x, int y, int z)
  {
    return ((long)(x & 0x3FFFFF) << 42) | ((long)(y & 0x1FFFFF) << 21) | (z & 0x1FFFFF);
  }
}