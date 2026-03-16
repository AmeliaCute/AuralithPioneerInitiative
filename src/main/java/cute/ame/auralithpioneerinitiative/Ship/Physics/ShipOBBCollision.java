package cute.ame.auralithpioneerinitiative.Ship.Physics;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class ShipOBBCollision
{
  private ShipOBBCollision() {}

  public record Manifold(Vec3 normal, double depth) {}

  public static @Nullable Manifold test(
      double cx, double cy, double cz,
      float ux, float uy, float uz,
      float vx, float vy, float vz,
      float wx, float wy, float wz,
      float ex, float ey, float ez,
      AABB  aabb
  )
  {
    double acx = (aabb.minX + aabb.maxX) * 0.5;
    double acy = (aabb.minY + aabb.maxY) * 0.5;
    double acz = (aabb.minZ + aabb.maxZ) * 0.5;
    float  ax  = (float)((aabb.maxX - aabb.minX) * 0.5);
    float  ay  = (float)((aabb.maxY - aabb.minY) * 0.5);
    float  az  = (float)((aabb.maxZ - aabb.minZ) * 0.5);

    float tx = (float)(cx - acx);
    float ty = (float)(cy - acy);
    float tz = (float)(cz - acz);

    float minPen = Float.MAX_VALUE;
    float bestNx = 0, bestNy = 1, bestNz = 0;

    float[][] axes = {
        { ux, uy, uz },
        { vx, vy, vz },
        { wx, wy, wz },
        { 1, 0, 0 },
        { 0, 1, 0 },
        { 0, 0, 1 },
        cross(ux,uy,uz, 1,0,0),
        cross(ux,uy,uz, 0,1,0),
        cross(ux,uy,uz, 0,0,1),
        cross(vx,vy,vz, 1,0,0),
        cross(vx,vy,vz, 0,1,0),
        cross(vx,vy,vz, 0,0,1),
        cross(wx,wy,wz, 1,0,0),
        cross(wx,wy,wz, 0,1,0),
        cross(wx,wy,wz, 0,0,1),
    };

    for (float[] axis : axes)
    {
      float len2 = axis[0]*axis[0] + axis[1]*axis[1] + axis[2]*axis[2];
      if (len2 < 1e-9f) continue;

      float invLen = (float)(1.0 / Math.sqrt(len2));
      float nx_ = axis[0] * invLen;
      float ny_ = axis[1] * invLen;
      float nz_ = axis[2] * invLen;
      float obbR = Math.abs(ex * dot(ux,uy,uz, nx_,ny_,nz_)) + Math.abs(ey * dot(vx,vy,vz, nx_,ny_,nz_)) + Math.abs(ez * dot(wx,wy,wz, nx_,ny_,nz_));
      float aabbR = Math.abs(ax * nx_) + Math.abs(ay * ny_) + Math.abs(az * nz_);
      float tOnAxis = dot(tx, ty, tz, nx_, ny_, nz_);
      float pen = obbR + aabbR - Math.abs(tOnAxis);

      if (pen <= 0f) return null;

      if (pen < minPen)
      {
        minPen = pen;
        float sign = tOnAxis >= 0f ? 1f : -1f;
        bestNx = sign * nx_;
        bestNy = sign * ny_;
        bestNz = sign * nz_;
      }
    }

    return new Manifold(new Vec3(bestNx, bestNy, bestNz), minPen);
  }

  private static float dot(float ax, float ay, float az, float bx, float by, float bz)
  {
    return ax*bx + ay*by + az*bz;
  }

  private static float[] cross(float ax, float ay, float az, float bx, float by, float bz)
  {
    return new float[]{ ay*bz - az*by, az*bx - ax*bz, ax*by - ay*bx };
  }
}