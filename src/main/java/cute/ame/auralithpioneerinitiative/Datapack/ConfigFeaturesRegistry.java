package cute.ame.auralithpioneerinitiative.Datapack;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Feature.Thermal.ThermalKelpFeature;
import cute.ame.auralithpioneerinitiative.Registries.BlockRegistry;
import cute.ame.auralithpioneerinitiative.Registries.FeatureRegistry;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WaterloggedTransparentBlock;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import java.util.List;

public class ConfigFeaturesRegistry
{
    public static final ResourceKey<ConfiguredFeature<?, ?>> THERMAL_PLANTS_I = register("thermal_plant_i");
    public static final ResourceKey<ConfiguredFeature<?, ?>> THERMAL_GEYSER = register("thermal_geyser");
    public static final ResourceKey<ConfiguredFeature<?, ?>> THERMAL_KELP = register("thermal_kelp");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?,?>> context)
    {
        FeatureUtils.register(context, THERMAL_PLANTS_I, Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(
                        BlockStateProvider.simple(
                                BlockRegistry.THERMAL_PLANT_I.get()
                                        .defaultBlockState()
                                        .setValue(WaterloggedTransparentBlock.WATERLOGGED, true)
                        )
                ));

        FeatureUtils.register(context, THERMAL_GEYSER, Feature.BLOCK_COLUMN,
                new BlockColumnConfiguration(
                        java.util.List.of(
                                BlockColumnConfiguration.layer(
                                        UniformInt.of(3, 7),
                                        BlockStateProvider.simple(Blocks.MAGMA_BLOCK)
                                )
                        ),
                        net.minecraft.core.Direction.UP,
                        BlockPredicate.matchesBlocks(Blocks.STONE, Blocks.DEEPSLATE),
                        false
                ));

        FeatureUtils.register(context, THERMAL_KELP, FeatureRegistry.THERMAL_KELP_FEATURE.get(),
                FeatureConfiguration.NONE);
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> register(String name)
    {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, name));
    }
}