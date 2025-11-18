package cute.ame.auralithpioneerinitiative.Registries;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Block.Thermal.ThermalKelp;
import cute.ame.auralithpioneerinitiative.Plant.Thermal.ThermalKelpPlant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WaterloggedTransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BlockRegistry
{
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, Auralithpioneerinitiative.MODID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, Auralithpioneerinitiative.MODID);

    public static Supplier<Block> ICE_II_STONE;

    public static Supplier<Block> THERMAL_PLANT_I;

    public static Supplier<Block> THERMAL_KELP;
    public static Supplier<Block> THERMAL_KELP_PLANT;

    private static <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> block) {
        Supplier<T> toReturn = BLOCKS.register(name, block);
        ITEMS.register(name, () -> new BlockItem(toReturn.get(), new Item.Properties()));
        return toReturn;
    }

    public static void register(IEventBus modEventBus)
    {
        ICE_II_STONE = registerBlock("ice_ii_stone",
                () -> new Block(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_BLUE)
                        .strength(10.0F, 100.0F)
                        .sound(SoundType.DEEPSLATE_BRICKS)
                        .requiresCorrectToolForDrops()));

        THERMAL_PLANT_I = registerBlock("thermal_plant_i",
                () -> new WaterloggedTransparentBlock(BlockBehaviour.Properties.of()
                        .lightLevel(value -> Blocks.GLOWSTONE.defaultBlockState().getLightEmission())
                        .strength(20.0F, 300.0F)
                        .sound(SoundType.POLISHED_TUFF)
                        .noOcclusion()
                        .requiresCorrectToolForDrops()));

        THERMAL_KELP = registerBlock("thermal_kelp",
                () -> new ThermalKelp(BlockBehaviour.Properties.of()
                        .lightLevel(value -> Blocks.GLOWSTONE.defaultBlockState().getLightEmission())
                        .mapColor(MapColor.WATER)
                        .noCollission()
                        .randomTicks()
                        .sound(SoundType.TUFF_BRICKS)
                        .strength(20.0F, 300.0F)
                        .noOcclusion()
                ));

        THERMAL_KELP_PLANT = registerBlock("thermal_kelp_plant",
                () -> new ThermalKelpPlant(BlockBehaviour.Properties.of()
                        .lightLevel(value -> Blocks.GLOWSTONE.defaultBlockState().getLightEmission())
                        .mapColor(MapColor.WATER)
                        .noCollission()
                        .randomTicks()
                        .sound(SoundType.TUFF_BRICKS)
                        .strength(20.0F, 300.0F)
                        .noOcclusion()
                ));

        BLOCKS.register(modEventBus);
    }
}
