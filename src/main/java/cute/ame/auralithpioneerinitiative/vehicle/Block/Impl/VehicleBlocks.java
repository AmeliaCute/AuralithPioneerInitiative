package cute.ame.auralithpioneerinitiative.vehicle.Block.Impl;

import cute.ame.auralithpioneerinitiative.vehicle.Block.VehicleBlockInterfaces.*;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public final class VehicleBlocks
{
  private VehicleBlocks() {}

  public static class SuspensionBlock extends Block implements ISuspensionBlock
  {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private final float restHeight;
    private final float stiffness;
    private final float damping;

    public SuspensionBlock(BlockBehaviour.Properties props, float restHeight, float stiffness, float damping)
    {
      super(props);
      this.restHeight = restHeight;
      this.stiffness = stiffness;
      this.damping = damping;
      registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN));
    }

    @Override public float getRestHeight() { return restHeight; }
    @Override public float getStiffness() { return stiffness; }
    @Override public float getDamping() { return damping; }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
      return defaultBlockState().setValue(FACING, ctx.getClickedFace().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b)
    {
      b.add(FACING);
    }
  }

  public static class RocketMotorBlock extends Block implements IRocketMotorBlock
  {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private final float thrustKN;
    private final float isp;
    private final int stageIndex;

    public RocketMotorBlock(BlockBehaviour.Properties props, float thrustKN, float isp, int stageIndex)
    {
      super(props);
      this.thrustKN = thrustKN;
      this.isp = isp;
      this.stageIndex = stageIndex;
      registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN));
    }

    @Override public float getThrustKN() { return thrustKN; }
    @Override public float getIsp() { return isp; }
    @Override public int getStageIndex() { return stageIndex; }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
      return defaultBlockState().setValue(FACING, ctx.getClickedFace().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b)
    {
      b.add(FACING);
    }
  }

  public static class FuelTankBlock extends Block implements IFuelTankBlock
  {
    private final float capacity;

    public FuelTankBlock(BlockBehaviour.Properties props, float capacity)
    {
      super(props);
      this.capacity = capacity;
    }

    @Override public float getCapacity() { return capacity; }
  }

  public static class GyroscopeBlock extends Block implements IGyroscopeBlock
  {
    private final float maxTorque;
    private final long powerConsumption;

    public GyroscopeBlock(BlockBehaviour.Properties props, float maxTorque, long powerConsumption)
    {
      super(props);
      this.maxTorque = maxTorque;
      this.powerConsumption = powerConsumption;
    }

    @Override public float getMaxTorque() { return maxTorque; }
    @Override public long getPowerConsumption() { return powerConsumption; }
  }
}