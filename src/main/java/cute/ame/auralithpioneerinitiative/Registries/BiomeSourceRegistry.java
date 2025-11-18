package cute.ame.auralithpioneerinitiative.Registries;

import com.mojang.serialization.MapCodec;
import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.WorldGen.YLevelBiomeSource;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BiomeSourceRegistry
{
    public static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, Auralithpioneerinitiative.MODID);

    public static final Supplier<MapCodec<YLevelBiomeSource>> Y_LEVEL_BIOME_SOURCE =
            BIOME_SOURCES.register("y_level", () -> YLevelBiomeSource.CODEC);
}
