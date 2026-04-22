package cute.ame.auralithpioneerinitiative.vehicle.Assembly;

import cute.ame.auralithpioneerinitiative.vehicle.Baking.VehicleSnapshot;
import cute.ame.auralithpioneerinitiative.vehicle.Block.Impl.SeatBlock;
import cute.ame.auralithpioneerinitiative.vehicle.Block.VehicleBlockInterfaces.*;
import cute.ame.auralithpioneerinitiative.vehicle.Entity.BlockVehicleEntity;
import cute.ame.auralithpioneerinitiative.vehicle.Entity.VehicleSeat;
import cute.ame.auralithpioneerinitiative.vehicle.Entity.VehicleType;
import cute.ame.auralithpioneerinitiative.vehicle.Network.VehicleSnapshotPacket;
import cute.ame.auralithpioneerinitiative.vehicle.Register.ModVehicleEntities;
import cute.ame.auralithpioneerinitiative.vehicle.Seat.CameraConfig;
import cute.ame.auralithpioneerinitiative.vehicle.Seat.SeatDefinition;
import cute.ame.auralithpioneerinitiative.vehicle.Types.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

public final class VehicleAssembler
{

  private VehicleAssembler() {}

  public static final int CAPTURE_RADIUS = 32; // make it dynamic

  @Nullable
  public static BlockVehicleEntity assemble(ServerLevel level, BlockPos corePos, Direction facing, @Nullable Player player)
  {
    List<BlockVehicleEntity.BlockEntry> snapshot = captureSnapshot(level, corePos);
    if (snapshot.isEmpty())
      return null;

    VehicleType type = detectType(snapshot);

    BlockVehicleEntity vehicle = createEntity(level, type);
    if (vehicle == null)
      return null;

    vehicle.setBlockSnapshot(snapshot);
    vehicle.setVehicleRotation(computeInitialRotation(facing));
    vehicle.moveTo(corePos.getX() + 0.5, corePos.getY(), corePos.getZ() + 0.5);
    populateSeats(vehicle, snapshot, corePos);

    level.addFreshEntity(vehicle);
    List<VehicleSnapshot.BlockEntry> pktEntries = new ArrayList<>(snapshot.size());
    for (var e : snapshot)
      pktEntries.add(new VehicleSnapshot.BlockEntry(e.relX(), e.relY(), e.relZ(), e.stateId()));

    PacketDistributor.sendToPlayersTrackingEntityAndSelf(vehicle, new VehicleSnapshotPacket(vehicle.getUUID(), pktEntries));
    String msg = String.format("[Pioneer] %s assembled : %d blocks (entity %s)", type.name().toLowerCase(), snapshot.size(), vehicle.getUUID());
    if (player != null) player.sendSystemMessage(Component.literal(msg));

    return vehicle;
  }

  public static List<BlockVehicleEntity.BlockEntry> captureSnapshot(ServerLevel level, BlockPos core)
  {
    List<BlockVehicleEntity.BlockEntry> result = new ArrayList<>();
    int r = CAPTURE_RADIUS;
    for (int dy = -r; dy <= r; dy++)
      for (int dz = -r; dz <= r; dz++)
        for (int dx = -r; dx <= r; dx++)
        {
          BlockState state = level.getBlockState(core.offset(dx, dy, dz));
          if (state.isAir()) continue;

          result.add(new BlockVehicleEntity.BlockEntry(dx, dy, dz, Block.getId(state)));
        }

    return result;
  }

  public static VehicleType detectType(List<BlockVehicleEntity.BlockEntry> snapshot)
  {
    boolean hasRocket = false, hasSuspension = false;
    for (var entry : snapshot)
    {
      Block block = Block.stateById(entry.stateId()).getBlock();
      if (block instanceof IRocketMotorBlock) { hasRocket = true; break; }
      if (block instanceof ISuspensionBlock) hasSuspension = true;
    }
    if (hasRocket) return VehicleType.ROCKET;
    if (hasSuspension) return VehicleType.GROUND;
    return VehicleType.SPACESHIP;
  }

  @Nullable
  private static BlockVehicleEntity createEntity(ServerLevel level, VehicleType type)
  {
    return switch (type)
    {
      case SPACESHIP -> ModVehicleEntities.SPACESHIP.get().create(level);
      case ROCKET -> ModVehicleEntities.ROCKET.get().create(level);
      case SUBMARINE -> ModVehicleEntities.SUBMARINE.get().create(level);
      case GROUND -> ModVehicleEntities.GROUND_VEHICLE.get().create(level);
    };
  }

  private static void populateSeats(BlockVehicleEntity vehicle, List<BlockVehicleEntity.BlockEntry> snapshot, BlockPos corePos)
  {
    List<VehicleSeat> seats = new ArrayList<>();

    for (var entry : snapshot)
    {
      BlockState state = Block.stateById(entry.stateId());
      if (!(state.getBlock() instanceof SeatBlock)) continue;

      boolean isPilot = SeatBlock.isPilotSeat(state);
      Vec3 localOffset = new Vec3(entry.relX() + 0.5, entry.relY() + 0.5, entry.relZ() + 0.5);
      CameraConfig cam = isPilot ? CameraConfig.COCKPIT : CameraConfig.DEFAULT;
      SeatDefinition def = new SeatDefinition(localOffset, isPilot, cam);
      seats.add(new VehicleSeat(def));
    }

    if (seats.isEmpty())
      seats.add(new VehicleSeat(SeatDefinition.pilotDefault()));

    seats.sort((a, b) -> Boolean.compare(b.isPilot(), a.isPilot()));

    for (VehicleSeat seat : seats)
      vehicle.addSeat(seat);
  }

  private static Quaternionf computeInitialRotation(Direction facing)
  {
    Quaternionf q = new Quaternionf();
    return switch (facing)
    {
      case SOUTH -> q.rotationY((float) Math.PI);
      case WEST -> q.rotationY((float) (Math.PI / 2.0));
      case EAST -> q.rotationY((float) (-Math.PI / 2.0));
      default -> q;
    };
  }
}