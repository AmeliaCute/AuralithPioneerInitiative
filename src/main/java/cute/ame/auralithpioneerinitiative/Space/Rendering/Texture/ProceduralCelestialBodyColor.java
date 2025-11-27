package cute.ame.auralithpioneerinitiative.Space.Rendering.Texture;

import cute.ame.auralithpioneerinitiative.Space.Biome.PlanetBiome;
import org.joml.Vector3f;

public class ProceduralCelestialBodyColor
{
    private static float hash(float x, float y, float z)
    {
        float h = (float)(Math.sin(x * 127.1 + y * 311.7 + z * 74.7) * 43758.5453);
        return h - (float)Math.floor(h);
    }

    private static float noise3D(float x, float y, float z)
    {
        float ix = (float)Math.floor(x);
        float iy = (float)Math.floor(y);
        float iz = (float)Math.floor(z);

        float fx = x - ix;
        float fy = y - iy;
        float fz = z - iz;

        fx = fx * fx * (3.0f - 2.0f * fx);
        fy = fy * fy * (3.0f - 2.0f * fy);
        fz = fz * fz * (3.0f - 2.0f * fz);

        float c000 = hash(ix, iy, iz);
        float c100 = hash(ix + 1, iy, iz);
        float c010 = hash(ix, iy + 1, iz);
        float c110 = hash(ix + 1, iy + 1, iz);
        float c001 = hash(ix, iy, iz + 1);
        float c101 = hash(ix + 1, iy, iz + 1);
        float c011 = hash(ix, iy + 1, iz + 1);
        float c111 = hash(ix + 1, iy + 1, iz + 1);

        float x00 = c000 * (1 - fx) + c100 * fx;
        float x10 = c010 * (1 - fx) + c110 * fx;
        float x01 = c001 * (1 - fx) + c101 * fx;
        float x11 = c011 * (1 - fx) + c111 * fx;

        float y0 = x00 * (1 - fy) + x10 * fy;
        float y1 = x01 * (1 - fy) + x11 * fy;

        return y0 * (1 - fz) + y1 * fz;
    }

    private static float fbm(float x, float y, float z, int octaves, float lacunarity, float gain)
    {
        float value = 0.0f;
        float amplitude = 1.0f;
        float frequency = 1.0f;
        float maxValue = 0.0f;

        for (int i = 0; i < octaves; i++) {
            value += amplitude * noise3D(x * frequency, y * frequency, z * frequency);
            maxValue += amplitude;
            frequency *= lacunarity;
            amplitude *= gain;
        }

        return value / maxValue;
    }

    public static Vector3f generateColor(Vector3f localPos, PlanetBiome biome, float seed)
    {
        Vector3f pos = new Vector3f(localPos).mul(1.0f / biome.surfaceColors().noiseScale1());
        pos.add(seed, seed, seed);

        int octaves = biome.surfaceColors().noiseOctaves();
        float noise1 = fbm(pos.x, pos.y, pos.z, octaves, 2.0f, 0.5f);
        float noise2 = fbm(pos.x * 2.0f, pos.y * 2.0f, pos.z * 2.0f, octaves, 2.0f, 0.5f);
        float noise3 = fbm(pos.x * 0.5f, pos.y * 0.5f, pos.z * 0.5f, octaves, 2.0f, 0.5f);

        noise1 = (noise1 + 1.0f) * 0.5f;
        noise2 = (noise2 + 1.0f) * 0.5f;
        noise3 = (noise3 + 1.0f) * 0.5f;

        float combined = (noise1 + noise2 + noise3) / 3.0f;

        return getBiomeColor(biome, noise1, noise2, noise3, combined, localPos);
    }

    private static Vector3f getBiomeColor(PlanetBiome biome, float n1, float n2, float n3, float combined, Vector3f localPos)
    {
        float[] c1 = biome.surfaceColors().color1();
        float[] c2 = biome.surfaceColors().color2();
        float[] c3 = biome.surfaceColors().color3();

        Vector3f color1 = new Vector3f(c1[0], c1[1], c1[2]);
        Vector3f color2 = new Vector3f(c2[0], c2[1], c2[2]);
        Vector3f color3 = new Vector3f(c3[0], c3[1], c3[2]);

        switch (biome.id()) {
            case PlanetBiome.BIOME_DEFAULT:
                if (combined < biome.landRatio()) {
                    return new Vector3f(color1).lerp(color2, combined / biome.landRatio());
                } else {
                    return color3;
                }

            case PlanetBiome.BIOME_AQUATIC:
                if (n1 < biome.iceCoverage()) {
                    return new Vector3f(color1).lerp(color2, n1 / biome.iceCoverage());
                } else {
                    return color3;
                }

            case PlanetBiome.BIOME_ARCTIC:
                return new Vector3f(color1).lerp(color2, n1).lerp(color3, n2 * 0.3f);

            case PlanetBiome.BIOME_DESERT:
                return new Vector3f(color1).lerp(color2, n1).lerp(color3, n2 * 0.5f);

            case PlanetBiome.BIOME_ROCKY:
                return new Vector3f(color1).lerp(color2, n1).lerp(color3, n3);

            case PlanetBiome.BIOME_ICE:
                return new Vector3f(color1).lerp(color2, n1).lerp(color3, n2 * 0.2f);

            case PlanetBiome.BIOME_GAS_GIANT:
                float bandPos = localPos.y * 5.0f + n1 * 0.5f;
                float band = (bandPos % 1.0f + 1.0f) % 1.0f;
                Vector3f bandColor = new Vector3f(color1).lerp(color2, band);
                return new Vector3f(bandColor).lerp(color3, n2 * 0.3f);

            case PlanetBiome.BIOME_AMMONIA:
                return new Vector3f(color1).lerp(color2, n1).lerp(color3, n2 * 0.4f);

            case PlanetBiome.BIOME_VOLCANIC:
                if (combined > 0.75f) {
                    return new Vector3f(color2).lerp(new Vector3f(1.0f, 0.8f, 0.3f), n1);
                } else {
                    return new Vector3f(color1).lerp(color3, n2);
                }

            default:
                return new Vector3f(color1).lerp(color2, n1).lerp(color3, n2);
        }
    }

    public static Vector3f generateAtmosphereColor(PlanetBiome biome, float fresnel, float sunAlignment)
    {
        float[] atmColor = biome.atmosphereColor();
        Vector3f baseColor = new Vector3f(atmColor[0], atmColor[1], atmColor[2]);

        float dayNight = (sunAlignment + 1.0f) * 0.5f;
        dayNight = Math.max(dayNight, biome.atmosphereSettings().dayNightBlend());

        if (biome.atmosphereSettings().enableScattering()) {
            Vector3f scatter = new Vector3f(0.5f, 0.7f, 1.0f);
            baseColor = new Vector3f(baseColor).lerp(scatter, fresnel * 0.3f);
        }

        return new Vector3f(baseColor).mul(dayNight * biome.atmosphereIntensity());
    }

    public static Vector3f generateCloudColor(PlanetBiome biome, float density)
    {
        float[] cloudCol = biome.cloudColor();
        Vector3f color = new Vector3f(cloudCol[0], cloudCol[1], cloudCol[2]);

        float brightness = 0.7f + density * 0.3f;
        return new Vector3f(color).mul(brightness);
    }

    public static Vector3f generateRingColor(float radius, float angle, float seed)
    {
        float band = (float)Math.sin(radius * 20.0f + seed) * 0.5f + 0.5f;
        float noise = noise3D(radius * 10.0f, angle * 10.0f, seed);
        noise = (noise + 1.0f) * 0.5f;

        Vector3f color1 = new Vector3f(0.8f, 0.7f, 0.6f);
        Vector3f color2 = new Vector3f(0.6f, 0.5f, 0.4f);
        Vector3f color3 = new Vector3f(0.9f, 0.8f, 0.7f);

        Vector3f finalColor = new Vector3f(color1).lerp(color2, band);
        finalColor = new Vector3f(finalColor).lerp(color3, noise * 0.3f);

        return finalColor;
    }
}