package cute.ame.auralithpioneerinitiative.vehicle.Block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class VehicleBlockInterfaces
{
  private VehicleBlockInterfaces() {}

  public interface IThrusterBlock
  {
    float getThrustForce();
    float getFuelConsumption();
    default Direction getThrustDirection(BlockState state)
    {
      if (state.hasProperty(BlockStateProperties.FACING)) return state.getValue(BlockStateProperties.FACING).getOpposite();
      return Direction.UP;
    }
  }

  public interface ISuspensionBlock
  {
    float getRestHeight();
    float getStiffness();
    float getDamping();
  }

  public interface IRocketMotorBlock
  {
    float getThrustKN();
    float getIsp();
    int getStageIndex();

    default float computeFuelRate()
    {
      return getThrustKN() / (getIsp() * 0.1f);
    }
  }

  public interface IFuelTankBlock
  {
    float getCapacity();
    default float getCurrentFuel() { return getCapacity(); }
  }

  public interface IGyroscopeBlock
  {
    float getMaxTorque();
    default long getPowerConsumption() { return 100L; }
  }

  public interface IStructuralBlock
  {
    float getMass();
  }
}