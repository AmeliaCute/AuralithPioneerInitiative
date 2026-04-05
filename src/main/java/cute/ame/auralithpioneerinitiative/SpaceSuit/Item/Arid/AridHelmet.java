package cute.ame.auralithpioneerinitiative.SpaceSuit.Item.Arid;

import cute.ame.auralithpioneerinitiative.Registrie.ModArmorMaterials;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class AridHelmet extends ArmorItem
{
  public AridHelmet(Properties props)
  {
    super(ModArmorMaterials.ARID, Type.HELMET, props.durability(800));
  }

  public static boolean isWearing(Player player)
  {
    ItemStack head = player.getInventory().armor.get(3);
    return head.getItem() instanceof AridHelmet;
  }
}
