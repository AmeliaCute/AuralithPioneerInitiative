package cute.ame.auralithpioneerinitiative.Ship.Multiblock;

import cute.ame.auralithpioneerinitiative.Registrie.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public class HoloPanelBlock extends BaseEntityBlock
{

  public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

  public HoloPanelBlock(BlockBehaviour.Properties properties)
  {
    super(properties);
    registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }

  @Override
  public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) { return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite()); }

  @Override
  protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() { return simpleCodec(HoloPanelBlock::new); }

  @Override
  public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

  @Override
  public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new HoloPanelBlockEntity(pos, state); }

  @Override
  public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
  {
    return null;
  }
}