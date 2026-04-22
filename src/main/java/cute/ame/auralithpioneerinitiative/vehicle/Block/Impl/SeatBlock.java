package cute.ame.auralithpioneerinitiative.vehicle.Block.Impl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SeatBlock extends Block
{
  public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
  public static final BooleanProperty PILOT = BooleanProperty.create("pilot");

  private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 8, 14);

  public SeatBlock(BlockBehaviour.Properties props)
  {
    super(props);
    registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PILOT, true));
  }

  @Override
  public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx)
  {
    return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite()).setValue(PILOT, !ctx.getPlayer().isShiftKeyDown());
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
  {
    builder.add(FACING, PILOT);
  }

  @Override
  public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx)
  {
    return SHAPE;
  }

  @Override
  public RenderShape getRenderShape(BlockState state)
  {
    return RenderShape.MODEL;
  }

  public static boolean isPilotSeat(BlockState state)
  {
    return state.getValue(PILOT);
  }

  public static Direction getFacing(BlockState state)
  {
    return state.getValue(FACING);
  }
}