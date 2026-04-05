package cute.ame.auralithpioneerinitiative.SpaceSuit.Network;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Registrie.ModAttachments;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Data.SuitData;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Item.SuitArmorItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FlashlightTogglePacket() implements CustomPacketPayload
{

  public static final Type<FlashlightTogglePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "flashlight_toggle"));
  public static final StreamCodec<RegistryFriendlyByteBuf, FlashlightTogglePacket> STREAM_CODEC = StreamCodec.unit(new FlashlightTogglePacket());

  @Override
  public Type<? extends CustomPacketPayload> type() { return TYPE; }

  public static void handle(FlashlightTogglePacket pkt, IPayloadContext ctx)
  {
    ctx.enqueueWork(() ->
    {
      if (!(ctx.player() instanceof ServerPlayer player)) return;
      if (!(player.getInventory().armor.get(3).getItem() instanceof SuitArmorItem)) return;

      SuitData data = player.getData(ModAttachments.SUIT_DATA);
      if (data.getEnergy() > 0 || data.isFlashlight()) data.setFlashlight(!data.isFlashlight());
    });
  }
}