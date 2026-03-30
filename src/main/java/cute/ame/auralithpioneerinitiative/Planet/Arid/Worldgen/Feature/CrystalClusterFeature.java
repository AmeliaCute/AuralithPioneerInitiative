package cute.ame.auralithpioneerinitiative.Planet.Arid.Worldgen.Feature;

import com.mojang.serialization.Codec;
import cute.ame.auralithpioneerinitiative.Registrie.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class CrystalClusterFeature extends Feature<NoneFeatureConfiguration>
{
  public CrystalClusterFeature(Codec<NoneFeatureConfiguration> codec) { super(codec); }

  @Override
  public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context)
  {
    WorldGenLevel level = context.level();
    RandomSource random = context.random();
    int count = 3 + context.random().nextInt(6);
    BlockPos origin = context.origin();
    boolean placed = false;

    BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();

    for (int i = 0; i < count; i++)
    {
      int ox = origin.getX() + random.nextInt(9) - 4;
      int oy = origin.getY() + random.nextInt(5) - 2;
      int oz = origin.getZ() + random.nextInt(9) - 4;
      mpos.set(ox, oy, oz);

      BlockPos below = mpos.below();
      if (!level.getBlockState(below).isSolid()) continue;
      if (!level.getBlockState(mpos).isAir()) continue;

      float r = random.nextFloat();
      BlockState crystal = r < 0.50f ? SMALL : r < 0.85f ? MEDIUM : LARGE;

      level.setBlock(mpos, crystal, 2);
      placed = true;
    }
    return placed;
  }

  private static final BlockState SMALL = ModBlocks.ARID_CRYSTAL_SMALL.get().defaultBlockState();
  private static final BlockState MEDIUM = ModBlocks.ARID_CRYSTAL_MEDIUM.get().defaultBlockState();
  private static final BlockState LARGE = ModBlocks.ARID_CRYSTAL_LARGE.get().defaultBlockState();


}