package cute.ame.auralithpioneerinitiative.Registries;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Space.Biome.PlanetBiome;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PlanetBiomeRegistry {

    public static final ResourceKey<Registry<PlanetBiome>> PLANET_BIOME_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "planet_biome"));

    public static final DeferredRegister<PlanetBiome> PLANET_BIOMES =
            DeferredRegister.create(PLANET_BIOME_REGISTRY_KEY, Auralithpioneerinitiative.MODID);

    public static final DeferredHolder<PlanetBiome, PlanetBiome> BIOME_DEFAULT =
            PLANET_BIOMES.register("default", () -> new PlanetBiome(
                    PlanetBiome.BIOME_DEFAULT,
                    42.0f,
                    0.7f,
                    0.3f,
                    true,
                    0.5f,
                    2,
                    0.01f,
                    new float[]{1.0f, 1.0f, 1.0f, 1.0f},
                    true,
                    1.0f,
                    0.4f,
                    new float[]{0.3f, 0.6f, 1.0f, 0.04f},
                    PlanetBiome.SurfaceColors.DEFAULT,
                    PlanetBiome.CloudSettings.DEFAULT,
                    PlanetBiome.AtmosphereSettings.DEFAULT
            ));

    public static final DeferredHolder<PlanetBiome, PlanetBiome> BIOME_AQUATIC =
            PLANET_BIOMES.register("aquatic", () -> new PlanetBiome(
                    PlanetBiome.BIOME_AQUATIC,
                    42.0f,
                    0.1f,
                    1.0f,
                    true,
                    0.4f,
                    2,
                    0.01f,
                    new float[]{1.0f, 1.0f, 1.0f, 1.0f},
                    true,
                    1.0f,
                    0.4f,
                    new float[]{0.3f, 0.6f, 1.0f, 0.04f},
                    new PlanetBiome.SurfaceColors(
                            new float[]{0.1f, 0.3f, 0.6f, 1.0f},
                            new float[]{0.2f, 0.5f, 0.8f, 1.0f},
                            new float[]{0.3f, 0.6f, 0.3f, 1.0f},
                            3.0f, 5.0f, 4.0f, 3, 2.0f, 0.15f
                    ),
                    PlanetBiome.CloudSettings.DEFAULT,
                    PlanetBiome.AtmosphereSettings.DEFAULT
            ));

    public static final DeferredHolder<PlanetBiome, PlanetBiome> BIOME_ARCTIC =
            PLANET_BIOMES.register("arctic", () -> new PlanetBiome(
                    PlanetBiome.BIOME_ARCTIC,
                    42.0f,
                    0.9f,
                    0.2f,
                    true,
                    0.2f,
                    1,
                    0.01f,
                    new float[]{1.0f, 1.0f, 1.0f, 1.0f},
                    true,
                    1.0f,
                    0.4f,
                    new float[]{0.7f, 0.8f, 0.95f, 0.04f},
                    new PlanetBiome.SurfaceColors(
                            new float[]{0.85f, 0.9f, 0.95f, 1.0f},
                            new float[]{0.7f, 0.8f, 0.9f, 1.0f},
                            new float[]{0.4f, 0.5f, 0.6f, 1.0f},
                            3.0f, 5.0f, 4.0f, 3, 2.0f, 0.15f
                    ),
                    PlanetBiome.CloudSettings.DEFAULT,
                    PlanetBiome.AtmosphereSettings.DEFAULT
            ));

    public static final DeferredHolder<PlanetBiome, PlanetBiome> BIOME_DESERT =
            PLANET_BIOMES.register("desert", () -> new PlanetBiome(
                    PlanetBiome.BIOME_DESERT,
                    42.0f,
                    0.0f,
                    0.95f,
                    true,
                    0.1f,
                    1,
                    0.01f,
                    new float[]{1.0f, 1.0f, 1.0f, 1.0f},
                    true,
                    1.0f,
                    0.4f,
                    new float[]{0.9f, 0.7f, 0.5f, 0.04f},
                    new PlanetBiome.SurfaceColors(
                            new float[]{0.9f, 0.7f, 0.4f, 1.0f},
                            new float[]{0.8f, 0.6f, 0.3f, 1.0f},
                            new float[]{0.6f, 0.4f, 0.2f, 1.0f},
                            3.0f, 5.0f, 4.0f, 3, 2.0f, 0.15f
                    ),
                    PlanetBiome.CloudSettings.DEFAULT,
                    PlanetBiome.AtmosphereSettings.DEFAULT
            ));

    public static final DeferredHolder<PlanetBiome, PlanetBiome> BIOME_ROCKY =
            PLANET_BIOMES.register("rocky", () -> new PlanetBiome(
                    PlanetBiome.BIOME_ROCKY,
                    42.0f,
                    0.0f,
                    1.0f,
                    false,
                    0.0f,
                    0,
                    0.01f,
                    new float[]{1.0f, 1.0f, 1.0f, 1.0f},
                    false,
                    1.0f,
                    0.4f,
                    new float[]{0.3f, 0.6f, 1.0f, 0.04f},
                    new PlanetBiome.SurfaceColors(
                            new float[]{0.4f, 0.35f, 0.3f, 1.0f},
                            new float[]{0.5f, 0.45f, 0.4f, 1.0f},
                            new float[]{0.3f, 0.25f, 0.2f, 1.0f},
                            3.0f, 5.0f, 4.0f, 3, 2.0f, 0.15f
                    ),
                    PlanetBiome.CloudSettings.DEFAULT,
                    PlanetBiome.AtmosphereSettings.DEFAULT
            ));

    public static final DeferredHolder<PlanetBiome, PlanetBiome> BIOME_ICE =
            PLANET_BIOMES.register("ice", () -> new PlanetBiome(
                    PlanetBiome.BIOME_ICE,
                    42.0f,
                    1.0f,
                    1.0f,
                    true,
                    0.3f,
                    1,
                    0.01f,
                    new float[]{1.0f, 1.0f, 1.0f, 1.0f},
                    true,
                    1.0f,
                    0.4f,
                    new float[]{0.7f, 0.85f, 0.95f, 0.04f},
                    new PlanetBiome.SurfaceColors(
                            new float[]{0.7f, 0.85f, 0.95f, 1.0f},
                            new float[]{0.5f, 0.7f, 0.9f, 1.0f},
                            new float[]{0.9f, 0.95f, 1.0f, 1.0f},
                            3.0f, 5.0f, 4.0f, 3, 2.0f, 0.15f
                    ),
                    PlanetBiome.CloudSettings.DEFAULT,
                    PlanetBiome.AtmosphereSettings.DEFAULT
            ));

    public static final DeferredHolder<PlanetBiome, PlanetBiome> BIOME_GAS_GIANT =
            PLANET_BIOMES.register("gas_giant", () -> new PlanetBiome(
                    PlanetBiome.BIOME_GAS_GIANT,
                    42.0f,
                    0.0f,
                    0.0f,
                    true,
                    1.0f,
                    3,
                    0.02f,
                    new float[]{1.0f, 1.0f, 1.0f, 1.0f},
                    true,
                    1.0f,
                    2.0f,
                    new float[]{0.9f, 0.7f, 0.5f, 0.04f},
                    new PlanetBiome.SurfaceColors(
                            new float[]{0.9f, 0.7f, 0.5f, 1.0f},
                            new float[]{0.8f, 0.6f, 0.4f, 1.0f},
                            new float[]{0.95f, 0.8f, 0.6f, 1.0f},
                            2.0f, 4.0f, 3.0f, 3, 1.5f, 0.3f
                    ),
                    new PlanetBiome.CloudSettings(
                            2.0f, 1.0f, 0.05f, 0.5f, true, 1.8f
                    ),
                    new PlanetBiome.AtmosphereSettings(
                            1.1f, 1.8f, 1.8f, true, 0.0f
                    )
            ));

    public static final DeferredHolder<PlanetBiome, PlanetBiome> BIOME_AMMONIA =
            PLANET_BIOMES.register("ammonia", () -> new PlanetBiome(
                    PlanetBiome.BIOME_AMMONIA,
                    42.0f,
                    0.0f,
                    0.4f,
                    true,
                    0.6f,
                    2,
                    0.01f,
                    new float[]{0.7f, 0.9f, 0.5f, 1.0f},
                    true,
                    1.0f,
                    0.4f,
                    new float[]{0.6f, 0.8f, 0.4f, 0.04f},
                    new PlanetBiome.SurfaceColors(
                            new float[]{0.6f, 0.8f, 0.3f, 1.0f},
                            new float[]{0.4f, 0.7f, 0.5f, 1.0f},
                            new float[]{0.8f, 0.9f, 0.5f, 1.0f},
                            3.0f, 5.0f, 4.0f, 3, 2.0f, 0.15f
                    ),
                    PlanetBiome.CloudSettings.DEFAULT,
                    PlanetBiome.AtmosphereSettings.DEFAULT
            ));

    public static final DeferredHolder<PlanetBiome, PlanetBiome> BIOME_VOLCANIC =
            PLANET_BIOMES.register("volcanic", () -> new PlanetBiome(
                    PlanetBiome.BIOME_VOLCANIC,
                    42.0f,
                    0.0f,
                    0.6f,
                    true,
                    0.3f,
                    2,
                    0.01f,
                    new float[]{0.4f, 0.35f, 0.3f, 1.0f},
                    true,
                    1.0f,
                    1.0f,
                    new float[]{0.8f, 0.4f, 0.2f, 0.04f},
                    new PlanetBiome.SurfaceColors(
                            new float[]{0.2f, 0.15f, 0.1f, 1.0f},
                            new float[]{0.9f, 0.3f, 0.1f, 1.0f},
                            new float[]{0.3f, 0.25f, 0.2f, 1.0f},
                            3.0f, 5.0f, 4.0f, 3, 2.0f, 0.15f
                    ),
                    PlanetBiome.CloudSettings.DEFAULT,
                    PlanetBiome.AtmosphereSettings.DEFAULT
            ));
}