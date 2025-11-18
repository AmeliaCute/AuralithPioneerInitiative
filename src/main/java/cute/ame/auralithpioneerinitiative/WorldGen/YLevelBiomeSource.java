package cute.ame.auralithpioneerinitiative.WorldGen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class YLevelBiomeSource extends BiomeSource {
    public static final MapCodec<YLevelBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Biome.LIST_CODEC.fieldOf("biomes").forGetter(source -> HolderSet.direct(source.biomes))
            ).apply(instance, holders -> new YLevelBiomeSource(holders.stream().toList()))
    );

    private final java.util.List<Holder<Biome>> biomes;

    public YLevelBiomeSource(java.util.List<Holder<Biome>> biomes)
    {
        this.biomes = biomes;
    }

    @Override
    protected @NotNull MapCodec<? extends BiomeSource> codec()
    {
        return CODEC;
    }

    @Override
    protected @NotNull Stream<Holder<Biome>> collectPossibleBiomes()
    {
        return this.biomes.stream();
    }

    @Override
    public @NotNull Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.@NotNull Sampler sampler)
    {
        // Y coordinate is in quarter-blocks (y * 4 = actual Y)
        // Sea level is at Y=2030
        // Biome distribution:
        // - Sunlit Waters: 1800-2032 (near surface)
        // - Twilight Depths: 1500-1800
        // - Midnight Abyss: 1000-1500
        // - Abyssal Plains: 500-1000
        // - Hadal Trench: 30-500
        // - Thermal Springs: 0-30 (deepest)

        int actualY = y * 4;

        if (actualY >= 1800) {
            return getBiomeByName("auralithpioneerinitiative:sunlit_waters");
        } else if (actualY >= 1500) {
            return getBiomeByName("auralithpioneerinitiative:twilight_depths");
        } else if (actualY >= 1000) {
            return getBiomeByName("auralithpioneerinitiative:midnight_abyss");
        } else if (actualY >= 500) {
            return getBiomeByName("auralithpioneerinitiative:abyssal_plains");
        } else if (actualY >= 100) {
            return getBiomeByName("auralithpioneerinitiative:hadal_trench");
        } else {
            return getBiomeByName("auralithpioneerinitiative:thermal_springs");
        }
    }

    private Holder<Biome> getBiomeByName(String name)
    {
        return this.biomes.stream()
                .filter(holder -> holder.is(ResourceKey.create(Registries.BIOME,
                        ResourceLocation.parse(name))))
                .findFirst()
                .orElse(this.biomes.getFirst());
    }
}