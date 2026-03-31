package cute.ame.auralithpioneerinitiative.Registrie;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Planet.Arid.Worldgen.AridBiomeSource;
import cute.ame.auralithpioneerinitiative.Planet.Arid.Worldgen.AridChunkGenerator;
import cute.ame.auralithpioneerinitiative.Planet.Arid.Worldgen.Feature.AridStrataFeature;
import cute.ame.auralithpioneerinitiative.Planet.Arid.Worldgen.Feature.CrystalSpikeFeature;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModWorldgen
{
  private ModWorldgen() {}

  public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Auralithpioneerinitiative.MODID);
  public static final DeferredHolder<Feature<?>, AridStrataFeature> ARID_STRATA = FEATURES.register("arid_strata", () -> new AridStrataFeature(NoneFeatureConfiguration.CODEC));
  public static final DeferredHolder<Feature<?>, CrystalSpikeFeature> CRYSTAL_SPIKE = FEATURES.register("crystal_spike", () -> new CrystalSpikeFeature(NoneFeatureConfiguration.CODEC));

  public static void registerCodecs()
  {
    Registry.register(
        BuiltInRegistries.BIOME_SOURCE,
        ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "arid_biome_source"),
        AridBiomeSource.CODEC
    );

    Registry.register(
        BuiltInRegistries.CHUNK_GENERATOR,
        ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "arid_generator"),
        AridChunkGenerator.CODEC
    );
    Auralithpioneerinitiative.LOGGER.info("[Auralith] Registered Arid worldgen codecs");
  }
}