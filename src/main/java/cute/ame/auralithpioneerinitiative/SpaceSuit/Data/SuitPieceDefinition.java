package cute.ame.auralithpioneerinitiative.SpaceSuit.Data;

public record SuitPieceDefinition
(
  ModuleSlotType... slots
)
{
  public int slotCount() { return slots.length; }

  public boolean accepts(int slotIndex, ModuleSlotType type)
  {
    if(slotIndex < 0 || slotIndex >= slotCount()) return false;
    return slots[slotIndex] == ModuleSlotType.FREE || slots[slotIndex] == type;
  }
}
