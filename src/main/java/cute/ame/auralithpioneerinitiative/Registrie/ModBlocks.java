package cute.ame.auralithpioneerinitiative.Registrie;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Block.Alien.AlienConduit;
import cute.ame.auralithpioneerinitiative.Block.Alien.AlienPanel;
import cute.ame.auralithpioneerinitiative.Block.Arid.*;
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

    public static final DeferredBlock<AridDust> ARID_DUST = BLOCKS.register(
        "arid_dust",
        AridDust::new
    );

    public static final DeferredBlock<AridRock> ARID_ROCK = BLOCKS.register(
        "arid_rock",
        () -> new AridRock(AridRock.Variant.RAW)
    );

    public static final DeferredBlock<AridRock> ARID_ROCK_CRACKED = BLOCKS.register(
        "arid_rock_cracked",
        () -> new AridRock(AridRock.Variant.CRACKED)
    );

    public static final DeferredBlock<AridRock> ARID_ROCK_POLISHED = BLOCKS.register(
        "arid_rock_polished",
        () -> new AridRock(AridRock.Variant.POLISHED)
    );

    public static final DeferredBlock<AridStrata> ARID_STRATA_RED = BLOCKS.register(
        "arid_strata_red",
        () -> new AridStrata(AridStrata.Variant.RED)
    );

    public static final DeferredBlock<AridStrata> ARID_STRATA_ORANGE = BLOCKS.register(
        "arid_strata_orange",
        () -> new AridStrata(AridStrata.Variant.ORANGE)
    );

    public static final DeferredBlock<AridStrata> ARID_STRATA_WHITE = BLOCKS.register(
        "arid_strata_white",
        () -> new AridStrata(AridStrata.Variant.WHITE)
    );

    public static final DeferredBlock<AridStrata> ARID_STRATA_MAROON = BLOCKS.register(
        "arid_strata_maroon",
        () -> new AridStrata(AridStrata.Variant.MAROON)
    );

    public static final DeferredBlock<AridCrystal> ARID_CRYSTAL_SMALL = BLOCKS.register(
        "arid_crystal_small",
        () -> new AridCrystal(AridCrystal.Size.SMALL)
    );

    public static final DeferredBlock<AridCrystal> ARID_CRYSTAL_MEDIUM = BLOCKS.register(
        "arid_crystal_medium",
        () -> new AridCrystal(AridCrystal.Size.MEDIUM)
    );

    public static final DeferredBlock<AridCrystal> ARID_CRYSTAL_LARGE = BLOCKS.register(
        "arid_crystal_large",
        () -> new AridCrystal(AridCrystal.Size.LARGE)
    );

    public static final DeferredBlock<AridGlyph> ARID_GLYPH = BLOCKS.register(
        "arid_glyph",
        AridGlyph::new
    );

    public static final DeferredBlock<AlienPanel> ALIEN_PANEL = BLOCKS.register(
        "alien_panel",
        AlienPanel::new
    );

    public static final DeferredBlock<AlienConduit> ALIEN_CONDUIT = BLOCKS.register(
        "alien_conduit",
        AlienConduit::new
    );
}