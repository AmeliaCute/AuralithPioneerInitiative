package cute.ame.auralithpioneerinitiative.WorldGen;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Datapack.BiomeRegistry;
import cute.ame.auralithpioneerinitiative.Datapack.ConfigFeaturesRegistry;
import cute.ame.auralithpioneerinitiative.Datapack.PlacedFeaturesRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class WorldGenProvider extends DatapackBuiltinEntriesProvider
{
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, ConfigFeaturesRegistry::bootstrap)
            .add(Registries.PLACED_FEATURE, PlacedFeaturesRegistry::bootstrap)
            .add(Registries.BIOME, BiomeRegistry::bootstrap);

    public WorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Auralithpioneerinitiative.MODID));
    }
}
