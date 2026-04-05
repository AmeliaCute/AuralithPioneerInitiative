package cute.ame.auralithpioneerinitiative.Registrie;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.SpaceSuit.GUI.SuitContainerMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenuTypes
{
  private ModMenuTypes() {}

  public static final DeferredRegister<MenuType<?>> MENU_TYPES =
      DeferredRegister.create(Registries.MENU, Auralithpioneerinitiative.MODID);

  public static final DeferredHolder<MenuType<?>, MenuType<SuitContainerMenu>> SUIT_EQUIPMENT =
      MENU_TYPES.register("suit_equipment", () ->
          IMenuTypeExtension.create(SuitContainerMenu::new));
}