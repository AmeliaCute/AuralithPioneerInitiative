package cute.ame.auralithpioneerinitiative.SpaceSuit.Network;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Registrie.ModMenuTypes;
import cute.ame.auralithpioneerinitiative.SpaceSuit.GUI.SuitContainerMenu;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Item.SuitArmorItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenSuitMenuPacket() implements CustomPacketPayload {

  public static final Type<OpenSuitMenuPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "open_suit_menu"));

  public static final StreamCodec<RegistryFriendlyByteBuf, OpenSuitMenuPacket> STREAM_CODEC = StreamCodec.unit(new OpenSuitMenuPacket());

  @Override
  public Type<? extends CustomPacketPayload> type() { return TYPE; }

  public static void handle(OpenSuitMenuPacket pkt, IPayloadContext ctx)
  {
    ctx.enqueueWork(() -> {
      if (!(ctx.player() instanceof ServerPlayer player)) return;

      boolean hasSuit = player.getInventory().armor.stream().anyMatch(s -> s.getItem() instanceof SuitArmorItem);
      if (!hasSuit) return;

      player.openMenu(new SimpleMenuProvider((id, inv, p) -> new SuitContainerMenu(id, inv), Component.literal("Suit Equipment")));
    });
  }
}