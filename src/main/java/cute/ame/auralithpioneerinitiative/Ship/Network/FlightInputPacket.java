package cute.ame.auralithpioneerinitiative.Ship.Network;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity;
import cute.ame.auralithpioneerinitiative.Ship.Physics.FlightInput;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FlightInputPacket(
    float thrustX,
    float thrustY,
    float thrustZ,
    float rotPitch,
    float rotYaw,
    float rotRoll,
    boolean boosting,
    boolean dismounting
) implements CustomPacketPayload
{
  public static final Type<FlightInputPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "flight_input"));
  public static final StreamCodec<FriendlyByteBuf, FlightInputPacket> STREAM_CODEC = StreamCodec.of(FlightInputPacket::encode, FlightInputPacket::decode);

  @Override
  public Type<? extends CustomPacketPayload> type() { return TYPE; }

  private static void encode(FriendlyByteBuf buf, FlightInputPacket p)
  {
    buf.writeFloat(p.thrustX);
    buf.writeFloat(p.thrustY);
    buf.writeFloat(p.thrustZ);
    buf.writeFloat(p.rotPitch);
    buf.writeFloat(p.rotYaw);
    buf.writeFloat(p.rotRoll);
    byte flags = 0;
    if (p.boosting) flags |= 0x01;
    if (p.dismounting) flags |= 0x02;
    buf.writeByte(flags);
  }

  private static FlightInputPacket decode(FriendlyByteBuf buf)
  {
    float tX = buf.readFloat();
    float tY = buf.readFloat();
    float tZ = buf.readFloat();
    float rP = buf.readFloat();
    float rY = buf.readFloat();
    float rR = buf.readFloat();
    byte  flags = buf.readByte();
    return new FlightInputPacket(tX, tY, tZ, rP, rY, rR,
        (flags & 0x01) != 0,
        (flags & 0x02) != 0);
  }

  public static void handle(FlightInputPacket packet, IPayloadContext ctx)
  {
    ctx.enqueueWork(() ->
    {
      if (!(ctx.player() instanceof ServerPlayer player)) return;

      Entity vehicle = player.getVehicle();
      if (!(vehicle instanceof ShipEntity ship)) return;

      if (packet.dismounting())
      {
        player.stopRiding();
        return;
      }

      ship.setLastFlightInput(new FlightInput(
          clamp(packet.thrustX()),
          clamp(packet.thrustY()),
          clamp(packet.thrustZ()),
          clamp(packet.rotPitch()),
          clamp(packet.rotYaw()),
          clamp(packet.rotRoll()),
          packet.boosting(),
          false
      ));
      ship.resetFlightInputAge();
    });
  }

  private static float clamp(float v) { return Math.max(-1f, Math.min(1f, v)); }
}