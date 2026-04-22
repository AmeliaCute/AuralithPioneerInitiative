package cute.ame.auralithpioneerinitiative.vehicle.Register;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.vehicle.Assembly.VehicleCoreBlockEntity;
import cute.ame.auralithpioneerinitiative.vehicle.Block.Impl.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModVehicleBlocks
{

  private ModVehicleBlocks() {}

  public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Auralithpioneerinitiative.MODID);
  public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Auralithpioneerinitiative.MODID);

  public static final DeferredBlock<VehicleCoreBlock> VEHICLE_CORE = BLOCKS.register(
      "vehicle_core",
      () -> new VehicleCoreBlock(BlockBehaviour.Properties.of()
          .mapColor(MapColor.METAL)
          .requiresCorrectToolForDrops()
          .strength(5.0f, 1200.0f)
          .sound(SoundType.METAL))
  );

  public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VehicleCoreBlockEntity>>
      VEHICLE_CORE_BLOCK_ENTITY = BLOCK_ENTITIES.register(
      "vehicle_core",
      () -> BlockEntityType.Builder
          .of(VehicleCoreBlockEntity::new, VEHICLE_CORE.get())
          .build(null)
  );

  public static final DeferredBlock<SeatBlock> SEAT = BLOCKS.register(
      "vehicle_seat",
      () -> new SeatBlock(BlockBehaviour.Properties.of()
          .mapColor(MapColor.COLOR_GRAY)
          .requiresCorrectToolForDrops()
          .strength(1.5f, 400.0f)
          .sound(SoundType.METAL)
          .noOcclusion())
  );

  public static final DeferredBlock<ThrusterBlock> THRUSTER_SMALL = BLOCKS.register(
      "thruster_small",
      () -> new ThrusterBlock(BlockBehaviour.Properties.of()
          .mapColor(MapColor.METAL).requiresCorrectToolForDrops()
          .strength(3.0f, 600.0f).sound(SoundType.METAL).noOcclusion(),
          5_000f, 0.5f)
  );

  public static final DeferredBlock<ThrusterBlock> THRUSTER_MEDIUM = BLOCKS.register(
      "thruster_medium",
      () -> new ThrusterBlock(BlockBehaviour.Properties.of()
          .mapColor(MapColor.METAL).requiresCorrectToolForDrops()
          .strength(4.0f, 800.0f).sound(SoundType.METAL).noOcclusion(),
          25_000f, 2.0f)
  );

  public static final DeferredBlock<ThrusterBlock> THRUSTER_LARGE = BLOCKS.register(
      "thruster_large",
      () -> new ThrusterBlock(BlockBehaviour.Properties.of()
          .mapColor(MapColor.METAL).requiresCorrectToolForDrops()
          .strength(5.0f, 1000.0f).sound(SoundType.METAL).noOcclusion(),
          100_000f, 8.0f)
  );

  public static final DeferredBlock<VehicleBlocks.SuspensionBlock> SUSPENSION_LIGHT = BLOCKS.register(
      "suspension_light",
      () -> new VehicleBlocks.SuspensionBlock(BlockBehaviour.Properties.of()
          .mapColor(MapColor.METAL).requiresCorrectToolForDrops()
          .strength(2.0f, 400.0f).sound(SoundType.METAL),
          1.5f, 0.10f, 0.05f)
  );

  public static final DeferredBlock<VehicleBlocks.SuspensionBlock> SUSPENSION_HEAVY = BLOCKS.register(
      "suspension_heavy",
      () -> new VehicleBlocks.SuspensionBlock(BlockBehaviour.Properties.of()
          .mapColor(MapColor.METAL).requiresCorrectToolForDrops()
          .strength(4.0f, 800.0f).sound(SoundType.METAL),
          2.0f, 0.06f, 0.08f)
  );

  public static final DeferredBlock<VehicleBlocks.RocketMotorBlock> ROCKET_MOTOR_SOLID = BLOCKS.register(
      "rocket_motor_solid",
      () -> new VehicleBlocks.RocketMotorBlock(BlockBehaviour.Properties.of()
          .mapColor(MapColor.METAL).requiresCorrectToolForDrops()
          .strength(5.0f, 1200.0f).sound(SoundType.METAL).noOcclusion(),
          180f, 280f, 0)
  );

  public static final DeferredBlock<VehicleBlocks.RocketMotorBlock> ROCKET_MOTOR_LIQUID = BLOCKS.register(
      "rocket_motor_liquid",
      () -> new VehicleBlocks.RocketMotorBlock(BlockBehaviour.Properties.of()
          .mapColor(MapColor.METAL).requiresCorrectToolForDrops()
          .strength(5.0f, 1200.0f).sound(SoundType.METAL).noOcclusion(),
          90f, 420f, 1)
  );

  public static final DeferredBlock<VehicleBlocks.FuelTankBlock> FUEL_TANK_SMALL = BLOCKS.register(
      "fuel_tank_small",
      () -> new VehicleBlocks.FuelTankBlock(BlockBehaviour.Properties.of()
          .mapColor(MapColor.METAL).requiresCorrectToolForDrops()
          .strength(2.0f, 400.0f).sound(SoundType.METAL),
          500f)
  );

  public static final DeferredBlock<VehicleBlocks.FuelTankBlock> FUEL_TANK_LARGE = BLOCKS.register(
      "fuel_tank_large",
      () -> new VehicleBlocks.FuelTankBlock(BlockBehaviour.Properties.of()
          .mapColor(MapColor.METAL).requiresCorrectToolForDrops()
          .strength(3.0f, 600.0f).sound(SoundType.METAL),
          2000f)
  );

  public static final DeferredBlock<VehicleBlocks.GyroscopeBlock> GYROSCOPE = BLOCKS.register(
      "gyroscope",
      () -> new VehicleBlocks.GyroscopeBlock(BlockBehaviour.Properties.of()
          .mapColor(MapColor.METAL).requiresCorrectToolForDrops()
          .strength(3.0f, 600.0f).sound(SoundType.METAL),
          500f, 50L)
  );
}