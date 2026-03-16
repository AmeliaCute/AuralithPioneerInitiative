package cute.ame.auralithpioneerinitiative.Ship.Shipyard;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public final class ShipyardBlockView implements BlockGetter
{
  private final net.minecraft.server.level.ServerLevel shipyardDim;
  private final int slot;

  public ShipyardBlockView(net.minecraft.server.level.ServerLevel shipyardDim, int slot)
  {
    this.shipyardDim = shipyardDim;
    this.slot = slot;
  }

  @Override
  public BlockState getBlockState(BlockPos shipPos)
  {
    BlockPos syPos = ShipyardAllocator.shipToShipyard(shipPos, slot);
    return shipyardDim.getBlockState(syPos);
  }

  @Override
  public @Nullable BlockEntity getBlockEntity(BlockPos shipPos)
  {
    BlockPos syPos = ShipyardAllocator.shipToShipyard(shipPos, slot);
    return shipyardDim.getBlockEntity(syPos);
  }

  @Override
  public FluidState getFluidState(BlockPos shipPos)
  {
    BlockPos syPos = ShipyardAllocator.shipToShipyard(shipPos, slot);
    return shipyardDim.getFluidState(syPos);
  }

  @Override
  public int getHeight() { return shipyardDim.getHeight(); }

  @Override
  public int getMinBuildHeight() { return shipyardDim.getMinBuildHeight(); }
}