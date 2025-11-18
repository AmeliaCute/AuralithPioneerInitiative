package cute.ame.auralithpioneerinitiative.Feature.Thermal;

import com.mojang.serialization.Codec;
import cute.ame.auralithpioneerinitiative.Registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ThermalKelpFeature extends Feature<NoneFeatureConfiguration> {

    public ThermalKelpFeature(Codec<NoneFeatureConfiguration> codec)
    {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();

        BlockState below = level.getBlockState(pos.below());
        if (!below.is(BlockRegistry.ICE_II_STONE.get()) || !level.getBlockState(pos).is(Blocks.WATER))
            return false;

        int height = random.nextInt(10) + 4;
        BlockPos.MutableBlockPos mutablePos = pos.mutable();

        for (int i = 0; i < height; i++) {
            if (!level.getBlockState(mutablePos).is(Blocks.WATER))
                break;

            if (i == 0)
                level.setBlock(mutablePos, BlockRegistry.THERMAL_KELP.get().defaultBlockState(), 2);
            else
                level.setBlock(mutablePos, BlockRegistry.THERMAL_KELP_PLANT.get().defaultBlockState(), 2);

            mutablePos.move(Direction.UP);
        }

        return true;
    }
}