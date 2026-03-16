package cute.ame.auralithpioneerinitiative.Ship.Network;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity;
import cute.ame.auralithpioneerinitiative.Ship.Physics.ShipTransformData;
import cute.ame.auralithpioneerinitiative.Ship.Physics.ShipTransformRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Quaternionf;

import java.util.UUID;

public record ShipTransformPacket(
    UUID   shipUUID,
    double posX, double posY, double posZ,
    float  qx,   float  qy,   float  qz,   float qw,
    float  velX, float  velY, float  velZ,
    float  bMinX, float bMinY, float bMinZ,
    float  bMaxX, float bMaxY, float bMaxZ
) implements CustomPacketPayload
{
    public static final Type<ShipTransformPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "ship_transform")
    );

    public static final StreamCodec<FriendlyByteBuf, ShipTransformPacket> STREAM_CODEC =
            StreamCodec.of(ShipTransformPacket::encode, ShipTransformPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    private static void encode(FriendlyByteBuf buf, ShipTransformPacket p)
    {
        buf.writeLong(p.shipUUID.getMostSignificantBits());
        buf.writeLong(p.shipUUID.getLeastSignificantBits());
        buf.writeDouble(p.posX); buf.writeDouble(p.posY); buf.writeDouble(p.posZ);
        buf.writeFloat(p.qx);   buf.writeFloat(p.qy);    buf.writeFloat(p.qz);  buf.writeFloat(p.qw);
        buf.writeFloat(p.velX); buf.writeFloat(p.velY);  buf.writeFloat(p.velZ);
        buf.writeFloat(p.bMinX); buf.writeFloat(p.bMinY); buf.writeFloat(p.bMinZ);
        buf.writeFloat(p.bMaxX); buf.writeFloat(p.bMaxY); buf.writeFloat(p.bMaxZ);
    }

    private static ShipTransformPacket decode(FriendlyByteBuf buf)
    {
        UUID   uuid = new UUID(buf.readLong(), buf.readLong());
        double px = buf.readDouble(), py = buf.readDouble(), pz = buf.readDouble();
        float  qx = buf.readFloat(), qy = buf.readFloat(), qz = buf.readFloat(), qw = buf.readFloat();
        float  vx = buf.readFloat(), vy = buf.readFloat(), vz = buf.readFloat();
        float  bx0 = buf.readFloat(), by0 = buf.readFloat(), bz0 = buf.readFloat();
        float  bx1 = buf.readFloat(), by1 = buf.readFloat(), bz1 = buf.readFloat();
        return new ShipTransformPacket(uuid, px, py, pz, qx, qy, qz, qw, vx, vy, vz, bx0, by0, bz0, bx1, by1, bz1);
    }

    @OnlyIn(Dist.CLIENT)
    public static void handle(ShipTransformPacket p, IPayloadContext ctx)
    {
        ctx.enqueueWork(() ->
        {
            AABB localBounds = new AABB(p.bMinX, p.bMinY, p.bMinZ, p.bMaxX, p.bMaxY, p.bMaxZ);
            ShipTransformData data = new ShipTransformData(
                    p.shipUUID,
                    new Vec3(p.posX, p.posY, p.posZ),
                    new Quaternionf(p.qx, p.qy, p.qz, p.qw),
                    localBounds
            );
            ShipTransformRegistry.update(p.shipUUID, data);

            if (Minecraft.getInstance().level != null)
            {
                for (Entity e : Minecraft.getInstance().level.entitiesForRendering())
                {
                    if (e instanceof ShipEntity ship && ship.getUUID().equals(p.shipUUID))
                    {
                        ship.lerpTo(p.posX, p.posY, p.posZ, ship.getYRot(), ship.getXRot(), 3);
                        break;
                    }
                }
            }
        });
    }
}
