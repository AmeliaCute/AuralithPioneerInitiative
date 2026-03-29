package cute.ame.auralithpioneerinitiative.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class AlienConduit extends Block
{
  public AlienConduit()
  {
    super(BlockBehaviour.Properties.of()
        .mapColor(MapColor.COLOR_CYAN)
        .requiresCorrectToolForDrops()
        .strength(3.0f, 1200.0f)
        .sound(SoundType.METAL)
        .lightLevel(state -> 10)
        .noOcclusion());
  }

  @Override
  public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random)
  {
    if (random.nextInt(4) == 0)
    {
      double px = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
      double py = pos.getY() + 1.0;
      double pz = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
      level.addParticle(ParticleTypes.PORTAL, px, py, pz, 0.0, 0.05, 0.0);
    }
  }
}