package cute.ame.auralithpioneerinitiative.Block.Alien;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class AlienPanel extends Block
{
  public AlienPanel()
  {
    super(BlockBehaviour.Properties.of()
        .mapColor(MapColor.COLOR_CYAN)
        .requiresCorrectToolForDrops()
        .strength(4.0f, 1200.0f)
        .sound(SoundType.METAL));
  }
}