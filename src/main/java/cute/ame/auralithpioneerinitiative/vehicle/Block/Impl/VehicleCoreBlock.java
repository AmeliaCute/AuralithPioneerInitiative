package cute.ame.auralithpioneerinitiative.vehicle.Block.Impl;

import cute.ame.auralithpioneerinitiative.vehicle.Assembly.VehicleCoreBlockEntity;
import cute.ame.auralithpioneerinitiative.vehicle.Register.ModVehicleBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class VehicleCoreBlock extends BaseEntityBlock
{
  public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

  public VehicleCoreBlock(BlockBehaviour.Properties props)
  {
    super(props);
    registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder)
  {
    builder.add(FACING);
  }

  @Override
  public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx)
  {
    return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
  }

  @Override
  public RenderShape getRenderShape(BlockState state)
  {
    return RenderShape.MODEL;
  }

  @Override
  protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec()
  {
    return simpleCodec(VehicleCoreBlock::new);
  }

  @Override
  public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
  {
    return new VehicleCoreBlockEntity(ModVehicleBlocks.VEHICLE_CORE_BLOCK_ENTITY.get(), pos, state);
  }

  @Override
  public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
  {
    return null;
  }

  @Override
  protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit)
  {
    if (level.isClientSide()) return InteractionResult.SUCCESS;

    BlockEntity be = level.getBlockEntity(pos);
    if (!(be instanceof VehicleCoreBlockEntity core)) return InteractionResult.PASS;

    return core.onUse(level, pos, state, player);
  }

  @Override
  public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston)
  {
    if (!state.is(newState.getBlock()))
    {
      BlockEntity be = level.getBlockEntity(pos);
      if (be instanceof VehicleCoreBlockEntity core) core.markDisassembled();
    }
    super.onRemove(state, level, pos, newState, movedByPiston);
  }
}