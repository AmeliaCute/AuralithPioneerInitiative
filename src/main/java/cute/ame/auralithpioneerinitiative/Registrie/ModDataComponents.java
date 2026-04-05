package cute.ame.auralithpioneerinitiative.Registrie;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Data.SuitInventoryComponent;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents
{
  private ModDataComponents() {}

  public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Auralithpioneerinitiative.MODID);

  public static final DeferredHolder<DataComponentType<?>, DataComponentType<SuitInventoryComponent>> SUIT_INVENTORY =
      DATA_COMPONENTS.register("suit_inventory", () ->
          DataComponentType.<SuitInventoryComponent>builder()
              .persistent(SuitInventoryComponent.CODEC)
              .build()
      );

  public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> O2_STORED =
      DATA_COMPONENTS.register("o2_stored", () ->
          DataComponentType.<Integer>builder()
              .persistent(DataComponents.DAMAGE.codec())
              .networkSynchronized(ByteBufCodecs.INT)
              .build()
      );
}