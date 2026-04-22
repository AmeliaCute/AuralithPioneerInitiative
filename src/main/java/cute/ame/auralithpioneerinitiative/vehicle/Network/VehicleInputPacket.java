package cute.ame.auralithpioneerinitiative.vehicle.Network;


import cute.ame.auralithpioneerinitiative.vehicle.Input.FlightInput;
import cute.ame.auralithpioneerinitiative.vehicle.Types.GroundVehicleEntity;
import cute.ame.auralithpioneerinitiative.vehicle.Types.RocketEntity;
import cute.ame.auralithpioneerinitiative.vehicle.Types.SpaceshipEntity;
import cute.ame.auralithpioneerinitiative.vehicle.Types.SubmarineEntity;
import cute.ame.auralithpioneerinitiative.vehicle.Entity.BlockVehicleEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record VehicleInputPacket(
    UUID  vehicleUUID,
    float thrustX,
    float thrustY,
    float thrustZ,
    float rollInput,
    byte flags
) implements CustomPacketPayload
{

  public static final Type<VehicleInputPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("auralithpioneerinitiative", "vehicle_input"));
  public static final StreamCodec<FriendlyByteBuf, VehicleInputPacket> STREAM_CODEC = StreamCodec.of(VehicleInputPacket::encode, VehicleInputPacket::decode);

  @Override
  public Type<? extends CustomPacketPayload> type() { return TYPE; }

  public boolean boosting() { return (flags & 0x01) != 0; }
  public boolean dismounting() { return (flags & 0x02) != 0; }

  private static void encode(FriendlyByteBuf buf, VehicleInputPacket p)
  {
    buf.writeLong(p.vehicleUUID.getMostSignificantBits());
    buf.writeLong(p.vehicleUUID.getLeastSignificantBits());
    buf.writeFloat(p.thrustX);
    buf.writeFloat(p.thrustY);
    buf.writeFloat(p.thrustZ);
    buf.writeFloat(p.rollInput);
    buf.writeByte(p.flags);
  }

  private static VehicleInputPacket decode(FriendlyByteBuf buf)
  {
    UUID uuid = new UUID(buf.readLong(), buf.readLong());
    float thrustX = buf.readFloat();
    float thrustY = buf.readFloat();
    float thrustZ = buf.readFloat();
    float roll = buf.readFloat();
    byte flags = buf.readByte();
    return new VehicleInputPacket(uuid, thrustX, thrustY, thrustZ, roll, flags);
  }

  public static void handle(VehicleInputPacket p, IPayloadContext ctx)
  {
    ctx.enqueueWork(() ->
    {
      if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;
      if (!(serverPlayer.getVehicle() instanceof BlockVehicleEntity vehicle)) return;
      if (!vehicle.getUUID().equals(p.vehicleUUID())) return;
      if (p.dismounting())
      {
        serverPlayer.stopRiding();
        return;
      }

      FlightInput input = new FlightInput(clamp(p.thrustX()), clamp(p.thrustY()), clamp(p.thrustZ()), 0f, 0f, clamp(p.rollInput()), p.boosting(), false);

      switch (vehicle)
      {
        case SpaceshipEntity ship -> ship.applyFlightInput(input);
        case RocketEntity rocket -> rocket.applyFlightInput(input);
        case SubmarineEntity sub -> sub.applyFlightInput(input);
        case GroundVehicleEntity gv -> gv.applyFlightInput(input);
        default -> {}
      }
    });
  }

  public static VehicleInputPacket build(UUID vehicleUUID, float thrustX, float thrustY, float thrustZ, float rollInput, boolean boosting, boolean dismounting)
  {
    byte flags = 0;
    if (boosting) flags |= 0x01;
    if (dismounting) flags |= 0x02;
    return new VehicleInputPacket(vehicleUUID, clamp(thrustX), clamp(thrustY), clamp(thrustZ), clamp(rollInput), flags);
  }

  private static float clamp(float v) { return Math.max(-1f, Math.min(1f, v)); }
}