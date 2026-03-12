package cute.ame.auralithpioneerinitiative.Registrie;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Item.PortableThrusterItem;
import cute.ame.auralithpioneerinitiative.Item.SpaceNavigatorItem;
import net.minecraft.world.item.Item.Properties;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems
{
  public ModItems() {}
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Auralithpioneerinitiative.MODID);

  public static final DeferredItem<PortableThrusterItem> PORTABLE_THRUSTER = ITEMS.register("portable_thruster", () -> new PortableThrusterItem(new Properties().stacksTo(1)));
  public static final DeferredItem<SpaceNavigatorItem> SPACE_NAVIGATOR = ITEMS.register("space_navigator",() -> new SpaceNavigatorItem(new Properties().stacksTo(1)));

}
