package cute.ame.auralithpioneerinitiative.Planet.Arid.Worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.List;
import java.util.stream.Stream;

public final class AridBiomeSource extends BiomeSource
{
  public static final int DUST = 1;
  public static final int CANYON = 2;
  public static final int CRYSTAL = 3;
  public static final int CRATER = 4;

  private static final float FREQ_A = 0.00065f;
  private static final float FREQ_B = 0.00050f;

  public static final MapCodec<AridBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance ->
      instance.group(Biome.LIST_CODEC.fieldOf("biomes").forGetter(b -> b.biomeList)).apply(instance, AridBiomeSource::new)
  );

  private final HolderSet<Biome> biomeList;

  public AridBiomeSource(HolderSet<Biome> biomeList)
  {
    this.biomeList = biomeList;
  }

  @Override
  protected MapCodec<? extends BiomeSource> codec() { return CODEC; }

  @Override
  protected Stream<Holder<Biome>> collectPossibleBiomes() { return biomeList.stream(); }

  @Override
  public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler)
  {
    return biomeList.get(getBiomeIndex(x << 2, z << 2));
  }

  public int getBiomeIndex(int x, int z)
  {
    float a = AridNoise.fbm(x * FREQ_A, z * FREQ_A, 1L, 4, 0.55f);
    float b = AridNoise.fbm(x * FREQ_B + 800, z * FREQ_B + 400, 200L, 3, 0.50f);

    a = a * 2f - 1f;
    b = b * 2f - 1f;

    if (a > 0.25f && Math.abs(b) > 0.20f) return CANYON;
    else if (a < -0.30f && b < -0.15f) return CRYSTAL;
    else return DUST;
  }

  public float getBiomeWeight(int biome, int x, int z)
  {
    float a = AridNoise.fbm(x * FREQ_A, z * FREQ_A, 1L,  4, 0.55f) * 2f - 1f;
    float b = AridNoise.fbm(x * FREQ_B + 800, z * FREQ_B + 400, 200L, 3, 0.50f) * 2f - 1f;

    return switch (biome)
    {
      case CANYON -> AridNoise.clamp01((a - 0.15f) / 0.20f) * AridNoise.clamp01((Math.abs(b) - 0.10f) / 0.15f);
      case CRYSTAL -> AridNoise.clamp01((-a - 0.20f) / 0.15f) * AridNoise.clamp01((-b - 0.05f) / 0.15f);
      case DUST -> AridNoise.clamp01((a - 0.00f) / 0.15f + (b - 0.25f) / 0.15f);
      default -> 0f;
    };
  }

  public HolderSet<Biome> getBiomeList() { return biomeList; }
}