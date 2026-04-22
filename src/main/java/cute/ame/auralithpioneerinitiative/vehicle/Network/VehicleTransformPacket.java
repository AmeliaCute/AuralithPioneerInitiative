package cute.ame.auralithpioneerinitiative.vehicle.Network;

import cute.ame.auralithpioneerinitiative.vehicle.Client.VehicleClientCache;
import cute.ame.auralithpioneerinitiative.vehicle.Entity.AbstractVehicleEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Quaternionf;

import java.util.UUID;

public record VehicleTransformPacket(
    UUID vehicleUUID,
    double posX, double posY, double posZ,
    float  qx, float  qy,   float  qz,   float qw
) implements CustomPacketPayload
{

  public static final Type<VehicleTransformPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("auralithpioneerinitiative", "vehicle_transform"));

  public static final StreamCodec<FriendlyByteBuf, VehicleTransformPacket> STREAM_CODEC = StreamCodec.of(VehicleTransformPacket::encode, VehicleTransformPacket::decode);

  @Override
  public Type<? extends CustomPacketPayload> type() { return TYPE; }

  private static void encode(FriendlyByteBuf buf, VehicleTransformPacket p)
  {
    buf.writeLong(p.vehicleUUID.getMostSignificantBits());
    buf.writeLong(p.vehicleUUID.getLeastSignificantBits());
    buf.writeDouble(p.posX); buf.writeDouble(p.posY); buf.writeDouble(p.posZ);
    buf.writeFloat(p.qx); buf.writeFloat(p.qy);
    buf.writeFloat(p.qz); buf.writeFloat(p.qw);
  }

  private static VehicleTransformPacket decode(FriendlyByteBuf buf)
  {
    UUID uuid = new UUID(buf.readLong(), buf.readLong());
    double px = buf.readDouble(), py = buf.readDouble(), pz = buf.readDouble();
    float qx = buf.readFloat(), qy = buf.readFloat();
    float qz = buf.readFloat(), qw = buf.readFloat();
    return new VehicleTransformPacket(uuid, px, py, pz, qx, qy, qz, qw);
  }

  @OnlyIn(Dist.CLIENT)
  public static void handle(VehicleTransformPacket p, IPayloadContext ctx)
  {
    ctx.enqueueWork(() ->
    {
      AbstractVehicleEntity vehicle = VehicleClientCache.getEntity(p.vehicleUUID());
      if (vehicle == null) return;

      Quaternionf newRot = new Quaternionf(p.qx(), p.qy(), p.qz(), p.qw());
      Vec3 newPos = new Vec3(p.posX(), p.posY(), p.posZ());

      boolean isLocalPilot = isLocalPilot(vehicle);

      vehicle.renderPrevPos = vehicle.position();
      vehicle.renderPrevRot = new Quaternionf(vehicle.getVehicleRotation());

      vehicle.renderTargetPos = newPos;
      vehicle.renderTargetRot = newRot;
      vehicle.lerpSteps = isLocalPilot ? 1 : 3;

      vehicle.setPos(p.posX(), p.posY(), p.posZ());
      vehicle.setVehicleRotation(newRot);
    });
  }


  @OnlyIn(Dist.CLIENT)
  private static boolean isLocalPilot(AbstractVehicleEntity vehicle)
  {
    var player = Minecraft.getInstance().player;
    return player != null && player.getVehicle() == vehicle;
  }

  public static VehicleTransformPacket from(AbstractVehicleEntity vehicle)
  {
    Quaternionf q = vehicle.getVehicleRotation();
    return new VehicleTransformPacket(vehicle.getUUID(), vehicle.getX(), vehicle.getY(), vehicle.getZ(), q.x, q.y, q.z, q.w);
  }
}
