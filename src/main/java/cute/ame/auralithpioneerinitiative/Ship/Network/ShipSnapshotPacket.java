package cute.ame.auralithpioneerinitiative.Ship.Network;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity;
import cute.ame.auralithpioneerinitiative.Ship.Renderer.ShipClientCache;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ShipSnapshotPacket(UUID shipUUID, List<BlockEntry> blocks) implements CustomPacketPayload
{
    public record BlockEntry(int relX, int relY, int relZ, int stateId)
    {
        public BlockState resolveState()
        {
            return Block.stateById(stateId);
        }
    }

    public static final Type<ShipSnapshotPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "ship_snapshot"));

    public static final StreamCodec<FriendlyByteBuf, ShipSnapshotPacket> STREAM_CODEC = StreamCodec.of(ShipSnapshotPacket::encode, ShipSnapshotPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    private static void encode(FriendlyByteBuf buf, ShipSnapshotPacket packet)
    {
        buf.writeLong(packet.shipUUID().getMostSignificantBits());
        buf.writeLong(packet.shipUUID().getLeastSignificantBits());
        buf.writeVarInt(packet.blocks().size());
        for (BlockEntry e : packet.blocks())
        {
            buf.writeShort(e.relX());
            buf.writeShort(e.relY());
            buf.writeShort(e.relZ());
            buf.writeVarInt(e.stateId());
        }
    }

    private static ShipSnapshotPacket decode(FriendlyByteBuf buf)
    {
        UUID uuid  = new UUID(buf.readLong(), buf.readLong());
        int  count = buf.readVarInt();
        List<BlockEntry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++)
        {
            int x  = buf.readShort();
            int y  = buf.readShort();
            int z  = buf.readShort();
            int id = buf.readVarInt();
            entries.add(new BlockEntry(x, y, z, id));
        }
        return new ShipSnapshotPacket(uuid, entries);
    }

    @OnlyIn(Dist.CLIENT)
    public static void handle(ShipSnapshotPacket packet, IPayloadContext ctx)
    {
        ctx.enqueueWork(() ->
        {
            ShipClientCache.onSnapshotReceived(packet.shipUUID(), packet.blocks());

            if (Minecraft.getInstance().level != null)
            {
                for (Entity e : Minecraft.getInstance().level.entitiesForRendering())
                {
                    if (e instanceof ShipEntity ship && ship.getUUID().equals(packet.shipUUID()))
                    {
                        ship.setBlockSnapshot(packet.blocks());
                        break;
                    }
                }
            }
        });
    }
}