package cute.ame.auralithpioneerinitiative.Registrie;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Ship.Multiblock.HoloPanelBlock;
import cute.ame.auralithpioneerinitiative.Ship.Multiblock.ShipCoreBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks
{
    private ModBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(Auralithpioneerinitiative.MODID);

    public static final DeferredBlock<ShipCoreBlock> SHIP_CORE = BLOCKS.register(
        "ship_core",
        () -> new ShipCoreBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL).requiresCorrectToolForDrops()
            .strength(5.0f, 1200.0f).sound(SoundType.METAL))
    );

    public static final DeferredBlock<HoloPanelBlock> HOLOPANEL = BLOCKS.register(
        "holopanel",
        () -> new HoloPanelBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL).requiresCorrectToolForDrops()
            .strength(3.0f, 1200.0f).sound(SoundType.METAL)
            .noOcclusion())
    );
}