package cute.ame.auralithpioneerinitiative.Block.Thermal;

import cute.ame.auralithpioneerinitiative.Registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ThermalKelp extends KelpBlock
{
    public ThermalKelp(Properties properties)
    {
        super(properties);
    }

    @Override
    protected boolean canSurvive(@NotNull BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);

        if(belowState.is(BlockRegistry.ICE_II_STONE.get()) || belowState.getBlock() instanceof ThermalKelp) return true;
        return super.canSurvive(state, level, pos);
    }

    @Override
    protected BlockState updateBodyAfterConvertedFromHead(BlockState head, BlockState body) {
        return BlockRegistry.THERMAL_KELP_PLANT.get().defaultBlockState();
    }

    @Override
    protected Block getBodyBlock() {
        return BlockRegistry.THERMAL_KELP_PLANT.get();
    }
}
