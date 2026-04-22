package cute.ame.auralithpioneerinitiative.vehicle.Network;

import cute.ame.auralithpioneerinitiative.vehicle.Baking.VehicleSnapshot;
import cute.ame.auralithpioneerinitiative.vehicle.Client.VehicleClientCache;
import cute.ame.auralithpioneerinitiative.vehicle.Entity.AbstractVehicleEntity;
import cute.ame.auralithpioneerinitiative.vehicle.Entity.BlockVehicleEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record VehicleSnapshotPacket(
    UUID vehicleUUID,
    List<VehicleSnapshot.BlockEntry> blocks
) implements CustomPacketPayload
{

  public static final Type<VehicleSnapshotPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("auralithpioneerinitiative", "vehicle_snapshot"));

  public static final StreamCodec<FriendlyByteBuf, VehicleSnapshotPacket> STREAM_CODEC = StreamCodec.of(VehicleSnapshotPacket::encode, VehicleSnapshotPacket::decode);

  @Override
  public Type<? extends CustomPacketPayload> type() { return TYPE; }

  private static void encode(FriendlyByteBuf buf, VehicleSnapshotPacket p)
  {
    buf.writeLong(p.vehicleUUID.getMostSignificantBits());
    buf.writeLong(p.vehicleUUID.getLeastSignificantBits());
    buf.writeVarInt(p.blocks.size());
    for (VehicleSnapshot.BlockEntry e : p.blocks)
    {
      buf.writeShort(e.relX());
      buf.writeShort(e.relY());
      buf.writeShort(e.relZ());
      buf.writeVarInt(e.stateId());
    }
  }

  private static VehicleSnapshotPacket decode(FriendlyByteBuf buf)
  {
    UUID uuid = new UUID(buf.readLong(), buf.readLong());
    int count = buf.readVarInt();
    List<VehicleSnapshot.BlockEntry> entries = new ArrayList<>(count);
    for (int i = 0; i < count; i++)
    {
      entries.add(new VehicleSnapshot.BlockEntry(buf.readShort(), buf.readShort(), buf.readShort(), buf.readVarInt()));
    }

    return new VehicleSnapshotPacket(uuid, entries);
  }


  @OnlyIn(Dist.CLIENT)
  public static void handle(VehicleSnapshotPacket p, IPayloadContext ctx)
  {
    ctx.enqueueWork(() ->
    {
      VehicleSnapshot snapshot = new VehicleSnapshot(p.blocks());

      AbstractVehicleEntity vehicle = VehicleClientCache.getEntity(p.vehicleUUID());
      if (vehicle instanceof BlockVehicleEntity bv)
      {
        List<BlockVehicleEntity.BlockEntry> entityEntries = new ArrayList<>(p.blocks().size());
        for (VehicleSnapshot.BlockEntry e : p.blocks())
        {
          entityEntries.add(new BlockVehicleEntity.BlockEntry(e.relX(), e.relY(), e.relZ(), e.stateId()));
        }
        bv.setBlockSnapshot(entityEntries);
      }

      VehicleClientCache.onSnapshotReceived(p.vehicleUUID(), snapshot);
    });
  }

  public static VehicleSnapshotPacket from(UUID uuid, List<BlockVehicleEntity.BlockEntry> snapshot)
  {
    List<VehicleSnapshot.BlockEntry> entries = new ArrayList<>(snapshot.size());
    for (var e : snapshot) entries.add(new VehicleSnapshot.BlockEntry(e.relX(), e.relY(), e.relZ(), e.stateId()));

    return new VehicleSnapshotPacket(uuid, entries);
  }
}