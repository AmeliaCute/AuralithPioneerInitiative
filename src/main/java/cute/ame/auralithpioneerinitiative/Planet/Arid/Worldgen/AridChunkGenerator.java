package cute.ame.auralithpioneerinitiative.Planet.Arid.Worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cute.ame.auralithpioneerinitiative.Registrie.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
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

  private static final int MIN_Y  = -64;
  private static final int MAX_Y = 320;
  private static final int SEA_LEVEL = -63;
  private static final int GEN_DEPTH =  384;
  private static final int BASE_Y =  80;

  private static final int CRATER_CELL = 512;
  private static final int CRATER_CHANCE = 37;
  private static final int CRATER_MIN_R = 50;
  private static final int CRATER_MAX_R = 200;
  private static final int CRATER_MAX_DEPTH= 38;
  private static final int TERRACE_STEP = 24;

  private static final int PLATEAU_Y = 60;
  private static final int CANYON_FLOOR = 37;
  private static final int BLEND_RADIUS = 80;

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
    int biomeIdx = getBiomeType(pos.getX(), pos.getZ());
    String[] names = {"Eroded Flats","Dust Desert","Canyon Maze","Crystal Caverns","Impact Crater"};
    info.add("[Arid] Biome: " + names[Math.min(biomeIdx, names.length-1)]);
    info.add("[Arid] Surface Y: " + getSurfaceHeight(pos.getX(), pos.getZ()));
  }

  @Override
  public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState state)
  {
    return getSurfaceHeight(x, z);
  }

  @Override
  public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState state)
  {
    int surfaceY  = getSurfaceHeight(x, z);
    int totalHeight = level.getHeight();
    BlockState[] column = new BlockState[totalHeight];
    for (int i = 0; i < totalHeight; i++)
    {
      int worldY = level.getMinBuildHeight() + i;
      if (worldY < MIN_Y + 1) column[i] = Blocks.BEDROCK.defaultBlockState();
      else if (worldY < surfaceY) column[i] = ModBlocks.ARID_ROCK.get().defaultBlockState();
      else column[i] = Blocks.AIR.defaultBlockState();
    }
    return new NoiseColumn(level.getMinBuildHeight(), column);
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
          int surfaceY = getSurfaceHeight(wx, wz);

          for (int y = MIN_Y; y < surfaceY; y++)
          {
            mpos.set(wx, y, wz);
            boolean bed = y <= MIN_Y + 4 && y < MIN_Y + (int)(AridNoise.hash2(wx, wz, seed) * 5);
            chunk.setBlockState(mpos, bed ? bedrock : rock, false);
          }
        }
      }
      return chunk;
    }); // MAYBE NEED AN EXECUTOR BUT NOT SURE
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
        int surfaceY = getSurfaceHeight(wx, wz);
        int biomeType = getBiomeType(wx, wz);

        int dustDepth = switch (biomeType) {
          case AridBiomeSource.DUST, AridBiomeSource.CRATER -> 3;
          default -> 1;
        };

        for (int d = 0; d < dustDepth; d++)
        {
          mpos.set(wx, surfaceY - 1 - d, wz);
          if (!chunk.getBlockState(mpos).isAir())
            chunk.setBlockState(mpos, dust, false);
        }

        if (biomeType == AridBiomeSource.CANYON) placeStrataColumn(chunk, wx, wz, surfaceY, mpos);
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
        int chamberY = 10 + (int)(n * 25);
        int radius   = 5  + (int)(n * 5);
        carveSphericalChamber(chunk, wx, chamberY, wz, radius);
      }
    }
  }

  @Override
  public void spawnOriginalMobs(WorldGenRegion region) {}

  public int getSurfaceHeight(int x, int z)
  {
    int base = blendedBiomeHeight(x, z);
    int craterDepress = getCraterDepression(x, z);
    return Math.max(MIN_Y + 5, base - craterDepress);
  }

  private int blendedBiomeHeight(int x, int z)
  {
    if (!(biomeSource instanceof AridBiomeSource abs))
      return rawBiomeHeight(AridBiomeSource.DUST, x, z);

    float totalWeight = 0f;
    float weightedH   = 0f;
    int step = BLEND_RADIUS / 2;

    for (int ox = -BLEND_RADIUS; ox <= BLEND_RADIUS; ox += step)
    {
      for (int oz = -BLEND_RADIUS; oz <= BLEND_RADIUS; oz += step)
      {
        int sx = x + ox, sz = z + oz;
        int biome = abs.getBiomeIndex(sx, sz);
        float dist = (float)Math.sqrt(ox*ox + oz*oz);
        float w = (float)Math.exp(-dist * dist / (2f * BLEND_RADIUS * BLEND_RADIUS * 0.25f));
        weightedH   += rawBiomeHeight(biome, sx, sz) * w;
        totalWeight += w;
      }
    }

    return totalWeight > 0 ? Math.round(weightedH / totalWeight) : BASE_Y;
  }

  private int rawBiomeHeight(int biome, int x, int z)
  {
    return switch (biome) {
      case AridBiomeSource.DUST -> dustH(x, z);
      case AridBiomeSource.CANYON -> canyonH(x, z);
      case AridBiomeSource.CRYSTAL -> crystalH(x, z);
      default  -> BASE_Y;
    };
  }

  private int dustH(int x, int z)
  {
    float dune = (float)(Math.sin(x * 0.055) * 4.5 + Math.sin(z * 0.048 + x * 0.02) * 3.5);
    float fbm  = AridNoise.fbm(x * 0.04f, z * 0.04f, seed+2L, 4, 0.50f) * 7f - 3.5f;
    return BASE_Y + (int)(dune + fbm);
  }

  private int canyonH(int x, int z)
  {
    float canal = AridNoise.fbm(x * 0.006f, z * 0.006f, seed+3L, 3, 0.50f);
    float dist  = Math.abs(canal - 0.5f) * 2f;

    if (dist < 0.30f)
    {
      float depth = (0.30f - dist) / 0.30f;
      float smoothDepth = depth * depth * (3f - 2f * depth);
      return (int)AridNoise.lerp(BASE_Y, CANYON_FLOOR,  smoothDepth);
    }
    float var = AridNoise.fbm(x * 0.05f, z * 0.05f, seed+4L, 2, 0.5f) * 6f - 3f;
    return PLATEAU_Y + (int)var;
  }

  private int crystalH(int x, int z)
  {
    float n = AridNoise.fbm(x * 0.04f, z * 0.04f, seed+5L, 4, 0.50f);
    return 58 + (int)(n * 12f - 6f);
  }

  private int getCraterDepression(int x, int z)
  {
    int cellX = Math.floorDiv(x, CRATER_CELL);
    int cellZ = Math.floorDiv(z, CRATER_CELL);
    int maxDepression = 0;

    for (int dcx = -1; dcx <= 1; dcx++)
    {
      for (int dcz = -1; dcz <= 1; dcz++)
      {
        int cx = cellX + dcx, cz = cellZ + dcz;

        long cellHash = hashCell(cx, cz, seed);
        if ((cellHash & 0xFF) >= (256 / CRATER_CHANCE)) continue;

        int craterX = cx * CRATER_CELL + (int)((hashCell(cx, cz, seed + 1) & 0xFFFF) % CRATER_CELL);
        int craterZ = cz * CRATER_CELL + (int)((hashCell(cx, cz, seed + 2) & 0xFFFF) % CRATER_CELL);

        int radius = CRATER_MIN_R + (int)((hashCell(cx, cz, seed + 3) & 0xFF) / 255f * (CRATER_MAX_R - CRATER_MIN_R));

        double dx = x - craterX, dz = z - craterZ;
        double dist = Math.sqrt(dx*dx + dz*dz);
        if (dist > radius) continue;

        double t = dist / radius;
        float maxDepth = CRATER_MAX_DEPTH * (radius / (float)CRATER_MAX_R);
        double rawDepth = maxDepth * (1.0 - t * t);
        int terraceY = (int)(rawDepth / TERRACE_STEP) * TERRACE_STEP;
        if (t > 0.85 && t < 1.0)
        {
          double rimT = (t - 0.85) / 0.15;
          terraceY = (int)(-maxDepth * 0.08 * Math.sin(rimT * Math.PI));
        }

        maxDepression = Math.max(maxDepression, terraceY);
      }
    }
    return maxDepression;
  }

  private static long hashCell(int cx, int cz, long seed)
  {
    long h = seed ^ ((long)cx * 0x9E3779B97F4A7C15L) ^ ((long)cz * 0x6C62272E07BB0142L);
    h ^= h >>> 33; h *= 0xFF51AFD7ED558CCDL; h ^= h >>> 33;
    return h;
  }


  private int getBiomeType(int x, int z)
  {
    return (biomeSource instanceof AridBiomeSource abs)? abs.getBiomeIndex(x, z) : AridBiomeSource.DUST;
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

    for (int y = 25; y < Math.min(62, surfaceY - 2); y++)
    {
      float offset = AridNoise.fbm(wx * 0.1f, wz * 0.1f, seed+7L+y, 1, 0.5f) * 2f;
      int idx = ((y + (int)offset) / 4) & 3;
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
