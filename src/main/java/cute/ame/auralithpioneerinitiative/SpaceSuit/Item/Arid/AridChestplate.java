package cute.ame.auralithpioneerinitiative.SpaceSuit.Item.Arid;

import cute.ame.auralithpioneerinitiative.Registrie.ModArmorMaterials;
import net.minecraft.world.item.ArmorItem;

public final class AridChestplate extends ArmorItem
{
  public static final int O2_BONUS = 3_000;

  public AridChestplate(Properties props)
  {
    super(ModArmorMaterials.ARID, Type.CHESTPLATE, props.durability(800));
  }
}
