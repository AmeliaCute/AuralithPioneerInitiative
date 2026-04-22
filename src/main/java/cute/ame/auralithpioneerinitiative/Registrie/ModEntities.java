package cute.ame.auralithpioneerinitiative.Registrie;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity;
import cute.ame.auralithpioneerinitiative.vehicle.Types.SpaceshipEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.joml.Quaternionf;

public final class ModEntities
{
    private ModEntities() {}

    public static final EntityDataSerializer<Quaternionf> QUATERNIONF =
      EntityDataSerializer.forValueType(
        net.minecraft.network.codec.StreamCodec.of(
          (buf, q) ->
          {
              buf.writeFloat(q.x);
              buf.writeFloat(q.y);
              buf.writeFloat(q.z);
              buf.writeFloat(q.w);
          },
          buf -> new Quaternionf(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat())
        )
      );

    public static final DeferredRegister<EntityDataSerializer<?>> DATA_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, Auralithpioneerinitiative.MODID);

    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<Quaternionf>> QUATERNIONF_SERIALIZER = DATA_SERIALIZERS.register("quaternionf", () -> QUATERNIONF);

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, Auralithpioneerinitiative.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<ShipEntity>> SHIP =
      ENTITIES.register("ship", () ->
        EntityType.Builder.<ShipEntity>of(ShipEntity::new, MobCategory.MISC)
          .sized(1.0f, 1.0f)
          .clientTrackingRange(512)
          .updateInterval(1)
          .build(ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "ship").toString())
      );

  public static final DeferredHolder<EntityType<?>, EntityType<SpaceshipEntity>> SPACESHIP =
      ENTITIES.register("spaceship", () ->
          EntityType.Builder.<SpaceshipEntity>of(SpaceshipEntity::new, MobCategory.MISC)
              .sized(1.0f, 1.0f)
              .clientTrackingRange(512)
              .updateInterval(1)
              .build(ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "spaceship").toString())
      );
}