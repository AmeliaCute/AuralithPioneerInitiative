package cute.ame.auralithpioneerinitiative.SkyPlanet.Texture;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import cute.ame.auralithpioneerinitiative.SkyPlanet.Data.ProceduralPlanetConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public final class PlanetTextureHelper
{
  private PlanetTextureHelper() {}
  private static final Logger LOGGER = LogUtils.getLogger();
  private static final Map<String, CubemapTextures> CACHE = new HashMap<>();

  public static final int FACE_FRONT = 0;
  public static final int FACE_BACK = 1;
  public static final int FACE_LEFT = 2;
  public static final int FACE_RIGHT = 3;
  public static final int FACE_TOP = 4;
  public static final int FACE_BOTTOM = 5;

  public record CubemapTextures(
      ResourceLocation front,
      ResourceLocation back,
      ResourceLocation left,
      ResourceLocation right,
      ResourceLocation top,
      ResourceLocation bottom)
  {
    public ResourceLocation get(int face)
    {
      return switch (face)
      {
        case FACE_FRONT -> front;
        case FACE_BACK -> back;
        case FACE_LEFT -> left;
        case FACE_RIGHT -> right;
        case FACE_TOP -> top;
        case FACE_BOTTOM -> bottom;
        default -> throw new IllegalArgumentException("Invalid face: " + face);
      };
    }
  }

  public static CubemapTextures getOrGenerate(ProceduralPlanetConfig cfg, String planetId)
  {
    String cacheKey = planetId + "@" + cfg.seed() + "_" + cfg.type().name();
    CubemapTextures cached = CACHE.get(cacheKey);
    if (cached != null) return cached;

    int res = Math.max(32, Math.min(cfg.resolution(), 64));
    LOGGER.debug("[Auralith] Generating {} cubemap for '{}' ({}x{} per face, seed={})", cfg.type(), planetId, res, res, cfg.seed());

    NativeImage frontImg = generateFace(cfg, res, FACE_FRONT);
    NativeImage backImg = generateFace(cfg, res, FACE_BACK);
    NativeImage leftImg = generateFace(cfg, res, FACE_LEFT);
    NativeImage rightImg = generateFace(cfg, res, FACE_RIGHT);
    NativeImage topImg = generateFace(cfg, res, FACE_TOP);
    NativeImage bottomImg = generateFace(cfg, res, FACE_BOTTOM);

    ResourceLocation front = register(frontImg, planetId, "front");
    ResourceLocation back = register(backImg, planetId, "back");
    ResourceLocation left = register(leftImg, planetId, "left");
    ResourceLocation right = register(rightImg, planetId, "right");
    ResourceLocation top = register(topImg, planetId, "top");
    ResourceLocation bottom = register(bottomImg, planetId, "bottom");

    CubemapTextures result = new CubemapTextures(front, back, left, right, top, bottom);
    CACHE.put(cacheKey, result);
    return result;
  }

  public static void invalidateAll() { CACHE.clear(); }

  private static ResourceLocation register(NativeImage image, String planetId, String faceName)
  {
    DynamicTexture dynTex = new DynamicTexture(image);
    String safeName = "generated/planet/" + planetId.replace(':', '_').replace('/', '_') + "_" + faceName;
    ResourceLocation rl = ResourceLocation.fromNamespaceAndPath("auralithpioneerinitiative", safeName);
    Minecraft.getInstance().getTextureManager().register(rl, dynTex);
    GlStateManager._bindTexture(dynTex.getId());
    GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
    GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
    GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
    GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
    GlStateManager._bindTexture(0);
    return rl;
  }

  private static void faceDir(int face, int x, int y, int res, float[] out)
  {
    float invRes = 1f / res;
    float u = (x + 0.5f) * 2f * invRes - 1f;
    float v = (y + 0.5f) * 2f * invRes - 1f;
    float dx, dy, dz;
    switch (face)
    {
      case FACE_FRONT -> { dx =  u; dy = -v; dz =  1f; }
      case FACE_BACK -> { dx = -u; dy = -v; dz = -1f; }
      case FACE_RIGHT -> { dx =  1f; dy = -v; dz = -u; }
      case FACE_LEFT -> { dx = -1f; dy = -v; dz =  u; }
      case FACE_TOP -> { dx =  u; dy =  1f; dz =  v; }
      case FACE_BOTTOM -> { dx =  u; dy = -1f; dz = -v; }
      default -> throw new IllegalArgumentException("face=" + face);
    }
    float invLen = 1f / (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    out[0] = dx * invLen;
    out[1] = dy * invLen;
    out[2] = dz * invLen;
  }

  private static NativeImage generateFace(ProceduralPlanetConfig cfg, int res, int face)
  {
    return switch (cfg.type())
    {
      case ROCKY -> genRocky(cfg, res, face);
      case GAS_GIANT -> genGasGiant(cfg, res, face);
      case OCEAN -> genOcean(cfg, res, face);
      case ICE -> genIce(cfg, res, face);
      case LAVA -> genLava(cfg, res, face);
      case TELLURIC -> genTelluric(cfg, res, face);
    };
  }

  private static NativeImage genRocky(ProceduralPlanetConfig cfg, int res, int face)
  {
    NativeImage img = new NativeImage(res, res, false);
    long seed = cfg.seed(); int oct = cfg.octaves(); float rough = cfg.roughness();
    float[] d = new float[3];
    for (int y = 0; y < res; y++)
    {
      for (int x = 0; x < res; x++)
      {
        faceDir(face, x, y, res, d);
        float dx = d[0], dy = d[1], dz = d[2];
        float height  = fbm3(dx * 3f, dy * 3f,  dz * 3f,  seed, oct, rough);
        float craterN = fbm3(dx * 12f, dy * 12f, dz * 12f, seed + 777L,  3, 0.40f);
        height = clamp01(height + Math.max(0, 0.08f - craterN) * 4f * 0.3f);
        img.setPixelRGBA(x, y, toRGBA(lerp(cfg.sr(), cfg.pr(), height), lerp(cfg.sg(), cfg.pg(), height), lerp(cfg.sb(), cfg.pb(), height), 1f));
      }
    }
    return img;
  }

  private static NativeImage genGasGiant(ProceduralPlanetConfig cfg, int res, int face)
  {
    NativeImage img = new NativeImage(res, res, false);
    long seed = cfg.seed();
    float numBands = 7f + pseudoRandom3(0, 0, 0, seed) * 5f;
    float[] d = new float[3];
    for (int y = 0; y < res; y++)
    {
      for (int x = 0; x < res; x++)
      {
        faceDir(face, x, y, res, d);
        float dx = d[0], dy = d[1], dz = d[2];
        float lat = (dy + 1f) * 0.5f;
        float turb = fbm3(dx * 3f, dy * 4f, dz * 3f, seed + 1L, 5, 0.55f) * 0.25f;
        float band = (float) Math.sin((lat + turb) * Math.PI * numBands);
        float t = clamp01((band + 1f) * 0.5f + fbm3(dx * 5f + turb, dy * 5f, dz * 5f, seed + 99L, 4, 0.5f) * 0.30f - 0.15f);
        img.setPixelRGBA(x, y, toRGBA(lerp(cfg.sr(), cfg.pr(), t), lerp(cfg.sg(), cfg.pg(), t), lerp(cfg.sb(), cfg.pb(), t), 1f));
      }
    }
    return img;
  }

  private static NativeImage genOcean(ProceduralPlanetConfig cfg, int res, int face)
  {
    NativeImage img = new NativeImage(res, res, false);
    long seed = cfg.seed(); int oct = cfg.octaves(); float rough = cfg.roughness();
    final float seaLevel = 0.78f;
    float[] d = new float[3];
    for (int y = 0; y < res; y++)
    {
      for (int x = 0; x < res; x++)
      {
        faceDir(face, x, y, res, d);
        float dx = d[0], dy = d[1], dz = d[2];
        float height = fbm3(dx * 2.5f, dy * 2.5f, dz * 2.5f, seed,        oct, rough);
        float depth  = fbm3(dx * 6f,   dy * 6f,   dz * 6f,   seed + 500L, 3,   0.45f) * 0.08f;
        float r, g, b;
        if (height < seaLevel)
        {
          float depthT = 1f - (height / seaLevel);
          r = lerp(cfg.pr(), cfg.pr() * 0.3f, depthT);
          g = lerp(cfg.pg(), cfg.pg() * 0.4f, depthT);
          b = lerp(cfg.pb(), cfg.pb() * 0.6f, depthT);
        }
        else
        {
          float t = (height - seaLevel) / (1f - seaLevel);
          r = lerp(cfg.pr() * 0.8f, cfg.sr(), t + depth);
          g = lerp(cfg.pg() * 0.9f, cfg.sg(), t + depth);
          b = lerp(cfg.pb() * 0.5f, cfg.sb(), t + depth);
        }
        img.setPixelRGBA(x, y, toRGBA(r, g, b, 1f));
      }
    }
    return img;
  }

  private static NativeImage genIce(ProceduralPlanetConfig cfg, int res, int face)
  {
    NativeImage img = new NativeImage(res, res, false);
    long seed = cfg.seed();
    float[] d = new float[3];
    for (int y = 0; y < res; y++)
    {
      for (int x = 0; x < res; x++)
      {
        faceDir(face, x, y, res, d);
        float dx = d[0], dy = d[1], dz = d[2];
        float base  = fbm3(dx * 4f, dy * 4f, dz * 4f, seed, 6, 0.50f);
        float warpX = fbm3(dx * 8f, dy * 8f, dz * 8f, seed + 11L, 3, 0.5f) * 0.15f;
        float warpY = fbm3(dx * 8f + 5.2f, dy * 8f + 1.3f, dz * 8f + 2.7f, seed + 22L, 3, 0.5f) * 0.15f;
        float crack = fbm3((dx + warpX) * 16f, (dy + warpY) * 16f, (dz + warpX) * 16f, seed + 33L, 2, 0.4f);
        float brightness = clamp01(base * 0.4f + 0.6f - Math.max(0, 0.12f - crack) / 0.12f * 0.5f);
        img.setPixelRGBA(x, y, toRGBA(lerp(cfg.sr(), cfg.pr(), brightness), lerp(cfg.sg(), cfg.pg(), brightness), lerp(cfg.sb(), cfg.pb(), brightness), 1f));
      }
    }
    return img;
  }

  private static NativeImage genLava(ProceduralPlanetConfig cfg, int res, int face)
  {
    NativeImage img = new NativeImage(res, res, false);
    long seed = cfg.seed(); int oct = cfg.octaves(); float rough = cfg.roughness();
    float[] d = new float[3];
    for (int y = 0; y < res; y++)
    {
      for (int x = 0; x < res; x++)
      {
        faceDir(face, x, y, res, d);
        float dx = d[0], dy = d[1], dz = d[2];
        float base = fbm3(dx * 3f, dy * 3f, dz * 3f, seed, oct, rough);
        float veinMask = clamp01(Math.max(0, 0.35f - base) * 5f);
        float glow = clamp01(veinMask + fbm3(dx * 8f, dy * 8f, dz * 8f, seed + 999L, 3, 0.6f) * 0.3f * veinMask);
        img.setPixelRGBA(x, y, toRGBA(lerp(cfg.sr(), cfg.pr(), glow), lerp(cfg.sg(), cfg.pg(), glow * 0.6f), lerp(cfg.sb(), cfg.pb(), glow * 0.1f), 1f));
      }
    }
    return img;
  }

  private static NativeImage genTelluric(ProceduralPlanetConfig cfg, int res, int face)
  {
    NativeImage img = new NativeImage(res, res, false);
    long seed = cfg.seed();
    int oct = cfg.octaves();
    float rough = cfg.roughness();

    float seaLevel = 0.42f + pseudoRandom3(1, 0, 0, seed) * 0.12f;
    float polarStart = 0.55f + pseudoRandom3(2, 0, 0, seed) * 0.15f;
    float polarEnd = 0.85f + pseudoRandom3(3, 0, 0, seed) * 0.10f;

    float[] d = new float[3];
    for (int y = 0; y < res; y++)
    {
      for (int x = 0; x < res; x++)
      {
        faceDir(face, x, y, res, d);
        float dx = d[0], dy = d[1], dz = d[2];

        float absDy = Math.abs(dy);
        float iceBlend = smoothStep(clamp01((absDy - polarStart) / (polarEnd - polarStart)));

        float warpScale = 1.2f;
        float wx = fbm3(dx * warpScale, dy * warpScale, dz * warpScale, seed + 11L, 3, 0.5f) * 0.55f;
        float wy = fbm3(dx * warpScale + 3.7f, dy * warpScale + 1.3f, dz * warpScale + 2.8f, seed + 22L, 3, 0.5f) * 0.55f;
        float wz = fbm3(dx * warpScale + 7.1f, dy * warpScale + 5.4f, dz * warpScale + 4.2f, seed + 33L, 3, 0.5f) * 0.55f;

        float continentScale = 1.3f;
        float landBase = fbm3((dx + wx) * continentScale, (dy + wy) * continentScale, (dz + wz) * continentScale, seed, oct, rough);

        float landMask = smoothStep(clamp01((landBase - seaLevel) / 0.08f));

        float detail = fbm3(dx * 5f, dy * 5f, dz * 5f, seed + 1L, 3, 0.45f) * 0.10f;

        float r, g, b;
        if (landMask < 0.05f)
        {
          float depth = clamp01(1f - landBase / seaLevel);
          r = lerp(cfg.pr(), cfg.pr() * 0.35f, depth);
          g = lerp(cfg.pg(), cfg.pg() * 0.40f, depth);
          b = lerp(cfg.pb(), cfg.pb() * 0.60f, depth);
        }
        else if (landMask < 0.15f)
        {
          float t = landMask / 0.15f;
          r = lerp(cfg.pr(), cfg.pr() * 1.15f, t);
          g = lerp(cfg.pg(), cfg.sg() * 1.10f, t);
          b = lerp(cfg.pb(), cfg.sb() * 0.45f, t);
        }
        else
        {
          float elev = clamp01(landBase - seaLevel + detail);
          if (elev < 0.18f)
          {
            r = cfg.sr(); g = cfg.sg(); b = cfg.sb();
          }
          else if (elev < 0.40f)
          {
            float t = (elev - 0.18f) / 0.22f;
            r = lerp(cfg.sr(), cfg.sr() * 0.65f, t);
            g = lerp(cfg.sg(), cfg.sg() * 0.60f, t);
            b = lerp(cfg.sb(), cfg.sb() * 0.50f, t);
          }
          else
          {
            float t = clamp01((elev - 0.40f) / 0.30f);
            r = lerp(0.45f, 0.88f, t);
            g = lerp(0.42f, 0.88f, t);
            b = lerp(0.38f, 0.90f, t);
          }
        }

        float cloud = fbm3(dx * 3.5f + 3.7f, dy * 3.5f + 1.2f, dz * 3.5f + 2.1f, seed + 8192L, 4, 0.50f);
        float cloudAmt = clamp01((cloud - 0.50f) / 0.18f) * 0.45f;
        r = lerp(r, 0.96f, cloudAmt);
        g = lerp(g, 0.96f, cloudAmt);
        b = lerp(b, 0.97f, cloudAmt);

        r = lerp(r, 0.90f, iceBlend);
        g = lerp(g, 0.93f, iceBlend);
        b = lerp(b, 1.00f, iceBlend);

        img.setPixelRGBA(x, y, toRGBA(r, g, b, 1f));
      }
    }
    return img;
  }

  private static float pseudoRandom3(int x, int y, int z, long seed)
  {
    long h = seed ^ (long) x * 0x9E3779B97F4A7C15L ^ (long) y * 0x6C62272E07BB0142L ^ (long) z * 0xD1B54A32D192ED03L;
    h ^= h >>> 33; h *= 0xFF51AFD7ED558CCDL;
    h ^= h >>> 33; h *= 0xC4CEB9FE1A85EC53L;
    h ^= h >>> 33;
    return (float) (h & 0x7FFF_FFFFL) / (float) 0x7FFF_FFFFL;
  }

  private static float valueNoise3(float x, float y, float z, long seed)
  {
    int ix = (int) Math.floor(x), iy = (int) Math.floor(y), iz = (int) Math.floor(z);
    float fx = x - ix, fy = y - iy, fz = z - iz;
    float ux = fx * fx * (3f - 2f * fx);
    float uy = fy * fy * (3f - 2f * fy);
    float uz = fz * fz * (3f - 2f * fz);

    float c000 = pseudoRandom3(ix, iy, iz, seed);
    float c100 = pseudoRandom3(ix + 1, iy, iz, seed);
    float c010 = pseudoRandom3(ix, iy + 1, iz, seed);
    float c110 = pseudoRandom3(ix + 1, iy + 1, iz, seed);
    float c001 = pseudoRandom3(ix, iy, iz + 1, seed);
    float c101 = pseudoRandom3(ix + 1, iy, iz + 1, seed);
    float c011 = pseudoRandom3(ix, iy + 1, iz + 1, seed);
    float c111 = pseudoRandom3(ix + 1, iy + 1, iz + 1, seed);

    float x00 = lerp(c000, c100, ux);
    float x10 = lerp(c010, c110, ux);
    float x01 = lerp(c001, c101, ux);
    float x11 = lerp(c011, c111, ux);
    float y0 = lerp(x00, x10, uy);
    float y1 = lerp(x01, x11, uy);
    return lerp(y0, y1, uz);
  }

  static float fbm3(float x, float y, float z, long seed, int octaves, float roughness)
  {
    float v = 0, a = 0.5f, f = 1f, n = 0;
    for (int i = 0; i < octaves; i++)
    {
      v += valueNoise3(x * f, y * f, z * f, seed + i * 13337L) * a;
      n += a; a *= roughness; f *= 2;
    }
    return v / n;
  }

  private static float smoothStep(float t) { return t * t * (3f - 2f * t); }
  private static float lerp(float a, float b, float t) { return a + (b - a) * t; }
  private static float clamp01(float v) { return Math.max(0f, Math.min(1f, v)); }

  private static int toRGBA(float r, float g, float b, float a)
  {
    return ((int) (clamp01(a) * 255f) << 24) | ((int) (clamp01(b) * 255f) << 16) | ((int) (clamp01(g) * 255f) << 8) | (int) (clamp01(r) * 255f);
  }
}