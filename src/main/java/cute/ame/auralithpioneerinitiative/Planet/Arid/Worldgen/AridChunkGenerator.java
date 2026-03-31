package cute.ame.auralithpioneerinitiative.Planet.Arid.Worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cute.ame.auralithpioneerinitiative.Registrie.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class AridChunkGenerator extends ChunkGenerator
{
  public static final MapCodec<AridChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
      instance.group(
          BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource),
          com.mojang.serialization.Codec.LONG.fieldOf("seed").forGetter(g -> g.seed)
      ).apply(instance, AridChunkGenerator::new)
  );

  private static final int MIN_Y = -64;
  private static final int MAX_Y = 320;
  private static final int SEA_LEVEL = -63;
  private static final int GEN_DEPTH = 384;
  private static final int BASE_Y = 72;

  private static final int CRATER_CELL = 512;
  private static final int CRATER_CHANCE = 4;
  private static final int CRATER_MIN_R = 60;
  private static final int CRATER_MAX_R = 220;
  private static final float CRATER_MAX_DEPTH = 36f;

  private static final int PLATEAU_Y = 85;
  private static final int CANYON_FLOOR = 42;
  private static final int BLEND_RADIUS = 96;
  private static final int BLEND_STEP = 16;

  private final BiomeSource biomeSource;
  private final long seed;

  public AridChunkGenerator(BiomeSource biomeSource, long seed)
  {
    super(biomeSource);
    this.biomeSource = biomeSource;
    this.seed = seed;
  }

  @Override protected MapCodec<? extends ChunkGenerator> codec() { return CODEC; }
  @Override public int getSeaLevel() { return SEA_LEVEL; }
  @Override public int getGenDepth() { return GEN_DEPTH; }
  @Override public int getMinY() { return MIN_Y; }

  @Override
  public void addDebugScreenInfo(List<String> info, RandomState state, BlockPos pos)
  {
    int biome = getBiomeType(pos.getX(), pos.getZ());
    String[] names = {"Dust Desert","Canyon Maze","Crystal Caverns","Impact Crater"};
    info.add("[Arid] Biome: " + names[Math.min(biome, names.length-1)]);
    info.add("[Arid] Y: " + getSurfaceHeight(pos.getX(), pos.getZ()));
    info.add("[Arid] CanyonVal: " + String.format("%.6f", getCanyonValue(pos.getX(), pos.getZ())));
  }

  @Override
  public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState state)
  {
    return getSurfaceHeight(x, z);
  }

  @Override
  public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState state)
  {
    int sy = getSurfaceHeight(x, z);
    int total = level.getHeight();
    BlockState[] col = new BlockState[total];
    for (int i = 0; i < total; i++)
    {
      int wy = level.getMinBuildHeight() + i;
      if (wy < MIN_Y + 1) col[i] = Blocks.BEDROCK.defaultBlockState();
      else if (wy < sy) col[i] = ModBlocks.ARID_ROCK.get().defaultBlockState();
      else col[i] = Blocks.AIR.defaultBlockState();
    }
    return new NoiseColumn(level.getMinBuildHeight(), col);
  }

  public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk)
  {
    return CompletableFuture.supplyAsync(() ->
    {
      int minCX = chunk.getPos().getMinBlockX();
      int minCZ = chunk.getPos().getMinBlockZ();
      BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
      BlockState rock = ModBlocks.ARID_ROCK.get().defaultBlockState();
      BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();

      for (int lx = 0; lx < 16; lx++)
      {
        int wx = minCX + lx;
        for (int lz = 0; lz < 16; lz++)
        {
          int wz = minCZ + lz;
          int sy = getSurfaceHeight(wx, wz);
          for (int y = MIN_Y; y < sy; y++)
          {
            mpos.set(wx, y, wz);
            boolean bed = y <= MIN_Y + 4 && y < MIN_Y + (int)(AridNoise.hash2(wx, wz, seed) * 5);
            chunk.setBlockState(mpos, bed ? bedrock : rock, false);
          }
        }
      }
      return chunk;
    });
  }

  @Override
  public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState randomState, ChunkAccess chunk)
  {
    int minCX = chunk.getPos().getMinBlockX();
    int minCZ = chunk.getPos().getMinBlockZ();
    BlockState dust = ModBlocks.ARID_DUST.get().defaultBlockState();
    BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();

    for (int lx = 0; lx < 16; lx++)
    {
      int wx = minCX + lx;
      for (int lz = 0; lz < 16; lz++)
      {
        int wz = minCZ + lz;
        int sy = getSurfaceHeight(wx, wz);
        int biome = getBiomeType(wx, wz);

        int dustDepth = (biome == AridBiomeSource.DUST || biome == AridBiomeSource.CRATER) ? 3 : 1;
        for (int d = 0; d < dustDepth; d++)
        {
          mpos.set(wx, sy - 1 - d, wz);
          if (!chunk.getBlockState(mpos).isAir())
            chunk.setBlockState(mpos, dust, false);
        }

        if (biome == AridBiomeSource.CANYON) placeStrataColumn(chunk, wx, wz, sy, mpos);
      }
    }
  }

  @Override
  public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step)
  {
    if (step != GenerationStep.Carving.AIR) return;
    int minCX = chunk.getPos().getMinBlockX();
    int minCZ = chunk.getPos().getMinBlockZ();

    for (int lx = 0; lx < 16; lx += 4)
    {
      int wx = minCX + lx;
      for (int lz = 0; lz < 16; lz += 4)
      {
        int wz = minCZ + lz;
        if (getBiomeType(wx, wz) != AridBiomeSource.CRYSTAL) continue;
        if (((wx & 0x1F) != 0) || ((wz & 0x1F) != 0)) continue;

        float n = AridNoise.fbm(wx * 0.05f, wz * 0.05f, this.seed + 9999L, 2, 0.5f);
        carveSphericalChamber(chunk, wx, 10 + (int)(n * 25), wz, 5 + (int)(n * 5));
      }
    }
  }

  @Override public void spawnOriginalMobs(WorldGenRegion region) {}

  public int getSurfaceHeight(int x, int z)
  {
    int base = blendedBiomeHeight(x, z);
    int craterDep = getCraterDepression(x, z);
    return Math.max(MIN_Y + 5, base - craterDep);
  }

  private int blendedBiomeHeight(int x, int z)
  {
    if (!(biomeSource instanceof AridBiomeSource abs)) return rawBiomeHeight(AridBiomeSource.DUST, x, z);

    double totalW = 0.0;
    double totalH = 0.0;
    double sigma2 = (BLEND_RADIUS * 0.5) * (BLEND_RADIUS * 0.5);

    for (int ox = -BLEND_RADIUS; ox <= BLEND_RADIUS; ox += BLEND_STEP)
    {
      for (int oz = -BLEND_RADIUS; oz <= BLEND_RADIUS; oz += BLEND_STEP)
      {
        int sx = x + ox, sz = z + oz;
        int biome = abs.getBiomeIndex(sx, sz);
        double dist2 = ox * ox + oz * oz;
        double w = Math.exp(-dist2 / (2.0 * sigma2));
        totalH += rawBiomeHeight(biome, sx, sz) * w;
        totalW += w;
      }
    }
    return totalW > 0.0 ? (int)Math.round(totalH / totalW) : BASE_Y;
  }

  private int rawBiomeHeight(int biome, int x, int z)
  {
    return switch (biome)
    {
      case AridBiomeSource.CANYON -> canyonH(x, z);
      case AridBiomeSource.CRYSTAL -> crystalH(x, z);
      default -> dustH(x, z);
    };
  }

  private int dustH(int x, int z)
  {
    float dune = (float)(Math.sin(x * 0.045) * 5.0 + Math.sin(z * 0.038 + x * 0.018) * 4.0);
    float fbm  = AridNoise.fbm(x * 0.025f, z * 0.025f, seed + 2L, 5, 0.52f) * 10f - 5f;
    float detail = AridNoise.fbm(x * 0.10f, z * 0.10f, seed + 20L, 3, 0.45f) * 3f - 1.5f;
    return BASE_Y + (int)(dune + fbm + detail);
  }

  private int canyonH(int x, int z)
  {
    float fx = x * 0.0055f, fz = z * 0.0055f;

    float dwx = AridNoise.fbm(fx + 3.7f, fz + 1.3f, seed + 30L, 3, 0.5f) * 1.8f;
    float dwz = AridNoise.fbm(fx + 8.1f, fz + 5.2f, seed + 31L, 3, 0.5f) * 1.8f;
    float wx = fx + dwx, wz = fz + dwz;

    float main = AridNoise.fbm(wx, wz, seed + 3L, 4, 0.52f);
    float mainDist = Math.abs(main - 0.50f) * 2f;

    float sec = AridNoise.fbm(wx * 2.1f + 15, wz * 2.1f + 22, seed + 40L, 3, 0.50f);
    float secDist = Math.abs(sec - 0.50f) * 2f;
    float canalDist = Math.min(mainDist, secDist * 0.75f + 0.1f);
    float platVar = AridNoise.fbm(x * 0.018f, z * 0.018f, seed + 4L, 3, 0.48f) * 9f - 4.5f;
    int plateau = PLATEAU_Y + (int)platVar;

    if (canalDist < 0.32f)
    {
      float t = 1f - (canalDist / 0.32f);
      t = t * t * (3f - 2f * t);

      float floorVar = AridNoise.fbm(x * 0.04f, z * 0.04f, seed + 50L, 3, 0.45f) * 6f - 3f;
      int floor = CANYON_FLOOR + (int)floorVar;

      return (int)AridNoise.lerp(plateau, floor, t);
    }
    else if (canalDist < 0.48f)
    {
      float t = (canalDist - 0.32f) / 0.16f;
      t = t * t * (3f - 2f * t);
      float rimBump = AridNoise.fbm(x * 0.06f, z * 0.06f, seed + 60L, 2, 0.5f) * 4f;
      return (int)AridNoise.lerp(plateau, plateau + 3 + (int)rimBump, 1f - t);
    }

    return plateau;
  }

  // I fucked up somewhere, but anyways i have another idea for this shit
  private int crystalH(int x, int z)
  {
    float n = AridNoise.fbm(x * 0.030f, z * 0.030f, seed + 5L, 5, 0.50f);
    float detail = AridNoise.fbm(x * 0.08f, z * 0.08f, seed + 51L, 3, 0.45f) * 4f - 2f;
    return 55 + (int)(n * 14f - 7f) + (int)detail;
  }

  private int getCraterDepression(int x, int z)
  {
    int cellX = Math.floorDiv(x, CRATER_CELL);
    int cellZ = Math.floorDiv(z, CRATER_CELL);
    double maxDep = 0.0;

    for (int dcx = -1; dcx <= 1; dcx++)
    {
      for (int dcz = -1; dcz <= 1; dcz++)
      {
        int cx = cellX + dcx;
        int cz = cellZ + dcz;
        long h = hashCell(cx, cz, seed);
        if ((h & 0xFF) >= (256 / CRATER_CHANCE)) continue;

        double jitterX = ((hashCell(cx, cz, seed + 1) & 0xFFFF) / 65535.0 - 0.5);
        double jitterZ = ((hashCell(cx, cz, seed + 2) & 0xFFFF) / 65535.0 - 0.5);
        double crX = cx * CRATER_CELL + CRATER_CELL * 0.5 + jitterX * CRATER_CELL * 0.8;
        double crZ = cz * CRATER_CELL + CRATER_CELL * 0.5 + jitterZ * CRATER_CELL * 0.8;

        int r = CRATER_MIN_R + (int)((hashCell(cx, cz, seed + 3) & 0xFF) / 255.0 * (CRATER_MAX_R - CRATER_MIN_R));

        double dx = x - crX;
        double dz = z - crZ;
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist >= r) continue;

        double t = dist / r;
        double maxD = CRATER_MAX_DEPTH * (r / (double)CRATER_MAX_R);
        double u = 1.0 - t;
        double smooth = u * u * (3.0 - 2.0 * u);
        double depth = maxD * smooth * smooth;

        double noise = ((hashCell((int)x, (int)z, seed + 999) & 0xFF) / 255.0 - 0.5) * 0.15;
        depth *= (1.0 + noise);

        if (t > 0.8 && t < 1.0)
        {
          double rimT = (t - 0.8) / 0.2;
          double rimShape = Math.sin(rimT * Math.PI);
          double rimHeight = -maxD * 0.12 * rimShape;

          depth = Math.min(depth, rimHeight);
        }

        maxDep = Math.max(maxDep, depth);
      }
    }

    return (int)Math.round(maxDep);
  }

  private static long hashCell(int cx, int cz, long seed)
  {
    long h = seed ^ ((long)cx * 0x9E3779B97F4A7C15L) ^ ((long)cz * 0x6C62272E07BB0142L);
    h ^= h >>> 33; h *= 0xFF51AFD7ED558CCDL; h ^= h >>> 33;
    return h;
  }

  private int getBiomeType(int x, int z)
  {
    return (biomeSource instanceof AridBiomeSource abs) ? abs.getBiomeIndex(x, z) : AridBiomeSource.DUST;
  }

  private float getCanyonValue(int x, int z)
  {
    float fx = x * 0.0055f, fz = z * 0.0055f;
    float dwx = AridNoise.fbm(fx + 3.7f, fz + 1.3f, seed + 30L, 3, 0.5f) * 1.8f;
    float dwz = AridNoise.fbm(fx + 8.1f, fz + 5.2f, seed + 31L, 3, 0.5f) * 1.8f;
    float main = AridNoise.fbm(fx + dwx, fz + dwz, seed + 3L, 4, 0.52f);
    return Math.abs(main - 0.50f) * 2f;
  }

  private void placeStrataColumn(ChunkAccess chunk, int wx, int wz, int surfaceY, BlockPos.MutableBlockPos mpos)
  {
    BlockState[] strata =
    {
      ModBlocks.ARID_STRATA_RED.get().defaultBlockState(),
      ModBlocks.ARID_STRATA_ORANGE.get().defaultBlockState(),
      ModBlocks.ARID_STRATA_WHITE.get().defaultBlockState(),
      ModBlocks.ARID_STRATA_MAROON.get().defaultBlockState()
    };
    int yMin = Math.max(CANYON_FLOOR - 2, MIN_Y + 5);
    int yMax = Math.min(PLATEAU_Y + 2, surfaceY - 1);
    for (int y = yMin; y < yMax; y++)
    {
      float wave = AridNoise.fbm(wx * 0.08f, wz * 0.08f, seed + 7L + y * 3, 2, 0.5f) * 3f;
      int idx = ((y + (int)wave) / 4) & 3;
      mpos.set(wx, y, wz);
      if (!chunk.getBlockState(mpos).isAir()) chunk.setBlockState(mpos, strata[idx], false);
    }
  }

  private void carveSphericalChamber(ChunkAccess chunk, int cx, int cy, int cz, int r)
  {
    int minCX = chunk.getPos().getMinBlockX();
    int minCZ = chunk.getPos().getMinBlockZ();
    BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
    for (int dx = -r; dx <= r; dx++)
    {
      int bx = cx + dx;
      if (bx < minCX || bx >= minCX + 16) continue;
      for (int dz = -r; dz <= r; dz++)
      {
        int bz = cz + dz;
        if (bz < minCZ || bz >= minCZ + 16) continue;
        for (int dy = -r; dy <= r; dy++)
        {
          if (dx*dx + dy*dy + dz*dz > r*r) continue;
          int by = cy + dy;
          if (by < MIN_Y || by >= MAX_Y) continue;
          mpos.set(bx, by, bz);
          chunk.setBlockState(mpos, Blocks.AIR.defaultBlockState(), false);
        }
      }
    }
  }
}