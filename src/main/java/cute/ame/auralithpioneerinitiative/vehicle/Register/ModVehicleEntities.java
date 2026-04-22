package cute.ame.auralithpioneerinitiative.vehicle.Register;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.vehicle.Types.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModVehicleEntities
{
  private ModVehicleEntities() {}

  public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, Auralithpioneerinitiative.MODID);

  public static final DeferredHolder<EntityType<?>, EntityType<SpaceshipEntity>> SPACESHIP =
      ENTITIES.register("spaceship", () ->
          EntityType.Builder.<SpaceshipEntity>of(SpaceshipEntity::new, MobCategory.MISC)
              .sized(1.0f, 1.0f)
              .clientTrackingRange(512)
              .updateInterval(1)
              .fireImmune()
              .build(id("spaceship"))
      );

  public static final DeferredHolder<EntityType<?>, EntityType<RocketEntity>> ROCKET =
      ENTITIES.register("rocket", () ->
          EntityType.Builder.<RocketEntity>of(RocketEntity::new, MobCategory.MISC)
              .sized(1.0f, 1.0f)
              .clientTrackingRange(512)
              .updateInterval(1)
              .fireImmune()
              .build(id("rocket"))
      );

  public static final DeferredHolder<EntityType<?>, EntityType<SubmarineEntity>> SUBMARINE =
      ENTITIES.register("submarine", () ->
          EntityType.Builder.<SubmarineEntity>of(SubmarineEntity::new, MobCategory.MISC)
              .sized(1.0f, 1.0f)
              .clientTrackingRange(256)
              .updateInterval(1)
              .build(id("submarine"))
      );

  public static final DeferredHolder<EntityType<?>, EntityType<GroundVehicleEntity>> GROUND_VEHICLE =
      ENTITIES.register("ground_vehicle", () ->
          EntityType.Builder.<GroundVehicleEntity>of(GroundVehicleEntity::new, MobCategory.MISC)
              .sized(1.0f, 1.0f)
              .clientTrackingRange(128)
              .updateInterval(1)
              .build(id("ground_vehicle"))
      );

  private static String id(String path) {
    return ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, path).toString();
  }
}