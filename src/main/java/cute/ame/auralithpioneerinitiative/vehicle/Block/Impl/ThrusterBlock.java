package cute.ame.auralithpioneerinitiative.vehicle.Block.Impl;

import cute.ame.auralithpioneerinitiative.vehicle.Block.VehicleBlockInterfaces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public class ThrusterBlock extends Block implements VehicleBlockInterfaces.IThrusterBlock
{
  public static final DirectionProperty FACING = BlockStateProperties.FACING;
  private final float thrustForce;
  private final float fuelConsumption;

  public ThrusterBlock(BlockBehaviour.Properties props, float thrustForce, float fuelConsumption)
  {
    super(props);
    this.thrustForce = thrustForce;
    this.fuelConsumption = fuelConsumption;
    registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN));
  }

  @Override public float getThrustForce() { return thrustForce; }
  @Override public float getFuelConsumption() { return fuelConsumption; }

  @Override
  public Direction getThrustDirection(BlockState state)
  {
    return state.getValue(FACING).getOpposite();
  }

  @Override
  public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx)
  {
    return defaultBlockState().setValue(FACING, ctx.getClickedFace().getOpposite());
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
  {
    builder.add(FACING);
  }
}