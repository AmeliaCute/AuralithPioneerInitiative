package cute.ame.auralithpioneerinitiative.Ship.Physics;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.UUID;

public record ShipTransformData(
    UUID shipId,
    Vec3 worldPos,
    Quaternionf rotation,
    AABB localBounds,
    int shipyardSlot
)
{
  public ShipTransformData(UUID shipId, Vec3 worldPos, Quaternionf rotation, AABB localBounds)
  {
    this(shipId, worldPos, rotation, localBounds, -1);
  }

  public Vec3 toWorld(Vec3 local)
  {
    return ShipTransform.shipToWorld(local, worldPos, rotation);
  }
  public Vec3 toShip(Vec3 world)
  {
    return ShipTransform.worldToShip(world, worldPos, rotation);
  }

  public boolean containsWorldPos(Vec3 worldPoint)
  {
    if (localBounds == null) return false;
    Vec3 local = toShip(worldPoint);
    return localBounds.contains(local);
  }

  public boolean hasShipyard()
  {
    return shipyardSlot >= 0;
  }
}