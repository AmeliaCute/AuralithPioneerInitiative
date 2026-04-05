package cute.ame.auralithpioneerinitiative.SpaceSuit.Item;

import cute.ame.auralithpioneerinitiative.SpaceSuit.Data.SuitPieceDefinition;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

public class SuitArmorItem extends ArmorItem
{
  public final SuitPieceDefinition definition;

  public SuitArmorItem(Holder<ArmorMaterial> material, Type type, SuitPieceDefinition definition, Properties props)
  {
    super(material, type, props.durability(800));
    this.definition = definition;
  }

  public SuitPieceDefinition getDefinition() { return definition; }
}
