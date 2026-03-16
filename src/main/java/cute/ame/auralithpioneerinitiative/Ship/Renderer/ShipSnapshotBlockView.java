package cute.ame.auralithpioneerinitiative.Ship.Renderer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class ShipSnapshotBlockView implements BlockGetter
{
  private final Map<Long, BlockState> blocks;

  private ShipSnapshotBlockView(Map<Long, BlockState> blocks)
  {
    this.blocks = blocks;
  }

  public static ShipSnapshotBlockView from(java.util.List<cute.ame.auralithpioneerinitiative.Ship.Network.ShipSnapshotPacket.BlockEntry> entries)
  {
    Map<Long, BlockState> map = new HashMap<>(entries.size() * 2);
    for (var e : entries) map.put(pack(e.relX(), e.relY(), e.relZ()), e.resolveState());
    return new ShipSnapshotBlockView(map);
  }

  @Override
  public BlockState getBlockState(BlockPos pos)
  {
    BlockState state = blocks.get(pack(pos.getX(), pos.getY(), pos.getZ()));
    return state != null ? state : Blocks.AIR.defaultBlockState();
  }

  @Override
  public @Nullable BlockEntity getBlockEntity(BlockPos pos) { return null; }

  @Override
  public FluidState getFluidState(BlockPos pos) { return Fluids.EMPTY.defaultFluidState(); }

  @Override
  public int getHeight() { return 384; }

  @Override
  public int getMinBuildHeight() { return -64; }

  public boolean isEmpty() { return blocks.isEmpty(); }

  private static long pack(int x, int y, int z)
  {
    return ((long)(x + 4096) & 0x1FFFL)
        | (((long)(y + 4096) & 0x1FFFL) << 13)
        | (((long)(z + 4096) & 0x1FFFL) << 26);
  }
}