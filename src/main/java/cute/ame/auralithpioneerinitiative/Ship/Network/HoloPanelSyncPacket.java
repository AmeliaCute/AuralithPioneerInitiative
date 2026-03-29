package cute.ame.auralithpioneerinitiative.Ship.Network;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.HoloPanel.HoloPanelClientState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record HoloPanelSyncPacket
(
    BlockPos blockPos,
    CompoundTag data
) implements CustomPacketPayload
{
  public static final Type<HoloPanelSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "holopanel_sync"));
  public static final StreamCodec<FriendlyByteBuf, HoloPanelSyncPacket> STREAM_CODEC = StreamCodec.of(HoloPanelSyncPacket::encode, HoloPanelSyncPacket::decode);

  @Override
  public Type<? extends CustomPacketPayload> type() { return TYPE; }

  private static void encode(FriendlyByteBuf buf, HoloPanelSyncPacket p)
  {
    buf.writeBlockPos(p.blockPos());
    buf.writeNbt(p.data());
  }

  private static HoloPanelSyncPacket decode(FriendlyByteBuf buf)
  {
    BlockPos pos = buf.readBlockPos();
    CompoundTag d = buf.readNbt();
    return new HoloPanelSyncPacket(pos, d == null ? new CompoundTag() : d);
  }

  @OnlyIn(Dist.CLIENT)
  public static void handle(HoloPanelSyncPacket packet, IPayloadContext ctx)
  {
    ctx.enqueueWork(() -> HoloPanelClientState.getInstance().update(packet.blockPos(), packet.data()));
  }
}
