package cute.ame.auralithpioneerinitiative.Ship.Network;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record HoloPanelClickPacket(
  ResourceLocation panelType,
  float u,
  float v,
  int button,
  String action
) implements CustomPacketPayload
{

  public static final Type<HoloPanelClickPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "holopanel_click"));
  public static final StreamCodec<FriendlyByteBuf, HoloPanelClickPacket> STREAM_CODEC = StreamCodec.of(HoloPanelClickPacket::encode, HoloPanelClickPacket::decode);

  @Override
  public Type<? extends CustomPacketPayload> type() { return TYPE; }

  private static void encode(FriendlyByteBuf buf, HoloPanelClickPacket p)
  {
    buf.writeResourceLocation(p.panelType());
    buf.writeFloat(p.u());
    buf.writeFloat(p.v());
    buf.writeByte(p.button());
    buf.writeUtf(p.action(), 256);
  }

  private static HoloPanelClickPacket decode(FriendlyByteBuf buf)
  {
    ResourceLocation pt = buf.readResourceLocation();
    float u = buf.readFloat();
    float v = buf.readFloat();
    int bt = buf.readByte() & 0xFF;
    String a = buf.readUtf(256);
    return new HoloPanelClickPacket(pt, u, v, bt, a);
  }

  public static void handle(HoloPanelClickPacket packet, IPayloadContext ctx)
  {
    ctx.enqueueWork(() ->
    {
      if (!(ctx.player() instanceof ServerPlayer player)) return;
      if (!packet.action().isEmpty()) HoloPanelActionRegistry.dispatch(packet.panelType(), packet.action(), player);
      else
      {
        // TODO:
        // LEGACY MODE, no need to be handled for now
      }
    });
  }
}