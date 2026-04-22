package cute.ame.auralithpioneerinitiative.vehicle.Assembly;

import cute.ame.auralithpioneerinitiative.vehicle.Types.GroundVehicleEntity;
import cute.ame.auralithpioneerinitiative.vehicle.Types.RocketEntity;
import cute.ame.auralithpioneerinitiative.vehicle.Types.SpaceshipEntity;
import cute.ame.auralithpioneerinitiative.vehicle.Types.SubmarineEntity;
import cute.ame.auralithpioneerinitiative.vehicle.Entity.BlockVehicleEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public class VehicleCoreBlockEntity extends BlockEntity
{
  private boolean assembled = false;
  private @Nullable UUID vehicleEntityUUID = null;

  private static final double BOARD_REACH = 6.0;
  private static final double BOARD_HIT_RADIUS = 2.0;

  public VehicleCoreBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  public InteractionResult onUse(Level level, BlockPos pos, BlockState state, Player player)
  {
    if (level.isClientSide()) return InteractionResult.SUCCESS;
    if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.PASS;

    Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) ? state.getValue(BlockStateProperties.HORIZONTAL_FACING) : Direction.NORTH;
    if (!assembled)
    {
      BlockVehicleEntity vehicle = VehicleAssembler.assemble(serverLevel, pos, facing, player);
      if (vehicle != null)
      {
        assembled = true;
        vehicleEntityUUID = vehicle.getUUID();
        setChanged();
      }

      return InteractionResult.CONSUME;
    }

    if (player.isShiftKeyDown())
    {
      // TODO: desassembling
      return InteractionResult.CONSUME;
    }

    return tryBoard(serverLevel, player);
  }

  private InteractionResult tryBoard(ServerLevel level, Player player)
  {
    if (vehicleEntityUUID == null)
    {
      assembled = false;
      setChanged();
      return InteractionResult.CONSUME;
    }

    BlockVehicleEntity vehicle = findVehicle(level);
    if (vehicle == null)
    {
      assembled = false;
      vehicleEntityUUID = null;
      setChanged();
      return InteractionResult.CONSUME;
    }

    if (vehicle instanceof SpaceshipEntity ship && ship.hasPilot())
      return InteractionResult.FAIL;

    Vec3 vehiclePos = vehicle.position();
    if (player.distanceTo(vehicle) > BOARD_REACH * 2)
      return InteractionResult.FAIL;

    if (vehicle instanceof SpaceshipEntity ship) ship.setPilotUUID(player.getUUID());
    else if (vehicle instanceof RocketEntity rocket) rocket.setPilotUUID(player.getUUID());
    else if (vehicle instanceof SubmarineEntity sub) sub.setPilotUUID(player.getUUID());
    else if (vehicle instanceof GroundVehicleEntity gv) gv.setPilotUUID(player.getUUID());

    var seats = vehicle.getSeats();
    if (!seats.isEmpty()) seats.get(0).setOccupantUUID(player.getUUID());

    player.startRiding(vehicle, true);
    return InteractionResult.CONSUME;
  }

  public void markDisassembled()
  {
    assembled = false;
    vehicleEntityUUID = null;
    setChanged();
  }

  @Nullable
  private BlockVehicleEntity findVehicle(ServerLevel level)
  {
    if (vehicleEntityUUID == null) return null;

    Entity e = level.getEntity(vehicleEntityUUID);
    return e instanceof BlockVehicleEntity bv ? bv : null;
  }

  public boolean isAssembled() { return assembled; }
  public @Nullable UUID getVehicleEntityUUID(){ return vehicleEntityUUID; }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
  {
    super.saveAdditional(tag, registries);
    tag.putBoolean("assembled", assembled);
    if (vehicleEntityUUID != null) tag.putUUID("vehicleUUID", vehicleEntityUUID);
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
  {
    super.loadAdditional(tag, registries);
    assembled = tag.getBoolean("assembled");
    vehicleEntityUUID = tag.hasUUID("vehicleUUID") ? tag.getUUID("vehicleUUID") : null;
  }
}
