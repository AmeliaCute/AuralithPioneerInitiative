package cute.ame.auralithpioneerinitiative.Ship.Physics;

import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class ShipTransform
{
  private ShipTransform() {}

  public static Vec3 shipToWorld(Vec3 localPos, Vec3 shipWorldPos, Quaternionf q)
  {
    Vector3f v = new Vector3f((float) localPos.x, (float) localPos.y, (float) localPos.z);
    q.transform(v);
    return new Vec3(shipWorldPos.x + v.x, shipWorldPos.y + v.y, shipWorldPos.z + v.z);
  }

  public static Vec3 worldToShip(Vec3 worldPos, Vec3 shipWorldPos, Quaternionf q)
  {
    float dx = (float)(worldPos.x - shipWorldPos.x);
    float dy = (float)(worldPos.y - shipWorldPos.y);
    float dz = (float)(worldPos.z - shipWorldPos.z);
    Vector3f v = new Vector3f(dx, dy, dz);
    new Quaternionf(q).conjugate().transform(v);
    return new Vec3(v.x, v.y, v.z);
  }

  public static Vector3f dirShipToWorld(Vector3f localDir, Quaternionf q)
  {
    Vector3f result = new Vector3f(localDir);
    q.transform(result);
    return result;
  }

  public static Vector3f dirWorldToShip(Vector3f worldDir, Quaternionf q)
  {
    Vector3f result = new Vector3f(worldDir);
    new Quaternionf(q).conjugate().transform(result);
    return result;
  }
}