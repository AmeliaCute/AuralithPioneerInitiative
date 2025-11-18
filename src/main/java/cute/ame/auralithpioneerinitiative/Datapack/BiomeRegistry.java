package cute.ame.auralithpioneerinitiative.Datapack;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;

public class BiomeRegistry
{
    public static final ResourceKey<Biome> SUNLIT_WATERS = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "sunlit_waters")
    );

    public static final ResourceKey<Biome> TWILIGHT_DEPTHS = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "twilight_depths")
    );

    public static final ResourceKey<Biome> MIDNIGHT_ABYSS = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "midnight_abyss")
    );

    public static final ResourceKey<Biome> ABYSSAL_PLAINS = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "abyssal_plains")
    );

    public static final ResourceKey<Biome> HADAL_TRENCH = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "hadal_trench")
    );

    public static final ResourceKey<Biome> THERMAL_SPRINGS = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "thermal_springs")
    );

    public static void bootstrap(BootstrapContext<Biome> context)
    {
        context.register(SUNLIT_WATERS, sunlitWaters(context));
        context.register(TWILIGHT_DEPTHS, twilightDepths(context));
        context.register(MIDNIGHT_ABYSS, midnightAbyss(context));
        context.register(ABYSSAL_PLAINS, abyssalPlains(context));
        context.register(HADAL_TRENCH, hadalTrench(context));
        context.register(THERMAL_SPRINGS, thermalSprings(context));
    }

    private static Biome sunlitWaters(BootstrapContext<Biome> context)
    {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();

        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE),
                context.lookup(Registries.CONFIGURED_CARVER)
        );

        BiomeDefaultFeatures.addDefaultSeagrass(biomeBuilder);

        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.5F)
                .downfall(0.5F)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0x1E90FF)
                        .waterFogColor(0x0A4D8C)
                        .fogColor(0x0A4D8C)
                        .skyColor(0x0A4D8C)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_UNDER_WATER))
                        .build())
                .mobSpawnSettings(spawnBuilder.build())
                .generationSettings(biomeBuilder.build())
                .build();
    }

    private static Biome twilightDepths(BootstrapContext<Biome> context)
    {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();

        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE),
                context.lookup(Registries.CONFIGURED_CARVER)
        );

        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.3F)
                .downfall(0.8F)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0x0B3D5F)
                        .waterFogColor(0x041D2F)
                        .fogColor(0x041D2F)
                        .skyColor(0x041D2F)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build())
                .mobSpawnSettings(spawnBuilder.build())
                .generationSettings(biomeBuilder.build())
                .build();
    }

    private static Biome midnightAbyss(BootstrapContext<Biome> context)
    {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();

        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE),
                context.lookup(Registries.CONFIGURED_CARVER)
        );

        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.1F)
                .downfall(0.8F)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0x020A14)
                        .waterFogColor(0x000000)
                        .fogColor(0x000000)
                        .skyColor(0x000000)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build())
                .mobSpawnSettings(spawnBuilder.build())
                .generationSettings(biomeBuilder.build())
                .build();
    }

    private static Biome abyssalPlains(BootstrapContext<Biome> context)
    {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();

        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE),
                context.lookup(Registries.CONFIGURED_CARVER)
        );

        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.05F)
                .downfall(0.9F)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0x000000)
                        .waterFogColor(0x000000)
                        .fogColor(0x000000)
                        .skyColor(0x000000)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build())
                .mobSpawnSettings(spawnBuilder.build())
                .generationSettings(biomeBuilder.build())
                .build();
    }

    private static Biome hadalTrench(BootstrapContext<Biome> context)
    {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();

        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE),
                context.lookup(Registries.CONFIGURED_CARVER)
        );

        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.0F)
                .downfall(1.0F)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0x000000)
                        .waterFogColor(0x000000)
                        .fogColor(0x000000)
                        .skyColor(0x000000)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build())
                .mobSpawnSettings(spawnBuilder.build())
                .generationSettings(biomeBuilder.build())
                .build();
    }

    private static Biome thermalSprings(BootstrapContext<Biome> context)
    {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();

        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE),
                context.lookup(Registries.CONFIGURED_CARVER)
        );

//        biomeBuilder.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES,
//                PlacedFeaturesRegistry.THERMAL_GEYSER);
//
//        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES,
//                PlacedFeaturesRegistry.PRESSURE_CRYSTAL_ORE);
//
//        biomeBuilder.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES,
//                PlacedFeaturesRegistry.VOLCANIC_ROCK_PILE);

        biomeBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION,
                PlacedFeaturesRegistry.THERMAL_PLANTS_I);
        biomeBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION,
                PlacedFeaturesRegistry.THERMAL_KELP);

        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.9F)
                .downfall(0.4F)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0x000000)
                        .waterFogColor(0x000000)
                        .fogColor(0x000000)
                        .skyColor(0x000000)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build())
                .mobSpawnSettings(spawnBuilder.build())
                .generationSettings(biomeBuilder.build())
                .build();
    }
}
