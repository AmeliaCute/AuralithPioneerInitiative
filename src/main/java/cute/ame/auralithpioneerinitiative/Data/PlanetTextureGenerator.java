package cute.ame.auralithpioneerinitiative.Data;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public final class PlanetTextureGenerator
{
    private PlanetTextureGenerator() {}
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, ResourceLocation> CACHE = new HashMap<>();

    public static ResourceLocation getOrGenerate(ProceduralPlanetConfig cfg, String planetId)
    {
      String cacheKey = planetId + "@" + cfg.seed() + "_" + cfg.type().name();
      ResourceLocation cached = CACHE.get(cacheKey);
      if (cached != null) return cached;

      int res = Math.max(32, Math.min(cfg.resolution(), 64));
      LOGGER.debug("[Auralith] Generating {} texture for '{}' ({}x{}, seed={})", cfg.type(), planetId, res, res, cfg.seed());

      NativeImage image = generate(cfg, res);
      DynamicTexture dynTex = new DynamicTexture(image);
      String safeName = "generated/planet/" + planetId.replace(':', '_').replace('/', '_');
      ResourceLocation rl = ResourceLocation.fromNamespaceAndPath("auralithpioneerinitiative", safeName);

      Minecraft.getInstance().getTextureManager().register(rl, dynTex);
      GlStateManager._bindTexture(dynTex.getId());
      GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
      GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
      GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
      GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
      GlStateManager._bindTexture(0);

      CACHE.put(cacheKey, rl);
      LOGGER.debug("[Auralith] Registered procedural texture at {}", rl);
      return rl;
    }

    public static void invalidateAll() { CACHE.clear(); }

    private static NativeImage generate(ProceduralPlanetConfig cfg, int res)
    {
      return switch (cfg.type())
      {
        case ROCKY     -> generateRocky(cfg, res);
        case GAS_GIANT -> generateGasGiant(cfg, res);
        case OCEAN     -> generateOcean(cfg, res);
        case ICE       -> generateIce(cfg, res);
        case LAVA      -> generateLava(cfg, res);
        case TELLURIC  -> generateTelluric(cfg, res);
      };
    }

    private static NativeImage generateRocky(ProceduralPlanetConfig cfg, int res)
    {
      NativeImage img = new NativeImage(res, res, false);
      long seed = cfg.seed(); int oct = cfg.octaves(); float rough = cfg.roughness();
      for (int y = 0; y < res; y++) for (int x = 0; x < res; x++)
      {
        float u = (float)x/res, v = (float)y/res;
        float height = fbm(u*3f,v*3f,seed,oct,rough);
        float craterN = fbm(u*12f,v*12f,seed+777L,3,0.40f);
        height = clamp01(height + Math.max(0,0.08f-craterN)*4f*0.3f);
        img.setPixelRGBA(x,y,toRGBA(lerp(cfg.sr(),cfg.pr(),height),lerp(cfg.sg(),cfg.pg(),height),lerp(cfg.sb(),cfg.pb(),height),1f));
      }
      return img;
    }

    private static NativeImage generateGasGiant(ProceduralPlanetConfig cfg, int res)
    {
      NativeImage img = new NativeImage(res, res, false);
      long seed = cfg.seed();
      float numBands = 7f + pseudoRandom(0,0,seed)*5f;
      for (int y = 0; y < res; y++) for (int x = 0; x < res; x++)
      {
        float u = (float)x/res, v = (float)y/res;
        float turb = fbm(u*3f,v*4f,seed+1L,5,0.55f)*0.25f;
        float t = clamp01(((float)Math.sin((v+turb)*Math.PI*numBands)+1f)*0.5f + fbm(u*5f+turb,v*5f,seed+99L,4,0.5f)*0.30f - 0.15f);
        img.setPixelRGBA(x,y,toRGBA(lerp(cfg.sr(),cfg.pr(),t),lerp(cfg.sg(),cfg.pg(),t),lerp(cfg.sb(),cfg.pb(),t),1f));
      }

      return img;
    }

    private static NativeImage generateOcean(ProceduralPlanetConfig cfg, int res) {
      NativeImage img = new NativeImage(res, res, false);
      long seed = cfg.seed(); int oct = cfg.octaves(); float rough = cfg.roughness();
      float seaLevel = 0.78f;
      for (int y = 0; y < res; y++) for (int x = 0; x < res; x++)
      {
        float u = (float)x/res, v = (float)y/res;
        float height = fbm(u*2.5f,v*2.5f,seed,oct,rough);
        float depth = fbm(u*6f,v*6f,seed+500L,3,0.45f)*0.08f;
        float r,g,b;
        if (height < seaLevel)
        {
          float d = 1f-(height/seaLevel);
          r=lerp(cfg.pr(),cfg.pr()*0.3f,d); g=lerp(cfg.pg(),cfg.pg()*0.4f,d); b=lerp(cfg.pb(),cfg.pb()*0.6f,d);
        }
        else
        {
          float t=(height-seaLevel)/(1f-seaLevel);
          r=lerp(cfg.pr()*0.8f,cfg.sr(),t+depth); g=lerp(cfg.pg()*0.9f,cfg.sg(),t+depth); b=lerp(cfg.pb()*0.5f,cfg.sb(),t+depth);
        }
        img.setPixelRGBA(x,y,toRGBA(r,g,b,1f));
      }

      return img;
    }

    private static NativeImage generateIce(ProceduralPlanetConfig cfg, int res) {
      NativeImage img = new NativeImage(res, res, false);
      long seed = cfg.seed();
      for (int y = 0; y < res; y++) for (int x = 0; x < res; x++)
      {
        float u = (float)x/res, v = (float)y/res;
        float base  = fbm(u*4f,v*4f,seed,6,0.50f);
        float warpX = fbm(u*8f,v*8f,seed+11L,3,0.5f)*0.15f;
        float warpY = fbm(u*8f+5.2f,v*8f+1.3f,seed+22L,3,0.5f)*0.15f;
        float crack = fbm((u+warpX)*16f,(v+warpY)*16f,seed+33L,2,0.4f);
        float brightness = clamp01(base*0.4f+0.6f - Math.max(0,0.12f-crack)/0.12f*0.5f);
        img.setPixelRGBA(x,y,toRGBA(lerp(cfg.sr(),cfg.pr(),brightness),lerp(cfg.sg(),cfg.pg(),brightness),lerp(cfg.sb(),cfg.pb(),brightness),1f));
      }

      return img;
    }

    private static NativeImage generateLava(ProceduralPlanetConfig cfg, int res)
    {
      NativeImage img = new NativeImage(res, res, false);
      long seed = cfg.seed(); int oct = cfg.octaves(); float rough = cfg.roughness();
      for (int y = 0; y < res; y++) for (int x = 0; x < res; x++)
      {
        float u = (float)x/res, v = (float)y/res;
        float base = fbm(u*3f,v*3f,seed,oct,rough);
        float veinMask = clamp01(Math.max(0,0.35f-base)*5f);
        float glow = clamp01(veinMask + fbm(u*8f,v*8f,seed+999L,3,0.6f)*0.3f*veinMask);
        img.setPixelRGBA(x,y,toRGBA(lerp(cfg.sr(),cfg.pr(),glow),lerp(cfg.sg(),cfg.pg(),glow*0.6f),lerp(cfg.sb(),cfg.pb(),glow*0.1f),1f));
      }

      return img;
    }

    private static NativeImage generateTelluric(ProceduralPlanetConfig cfg, int res)
    {
      NativeImage img  = new NativeImage(res, res, false);
      long seed = cfg.seed();
      int  oct = cfg.octaves();
      float rough = cfg.roughness();

      float seaLevel = 0.40f + (pseudoRandom(1, 0, seed) * 0.20f);
      float polarEdge = 0.12f + pseudoRandom(2, 0, seed) * 0.10f;
      float polarFade = 0.10f;

      for (int y = 0; y < res; y++)
      {
        float v = (float) y / res;
        float distFromPole = Math.min(v, 1f - v) * 2f;
        float iceBlend = clamp01(1f - (distFromPole - 0f) / (polarEdge + polarFade));
        iceBlend = smoothStep(iceBlend);

        for (int x = 0; x < res; x++)
        {
          float u = (float) x / res;

          float continents = fbm(u * 2.5f, v * 2.5f, seed, oct, rough);
          float detail = fbm(u * 8.0f, v * 8.0f, seed + 1L, 3, 0.45f) * 0.15f;
          float elevation = clamp01(continents + detail);
          float r, g, b;

          if (elevation < seaLevel - 0.12f)
          {
            float depth = clamp01((seaLevel - 0.12f - elevation) / 0.30f);
            r = lerp(cfg.pr(), cfg.pr() * 0.40f, depth);
            g = lerp(cfg.pg(), cfg.pg() * 0.45f, depth);
            b = lerp(cfg.pb(), cfg.pb() * 0.65f, depth);
          }
          else if (elevation < seaLevel)
          {
            r = cfg.pr(); g = cfg.pg(); b = cfg.pb();
          }
          else if (elevation < seaLevel + 0.04f)
          {
            float t = (elevation - seaLevel) / 0.04f;
            r = lerp(cfg.pr() * 1.1f, cfg.sr() * 1.3f, t);
            g = lerp(cfg.pg() * 1.0f, cfg.sg() * 1.1f, t);
            b = lerp(cfg.pb() * 0.5f, cfg.sb() * 0.5f, t);
          }
          else if (elevation < seaLevel + 0.35f)
          {
            r = cfg.sr(); g = cfg.sg(); b = cfg.sb();
          }
          else if (elevation < seaLevel + 0.60f)
          {
            float t = (elevation - seaLevel - 0.35f) / 0.25f;
            r = lerp(cfg.sr(), cfg.sr() * 0.55f, t);
            g = lerp(cfg.sg(), cfg.sg() * 0.50f, t);
            b = lerp(cfg.sb(), cfg.sb() * 0.40f, t);
          }
          else
          {
            float t = clamp01((elevation - seaLevel - 0.60f) / 0.30f);
            r = lerp(0.42f, 0.82f, t);
            g = lerp(0.40f, 0.82f, t);
            b = lerp(0.36f, 0.82f, t);
          }

          float cloud = fbm(u * 5.0f + 3.7f, v * 5.0f + 1.2f, seed + 8192L, 4, 0.50f);
          float cloudAmt = clamp01((cloud - 0.52f) / 0.20f) * 0.50f;
          r = lerp(r, 0.96f, cloudAmt);
          g = lerp(g, 0.96f, cloudAmt);
          b = lerp(b, 0.97f, cloudAmt);

          r = lerp(r, 0.92f, iceBlend);
          g = lerp(g, 0.94f, iceBlend);
          b = lerp(b, 1.00f, iceBlend);

          img.setPixelRGBA(x, y, toRGBA(r, g, b, 1f));
        }
      }

      return img;
    }

    private static float pseudoRandom(int x, int y, long seed)
    {
      long h = seed ^ (long)x*0x9E3779B97F4A7C15L ^ (long)y*0x6C62272E07BB0142L;
      h ^= h>>>33; h*=0xFF51AFD7ED558CCDL; h ^= h>>>33; h*=0xC4CEB9FE1A85EC53L; h ^= h>>>33;
      return (float)(h&0x7FFF_FFFFL)/(float)0x7FFF_FFFFL;
    }

    private static float valueNoise(float x, float y, long seed)
    {
      int ix=(int)Math.floor(x), iy=(int)Math.floor(y);
      float fx=x-ix, fy=y-iy;
      float ux=fx*fx*(3f-2f*fx), uy=fy*fy*(3f-2f*fy);
      return lerp(lerp(pseudoRandom(ix,iy,seed),pseudoRandom(ix+1,iy,seed),ux), lerp(pseudoRandom(ix,iy+1,seed),pseudoRandom(ix+1,iy+1,seed),ux),uy);
    }

    static float fbm(float x, float y, long seed, int octaves, float roughness)
    {
      float v=0,a=0.5f,f=1f,n=0;
      for(int i=0;i<octaves;i++){v+=valueNoise(x*f,y*f,seed+i*13337L)*a;n+=a;a*=roughness;f*=2;}
      return v/n;
    }

    private static float smoothStep(float t) { return t * t * (3f - 2f * t); }
    private static float lerp(float a,float b,float t){return a+(b-a)*t;}
    private static float clamp01(float v){return Math.max(0f,Math.min(1f,v));}

    private static int toRGBA(float r,float g,float b,float a)
    {
      return ((int)(clamp01(a)*255f)<<24)|((int)(clamp01(b)*255f)<<16)|((int)(clamp01(g)*255f)<<8)|(int)(clamp01(r)*255f);
    }
}
