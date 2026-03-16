package cute.ame.auralithpioneerinitiative.Ship.Physics;

import cute.ame.auralithpioneerinitiative.API.AuralithAPI;
import cute.ame.auralithpioneerinitiative.Ship.Data.ShipDefinition;
import cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class ShipPhysics
{
  private Vec3 linearVelocity = Vec3.ZERO;
  private Vec3 angularVelocity = Vec3.ZERO;

  private float thrustAccel = 0.06f;
  private float maxSpeed = 4.0f;
  private float angularAccel = 0.006f;
  private static final float ANGULAR_DRAG = 0.10f;

  public void loadFromDefinition(ShipDefinition def)
  {
    if (def == null) return;
    float mass = Math.max(1f, def.mass());
    thrustAccel = Math.max(0.005f, Math.min(0.25f, 60_000f / (mass * 1000f)));
    maxSpeed = maxSpeedForClass(def.shipClass()) * 16.0f;
    int[] sz = def.computeSize();
    float dia = (float) Math.sqrt(sz[0]*sz[0] + sz[1]*sz[1] + sz[2]*sz[2]);
    float moi = Math.max(1f, mass * (dia * dia) / 20f);
    angularAccel = Math.max(0.0005f, Math.min(0.02f, 1_000f / moi));
  }

  public void integrate(ShipEntity ship, FlightInput input)
  {
    float boostMul = input.boosting() ? 3.0f : 1.0f;
    Vector3f localT = new Vector3f(input.thrustX() * thrustAccel * boostMul, input.thrustY() * thrustAccel * boostMul, input.thrustZ() * thrustAccel * boostMul);
    ship.getShipRotation().transform(localT);

    float g = AuralithAPI.getGravityFor(ship.level().dimension());
    Vec3 worldThrust = new Vec3(localT.x, localT.y - g * 0.002, localT.z);

    linearVelocity = linearVelocity.add(worldThrust);
    linearVelocity = linearVelocity.scale(1.0 - (g > 0f ? 0.025 : 0.002));
    double speedCap = input.boosting() ? maxSpeed * 3.0 : maxSpeed;
    if (linearVelocity.lengthSqr() > speedCap * speedCap) linearVelocity = linearVelocity.normalize().scale(speedCap);

    angularVelocity = angularVelocity.add(input.rotPitch() * angularAccel, input.rotYaw() * angularAccel, input.rotRoll() * angularAccel).scale(1.0 - ANGULAR_DRAG);
    Quaternionf dq = angularVelocityToQuaternion(angularVelocity);
    ship.setShipRotation(new Quaternionf(ship.getShipRotation()).mul(dq).normalize());
    moveWithOBBCollision(ship);
  }

  private void moveWithOBBCollision(ShipEntity ship)
  {
    double dx = linearVelocity.x;
    double dy = linearVelocity.y;
    double dz = linearVelocity.z;

    Quaternionf q = ship.getShipRotation();
    AABB localBounds = getLocalBounds(ship);

    float localCx = (float)((localBounds.minX + localBounds.maxX) * 0.5);
    float localCy = (float)((localBounds.minY + localBounds.maxY) * 0.5);
    float localCz = (float)((localBounds.minZ + localBounds.maxZ) * 0.5);

    float hx = (float)((localBounds.maxX - localBounds.minX) * 0.5);
    float hy = (float)((localBounds.maxY - localBounds.minY) * 0.5);
    float hz = (float)((localBounds.maxZ - localBounds.minZ) * 0.5);

    Vector3f obbOffsetWS = new Vector3f(localCx, localCy, localCz);
    q.transform(obbOffsetWS);

    double obbWx = ship.getX() + obbOffsetWS.x;
    double obbWy = ship.getY() + obbOffsetWS.y;
    double obbWz = ship.getZ() + obbOffsetWS.z;

    Vector3f axX = new Vector3f(1, 0, 0); q.transform(axX);
    Vector3f axY = new Vector3f(0, 1, 0); q.transform(axY);
    Vector3f axZ = new Vector3f(0, 0, 1); q.transform(axZ);

    AABB worldAABB = ship.getBoundingBox();
    if (dy != 0)
    {
      boolean collides = obbVsWorldBlocks(ship, obbWx, obbWy + dy, obbWz, axX, axY, axZ, hx, hy, hz, worldAABB.move(0, dy, 0));
      if (collides)
      {
        dy = 0;
        linearVelocity = new Vec3(linearVelocity.x, 0, linearVelocity.z);
      }
    }

    if (dx != 0)
    {
      boolean collides = obbVsWorldBlocks(ship, obbWx + dx, obbWy, obbWz, axX, axY, axZ, hx, hy, hz, worldAABB.move(dx, 0, 0));
      if (collides)
      {
        dx = 0;
        linearVelocity = new Vec3(0, linearVelocity.y, linearVelocity.z);
      }
    }

    if (dz != 0)
    {
      boolean collides = obbVsWorldBlocks(ship, obbWx, obbWy, obbWz + dz, axX, axY, axZ, hx, hy, hz, worldAABB.move(0, 0, dz));
      if (collides)
      {
        dz = 0;
        linearVelocity = new Vec3(linearVelocity.x, linearVelocity.y, 0);
      }
    }

    ship.setPos(ship.getX() + dx, ship.getY() + dy, ship.getZ() + dz);
  }

  private boolean obbVsWorldBlocks(ShipEntity ship, double cx, double cy, double cz, Vector3f axX, Vector3f axY, Vector3f axZ, float hx, float hy, float hz, AABB queryAABB)
  {
    AABB query = queryAABB.inflate(0.05);
    for (VoxelShape shape : (Iterable<VoxelShape>) (() -> ship.level().getBlockCollisions(ship, query).iterator()))
    {
      if (shape.isEmpty()) continue;
      AABB blockBB = shape.bounds();

      ShipOBBCollision.Manifold m = ShipOBBCollision.test(
          cx, cy, cz,
          axX.x, axX.y, axX.z,
          axY.x, axY.y, axY.z,
          axZ.x, axZ.y, axZ.z,
          hx, hy, hz,
          blockBB
      );
      if (m != null) return true;
    }
    return false;
  }

  public static AABB getLocalBounds(ShipEntity ship)
  {
    var snapshot = ship.getBlockSnapshot();
    if (!snapshot.isEmpty())
    {
      int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
      int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
      for (var e : snapshot)
      {
        if (e.relX() < minX) minX = e.relX();
        if (e.relX() + 1 > maxX) maxX = e.relX() + 1;
        if (e.relY() < minY) minY = e.relY();
        if (e.relY() + 1 > maxY) maxY = e.relY() + 1;
        if (e.relZ() < minZ) minZ = e.relZ();
        if (e.relZ() + 1 > maxZ) maxZ = e.relZ() + 1;
      }
      return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }
    return ship.getDefinition()
        .map(def ->
        {
          int[] sz = def.computeSize();
          return new AABB(-sz[0] * 0.5, 0, -sz[2] * 0.5, sz[0] * 0.5, sz[1], sz[2] * 0.5);
        }).orElse(new AABB(-1.5, 0, -1.5, 1.5, 2, 1.5));
  }

  private static Quaternionf angularVelocityToQuaternion(Vec3 av)
  {
    double len = av.length();
    if (len < 1e-9) return new Quaternionf();
    double halfAngle = len * 0.5;
    float  s         = (float)(Math.sin(halfAngle) / len);
    return new Quaternionf((float)(av.x*s), (float)(av.y*s), (float)(av.z*s), (float)Math.cos(halfAngle));
  }

  public boolean isMoving()
  {
    return linearVelocity.lengthSqr() > 1e-6 || angularVelocity.lengthSqr() > 1e-8;
  }

  private static float maxSpeedForClass(String cls)
  {
    return switch (cls == null ? "" : cls.toLowerCase(java.util.Locale.ROOT))
    {
      case "heavy_fighter" -> 2.8f;
      case "corvette" -> 3.2f;
      case "frigate" -> 2.0f;
      case "cargo" -> 1.2f;
      case "capital" -> 0.6f;
      default -> 4.0f; // interceptor is default
    };
  }

  public void save(CompoundTag tag)
  {
    tag.putDouble("phsLvX", linearVelocity.x);
    tag.putDouble("phsLvY", linearVelocity.y);
    tag.putDouble("phsLvZ", linearVelocity.z);
    tag.putDouble("phsAvX", angularVelocity.x);
    tag.putDouble("phsAvY", angularVelocity.y);
    tag.putDouble("phsAvZ", angularVelocity.z);
  }

  public void load(CompoundTag tag)
  {
    if (!tag.contains("phsLvX")) return;
    linearVelocity  = new Vec3(tag.getDouble("phsLvX"), tag.getDouble("phsLvY"), tag.getDouble("phsLvZ"));
    angularVelocity = new Vec3(tag.getDouble("phsAvX"), tag.getDouble("phsAvY"), tag.getDouble("phsAvZ"));
  }

  public Vec3 getLinearVelocity()       { return linearVelocity;  }
  public Vec3 getAngularVelocity()      { return angularVelocity; }
  public void setLinearVelocity(Vec3 v) { linearVelocity  = v;    }
  public void setAngularVelocity(Vec3 v){ angularVelocity = v;    }
}