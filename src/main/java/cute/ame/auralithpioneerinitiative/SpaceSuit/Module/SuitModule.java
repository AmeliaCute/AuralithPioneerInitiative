package cute.ame.auralithpioneerinitiative.SpaceSuit.Module;

import cute.ame.auralithpioneerinitiative.SpaceSuit.Data.ModuleSlotType;
import net.minecraft.world.item.Item;

public class SuitModule extends Item
{
  private final ModuleSlotType slotType;

  public SuitModule(ModuleSlotType slotType, Properties props) {
    super(props.stacksTo(1));
    this.slotType = slotType;
  }

  public ModuleSlotType getSlotType() { return slotType; }
}
