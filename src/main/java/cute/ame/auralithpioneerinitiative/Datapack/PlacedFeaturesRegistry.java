package cute.ame.auralithpioneerinitiative.Datapack;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Registries.BlockRegistry;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.placement.*;

public class PlacedFeaturesRegistry
{
    public static final ResourceKey<PlacedFeature> THERMAL_PLANTS_I = registerPlacedKey("thermal_plant_i");
    public static final ResourceKey<PlacedFeature> THERMAL_GEYSER = registerPlacedKey("thermal_geyser");
    public static final ResourceKey<PlacedFeature> THERMAL_KELP = registerPlacedKey("thermal_kelp");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        PlacementUtils.register(context, THERMAL_PLANTS_I,
                configuredFeatures.getOrThrow(ConfigFeaturesRegistry.THERMAL_PLANTS_I),
                CountPlacement.of(80),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(1), VerticalAnchor.absolute(100)),
                BlockPredicateFilter.forPredicate(
                        BlockPredicate.allOf(
                                BlockPredicate.matchesBlocks(Direction.DOWN.getNormal(),
                                        Blocks.BASALT, Blocks.ANDESITE, Blocks.BLACKSTONE,
                                        Blocks.MAGMA_BLOCK, Blocks.DEEPSLATE, BlockRegistry.ICE_II_STONE.get()),
                                BlockPredicate.matchesBlocks(Blocks.WATER)
                        )
                ),
                BiomeFilter.biome());

        PlacementUtils.register(context, THERMAL_GEYSER,
                configuredFeatures.getOrThrow(ConfigFeaturesRegistry.THERMAL_GEYSER),
                CountPlacement.of(4),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(10), VerticalAnchor.absolute(100)),
                BiomeFilter.biome());

        PlacementUtils.register(context, THERMAL_KELP,
                configuredFeatures.getOrThrow(ConfigFeaturesRegistry.THERMAL_KELP),
                CountPlacement.of(200),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(1), VerticalAnchor.absolute(100)),
                BlockPredicateFilter.forPredicate(
                        BlockPredicate.allOf(
                                BlockPredicate.matchesBlocks(Direction.DOWN.getNormal(),
                                        BlockRegistry.ICE_II_STONE.get()),
                                BlockPredicate.matchesBlocks(Blocks.WATER)
                        )
                ),
                BiomeFilter.biome());
    }

    private static ResourceKey<PlacedFeature> registerPlacedKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE,
                ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, name));
    }
}