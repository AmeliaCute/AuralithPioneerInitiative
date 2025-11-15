package cute.ame.auralithpioneerinitiative.Space.Biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PlanetBiome(
        int id,
        float seed,
        float iceCoverage,
        float landRatio,
        boolean hasClouds,
        float cloudCoverage,
        int cloudLayers,
        float cloudSpeed,
        float[] cloudColor,
        boolean hasAtmosphere,
        float fresnelPower,
        float atmosphereIntensity,
        float[] atmosphereColor,

        SurfaceColors surfaceColors,
        CloudSettings cloudSettings,
        AtmosphereSettings atmosphereSettings
) {
    public static final int BIOME_DEFAULT = 0;
    public static final int BIOME_AQUATIC = 1;
    public static final int BIOME_ARCTIC = 2;
    public static final int BIOME_DESERT = 3;
    public static final int BIOME_ROCKY = 4;
    public static final int BIOME_ICE = 5;
    public static final int BIOME_GAS_GIANT = 6;
    public static final int BIOME_AMMONIA = 7;
    public static final int BIOME_VOLCANIC = 8;

    public static final Codec<PlanetBiome> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("id").forGetter(PlanetBiome::id),
                    Codec.FLOAT.fieldOf("seed").forGetter(PlanetBiome::seed),
                    Codec.FLOAT.fieldOf("ice_coverage").forGetter(PlanetBiome::iceCoverage),
                    Codec.FLOAT.fieldOf("land_ratio").forGetter(PlanetBiome::landRatio),
                    Codec.BOOL.fieldOf("has_clouds").forGetter(PlanetBiome::hasClouds),
                    Codec.FLOAT.fieldOf("cloud_coverage").forGetter(PlanetBiome::cloudCoverage),
                    Codec.INT.fieldOf("cloud_layers").forGetter(PlanetBiome::cloudLayers),
                    Codec.FLOAT.fieldOf("cloud_speed").forGetter(PlanetBiome::cloudSpeed),
                    floatArrayCodec(4).fieldOf("cloud_color").forGetter(PlanetBiome::cloudColor),
                    Codec.BOOL.fieldOf("has_atmosphere").forGetter(PlanetBiome::hasAtmosphere),
                    Codec.FLOAT.fieldOf("fresnel_power").forGetter(PlanetBiome::fresnelPower),
                    Codec.FLOAT.fieldOf("atmosphere_intensity").forGetter(PlanetBiome::atmosphereIntensity),
                    floatArrayCodec(4).fieldOf("atmosphere_color").forGetter(PlanetBiome::atmosphereColor),
                    SurfaceColors.CODEC.optionalFieldOf("surface_colors", SurfaceColors.DEFAULT).forGetter(PlanetBiome::surfaceColors),
                    CloudSettings.CODEC.optionalFieldOf("cloud_settings", CloudSettings.DEFAULT).forGetter(PlanetBiome::cloudSettings),
                    AtmosphereSettings.CODEC.optionalFieldOf("atmosphere_settings", AtmosphereSettings.DEFAULT).forGetter(PlanetBiome::atmosphereSettings)
            ).apply(instance, PlanetBiome::new)
    );

    public record SurfaceColors(
            float[] color1,
            float[] color2,
            float[] color3,
            float noiseScale1,
            float noiseScale2,
            float noiseScale3,
            int noiseOctaves,
            float rimLightPower,
            float rimLightIntensity
    ) {
        public static final SurfaceColors DEFAULT = new SurfaceColors(
                new float[]{0.5f, 0.5f, 0.5f, 1.0f},
                new float[]{0.6f, 0.6f, 0.6f, 1.0f},
                new float[]{0.4f, 0.4f, 0.4f, 1.0f},
                3.0f, 5.0f, 4.0f,
                3,
                2.0f, 0.15f
        );

        public static final Codec<SurfaceColors> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        floatArrayCodec(4).fieldOf("color1").forGetter(SurfaceColors::color1),
                        floatArrayCodec(4).fieldOf("color2").forGetter(SurfaceColors::color2),
                        floatArrayCodec(4).fieldOf("color3").forGetter(SurfaceColors::color3),
                        Codec.FLOAT.optionalFieldOf("noise_scale_1", 3.0f).forGetter(SurfaceColors::noiseScale1),
                        Codec.FLOAT.optionalFieldOf("noise_scale_2", 5.0f).forGetter(SurfaceColors::noiseScale2),
                        Codec.FLOAT.optionalFieldOf("noise_scale_3", 4.0f).forGetter(SurfaceColors::noiseScale3),
                        Codec.INT.optionalFieldOf("noise_octaves", 3).forGetter(SurfaceColors::noiseOctaves),
                        Codec.FLOAT.optionalFieldOf("rim_light_power", 2.0f).forGetter(SurfaceColors::rimLightPower),
                        Codec.FLOAT.optionalFieldOf("rim_light_intensity", 0.15f).forGetter(SurfaceColors::rimLightIntensity)
                ).apply(instance, SurfaceColors::new)
        );
    }

    public record CloudSettings(
            float turbulence,
            float density,
            float edgeSoftness,
            float shadowIntensity,
            boolean castsShadows,
            float animationSpeed
    ) {
        public static final CloudSettings DEFAULT = new CloudSettings(
                1.0f, 0.8f, 0.1f, 0.3f, false, 1.0f
        );

        public static final Codec<CloudSettings> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.FLOAT.optionalFieldOf("turbulence", 1.0f).forGetter(CloudSettings::turbulence),
                        Codec.FLOAT.optionalFieldOf("density", 0.8f).forGetter(CloudSettings::density),
                        Codec.FLOAT.optionalFieldOf("edge_softness", 0.1f).forGetter(CloudSettings::edgeSoftness),
                        Codec.FLOAT.optionalFieldOf("shadow_intensity", 0.3f).forGetter(CloudSettings::shadowIntensity),
                        Codec.BOOL.optionalFieldOf("casts_shadows", false).forGetter(CloudSettings::castsShadows),
                        Codec.FLOAT.optionalFieldOf("animation_speed", 1.0f).forGetter(CloudSettings::animationSpeed)
                ).apply(instance, CloudSettings::new)
        );
    }

    public record AtmosphereSettings(
            float glowRadius,
            float glowFalloff,
            float scatteringIntensity,
            boolean enableScattering,
            float dayNightBlend
    ) {
        public static final AtmosphereSettings DEFAULT = new AtmosphereSettings(
                1.05f, 2.0f, 1.0f, true, 0.0f
        );

        public static final Codec<AtmosphereSettings> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.FLOAT.optionalFieldOf("glow_radius", 1.05f).forGetter(AtmosphereSettings::glowRadius),
                        Codec.FLOAT.optionalFieldOf("glow_falloff", 2.0f).forGetter(AtmosphereSettings::glowFalloff),
                        Codec.FLOAT.optionalFieldOf("scattering_intensity", 1.0f).forGetter(AtmosphereSettings::scatteringIntensity),
                        Codec.BOOL.optionalFieldOf("enable_scattering", true).forGetter(AtmosphereSettings::enableScattering),
                        Codec.FLOAT.optionalFieldOf("day_night_blend", 0.0f).forGetter(AtmosphereSettings::dayNightBlend)
                ).apply(instance, AtmosphereSettings::new)
        );
    }

    private static Codec<float[]> floatArrayCodec(int size) {
        return Codec.FLOAT.listOf().xmap(
                list -> {
                    float[] arr = new float[Math.min(list.size(), size)];
                    for (int i = 0; i < arr.length; i++) arr[i] = list.get(i);
                    return arr;
                },
                arr -> {
                    java.util.List<Float> list = new java.util.ArrayList<>();
                    for (float f : arr) list.add(f);
                    return list;
                }
        );
    }
}