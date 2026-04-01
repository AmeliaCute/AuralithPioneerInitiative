package cute.ame.auralithpioneerinitiative.SpaceSuit.Network;


import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Registrie.ModAttachments;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Data.SuitData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SuitSyncPacket
(
    int o2,
    int energy,
    boolean flashlight,
    boolean helmet
) implements CustomPacketPayload
{
  public static final Type<SuitSyncPacket> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "suit_sync"));

  public static final StreamCodec<RegistryFriendlyByteBuf, SuitSyncPacket> STREAM_CODEC =
    StreamCodec.composite(
      ByteBufCodecs.INT, SuitSyncPacket::o2,
      ByteBufCodecs.INT, SuitSyncPacket::energy,
      ByteBufCodecs.BOOL, SuitSyncPacket::flashlight,
      ByteBufCodecs.BOOL, SuitSyncPacket::helmet,
      SuitSyncPacket::new
    );

  @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

  public static void handle(SuitSyncPacket pkt, IPayloadContext ctx)
  {
    ctx.enqueueWork(() ->
    {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) return;

      SuitData data = mc.player.getData(ModAttachments.SUIT_DATA);
      data.setO2(pkt.o2());
      data.setEnergy(pkt.energy());
      data.setFlashlight(pkt.flashlight());
      data.setHelmetOn(pkt.helmet());
    });
  }

  public static void sendTo(ServerPlayer player)
  {
    SuitData d = player.getData(ModAttachments.SUIT_DATA);
    PacketDistributor.sendToPlayer(player, new SuitSyncPacket(d.getO2(), d.getEnergy(), d.isFlashlight(), d.isHelmetOn()));
  }
}
