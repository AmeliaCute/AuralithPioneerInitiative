package cute.ame.auralithpioneerinitiative.vehicle.Client;

import cute.ame.auralithpioneerinitiative.vehicle.Entity.AbstractVehicleEntity;
import net.minecraft.client.Camera;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class VehicleLodManager
{
  private VehicleLodManager() {}

  private static final double LOD0_MAX_DIST = 64.0;
  private static final double LOD1_MAX_DIST = 192.0;
  private static final double LOD2_MAX_DIST = 512.0;

  public static int getLODLevel(AbstractVehicleEntity entity, Camera camera)
  {
    double dx = entity.getX() - camera.getPosition().x;
    double dy = entity.getY() - camera.getPosition().y;
    double dz = entity.getZ() - camera.getPosition().z;
    double distSq = dx*dx + dy*dy + dz*dz;

    if (distSq < LOD0_MAX_DIST * LOD0_MAX_DIST) return 0;
    if (distSq < LOD1_MAX_DIST * LOD1_MAX_DIST) return 1;
    if (distSq < LOD2_MAX_DIST * LOD2_MAX_DIST) return 2;
    return 3;
  }
}