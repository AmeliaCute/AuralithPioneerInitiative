package cute.ame.auralithpioneerinitiative.Registrie;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Ship.Multiblock.HoloPanelBlockEntity;
import cute.ame.auralithpioneerinitiative.Ship.Multiblock.ShipCoreBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;

public final class ModBlockEntities
{
    private ModBlockEntities() {}

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Auralithpioneerinitiative.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShipCoreBlockEntity>> SHIP_CORE =
        BLOCK_ENTITIES.register("ship_core", () -> BlockEntityType.Builder.of(ShipCoreBlockEntity::new, ModBlocks.SHIP_CORE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HoloPanelBlockEntity>> HOLOPANEL =
        BLOCK_ENTITIES.register("holopanel", () -> BlockEntityType.Builder.of(HoloPanelBlockEntity::new, ModBlocks.HOLOPANEL.get()).build(null));
}