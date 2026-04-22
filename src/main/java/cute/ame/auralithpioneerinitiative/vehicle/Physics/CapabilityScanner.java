package cute.ame.auralithpioneerinitiative.vehicle.Physics;

import cute.ame.auralithpioneerinitiative.vehicle.Baking.VehicleSnapshot;
import cute.ame.auralithpioneerinitiative.vehicle.Block.VehicleBlockInterfaces.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;

public final class CapabilityScanner
{
  private CapabilityScanner() {}

  private static final float DENSITY_LIGHT =  50f;
  private static final float DENSITY_MEDIUM = 150f;
  private static final float DENSITY_HEAVY = 400f;
  private static final float DENSITY_VHEAVY = 800f;

  public static VehicleCapabilities scan(List<VehicleSnapshot.BlockEntry> blocks)
  {
    if (blocks.isEmpty()) return VehicleCapabilities.empty(1f);

    float totalMass  = 0f;
    double comX = 0, comY = 0, comZ = 0;

    List<VehicleCapabilities.ThrusterMount> thrusters = new ArrayList<>();
    List<VehicleCapabilities.SuspensionMount> suspensions = new ArrayList<>();
    List<VehicleCapabilities.RocketMotorMount> rocketMotors= new ArrayList<>();
    Map<Integer, Float> stageFuel = new HashMap<>();
    float totalFuelCapacity = 0f;
    float totalGyroTorque = 0f;

    for (VehicleSnapshot.BlockEntry entry : blocks)
    {
      BlockState state = entry.resolveState();
      Block block = state.getBlock();

      float blockMass = computeMass(block, state);
      totalMass += blockMass;

      comX += entry.relX() * blockMass;
      comY += entry.relY() * blockMass;
      comZ += entry.relZ() * blockMass;

      Vec3 localPos = new Vec3(entry.relX() + 0.5, entry.relY() + 0.5, entry.relZ() + 0.5);

      if (block instanceof IThrusterBlock tb)
      {
        Direction dir = tb.getThrustDirection(state);
        Vector3f dirVec = new Vector3f(dir.getStepX(), dir.getStepY(), dir.getStepZ());
        thrusters.add(new VehicleCapabilities.ThrusterMount(localPos, dirVec, tb.getThrustForce(), tb.getFuelConsumption(), 0));
      }

      if (block instanceof ISuspensionBlock sb)
      {
        suspensions.add(new VehicleCapabilities.SuspensionMount(localPos, sb.getRestHeight(), sb.getStiffness(), sb.getDamping()));
      }

      if (block instanceof IRocketMotorBlock rm)
      {
        rocketMotors.add(new VehicleCapabilities.RocketMotorMount(localPos, rm.getThrustKN(), rm.getIsp(), rm.computeFuelRate()));
        stageFuel.putIfAbsent(rm.getStageIndex(), 0f);
      }

      if (block instanceof IFuelTankBlock ft)
      {
        totalFuelCapacity += ft.getCapacity();
      }

      if (block instanceof IGyroscopeBlock gb)
      {
        totalGyroTorque += gb.getMaxTorque();
      }
    }

    float safeMass = Math.max(1f, totalMass);
    Vec3 centerOfMass = new Vec3(comX / safeMass, comY / safeMass, comZ / safeMass);

    float radius = computeEquivalentRadius(blocks);
    float moi = 0.4f * safeMass * radius * radius;

    float maxTotalThrust = thrusters.stream().map(VehicleCapabilities.ThrusterMount::force).reduce(0f, Float::sum);
    float rocketThrust = rocketMotors.stream().map(m -> m.thrustKN() * 1000f).reduce(0f, Float::sum);
    maxTotalThrust += rocketThrust;

    Map<Integer, List<VehicleCapabilities.RocketMotorMount>> motorsByStage = new TreeMap<>();
    for (VehicleCapabilities.RocketMotorMount m : rocketMotors)
    {
      //TODO:
    }
    List<VehicleCapabilities.RocketStage> rocketStages = buildRocketStages(blocks, totalFuelCapacity);

    float currentFuel = totalFuelCapacity;
    return new VehicleCapabilities(safeMass, centerOfMass, moi, thrusters, maxTotalThrust, suspensions, totalFuelCapacity, currentFuel, rocketStages, totalGyroTorque);
  }

  private static List<VehicleCapabilities.RocketStage> buildRocketStages(List<VehicleSnapshot.BlockEntry> blocks, float totalFuel)
  {
    Map<Integer, List<VehicleCapabilities.RocketMotorMount>> byStage = new TreeMap<>();

    for (VehicleSnapshot.BlockEntry entry : blocks)
    {
      BlockState state = entry.resolveState();
      Block block = state.getBlock();
      if (!(block instanceof IRocketMotorBlock rm)) continue;

      Vec3 localPos = new Vec3(entry.relX() + 0.5, entry.relY() + 0.5, entry.relZ() + 0.5);
      byStage.computeIfAbsent(rm.getStageIndex(), k -> new ArrayList<>()).add(new VehicleCapabilities.RocketMotorMount(localPos, rm.getThrustKN(), rm.getIsp(), rm.computeFuelRate()));
    }

    List<VehicleCapabilities.RocketStage> stages = new ArrayList<>();
    int stageCount = byStage.size();
    float fuelPerStage = stageCount > 0 ? totalFuel / stageCount : 0f;

    for (Map.Entry<Integer, List<VehicleCapabilities.RocketMotorMount>> e : byStage.entrySet())
    {
      stages.add(new VehicleCapabilities.RocketStage(e.getKey(), e.getValue(), fuelPerStage));
    }

    return stages;
  }

  private static float computeMass(Block block, BlockState state)
  {
    if (block instanceof IStructuralBlock sb) return sb.getMass();
    float hardness = state.getDestroySpeed(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);

    if (hardness < 0)  return DENSITY_VHEAVY * 2;
    if (hardness < 1f) return DENSITY_LIGHT;
    if (hardness < 3f) return DENSITY_MEDIUM;
    if (hardness < 6f) return DENSITY_HEAVY;
    return DENSITY_VHEAVY;
  }

  private static float computeEquivalentRadius(List<VehicleSnapshot.BlockEntry> blocks)
  {
    float maxDist = 0f;
    int mnX = Integer.MAX_VALUE, mnY = Integer.MAX_VALUE, mnZ = Integer.MAX_VALUE;
    int mxX = Integer.MIN_VALUE, mxY = Integer.MIN_VALUE, mxZ = Integer.MIN_VALUE;
    for (VehicleSnapshot.BlockEntry b : blocks)
    {
      if (b.relX() < mnX) mnX = b.relX(); if (b.relX() > mxX) mxX = b.relX();
      if (b.relY() < mnY) mnY = b.relY(); if (b.relY() > mxY) mxY = b.relY();
      if (b.relZ() < mnZ) mnZ = b.relZ(); if (b.relZ() > mxZ) mxZ = b.relZ();
    }
    float dx = mxX - mnX, dy = mxY - mnY, dz = mxZ - mnZ;
    return (float)(Math.sqrt(dx*dx + dy*dy + dz*dz) * 0.5);
  }
}
