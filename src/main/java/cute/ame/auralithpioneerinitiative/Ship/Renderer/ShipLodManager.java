package cute.ame.auralithpioneerinitiative.Ship.Renderer;

import cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity;
import net.minecraft.client.Camera;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ShipLodManager
{
    private ShipLodManager() {}

    public static int getLODLevel(ShipEntity entity, Camera camera)
    {
        double dx = entity.getX() - camera.getPosition().x;
        double dy = entity.getY() - camera.getPosition().y;
        double dz = entity.getZ() - camera.getPosition().z;
        double distSq = dx*dx + dy*dy + dz*dz;

        if (distSq < BakedShipMesh.LOD0_MAX_DIST * BakedShipMesh.LOD0_MAX_DIST) return 0;
        if (distSq < BakedShipMesh.LOD1_MAX_DIST * BakedShipMesh.LOD1_MAX_DIST) return 1;
        if (distSq < BakedShipMesh.LOD2_MAX_DIST * BakedShipMesh.LOD2_MAX_DIST) return 2;
        return 3;
    }
}