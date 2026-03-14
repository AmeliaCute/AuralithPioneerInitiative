package cute.ame.auralithpioneerinitiative.Ship.Physics;

import cute.ame.auralithpioneerinitiative.API.AuralithAPI;
import cute.ame.auralithpioneerinitiative.Ship.Data.ShipDefinition;
import cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
    thrustAccel  = Math.max(0.005f, Math.min(0.25f, 60_000f / (mass * 1000f)));
    maxSpeed = maxSpeedForClass(def.shipClass());
    int[] sz = def.computeSize();
    float dia = (float) Math.sqrt(sz[0]*sz[0] + sz[1]*sz[1] + sz[2]*sz[2]);
    float moi = Math.max(1f, mass * (dia * dia) / 20f);
    angularAccel = Math.max(0.0005f, Math.min(0.02f, 1_000f / moi));
  }

  public void integrate(ShipEntity ship, FlightInput input)
  {
    float boostMul = input.boosting() ? 3.0f : 1.0f;
    Vector3f localT = new Vector3f(
        input.thrustX() * thrustAccel * boostMul,
        input.thrustY() * thrustAccel * boostMul,
        input.thrustZ() * thrustAccel * boostMul
    );
    ship.getShipRotation().transform(localT);

    float g = AuralithAPI.getGravityFor(ship.level().dimension());
    Vec3 worldThrust = new Vec3(localT.x, localT.y - g * 0.002, localT.z);

    linearVelocity = linearVelocity.add(worldThrust);
    linearVelocity = linearVelocity.scale(1.0 - (g > 0f ? 0.025 : 0.002));
    double speedCap = input.boosting() ? maxSpeed * 3.0 : maxSpeed;
    if (linearVelocity.lengthSqr() > speedCap * speedCap)
      linearVelocity = linearVelocity.normalize().scale(speedCap);

    angularVelocity = angularVelocity.add(input.rotPitch() * angularAccel, input.rotYaw() * angularAccel, input.rotRoll() * angularAccel).scale(1.0 - ANGULAR_DRAG);
    Quaternionf dq = angularVelocityToQuaternion(angularVelocity);
    ship.setShipRotation(new Quaternionf(ship.getShipRotation()).mul(dq).normalize());

    moveWithCollision(ship);
  }

  private void moveWithCollision(ShipEntity ship)
  {
    double dx = linearVelocity.x;
    double dy = linearVelocity.y;
    double dz = linearVelocity.z;

    AABB bb = ship.getBoundingBox();

    if (dy != 0)
    {
      AABB probe = (dy < 0)
          ? new AABB(bb.minX, bb.minY + dy, bb.minZ, bb.maxX, bb.minY, bb.maxZ)
          : new AABB(bb.minX, bb.maxY, bb.minZ, bb.maxX, bb.maxY + dy, bb.maxZ);
      if (hasBlockCollision(ship, probe)) { dy = 0; linearVelocity = new Vec3(linearVelocity.x, 0, linearVelocity.z); }
    }

    if (dx != 0)
    {
      AABB probe = (dx < 0)
          ? new AABB(bb.minX + dx, bb.minY, bb.minZ, bb.minX, bb.maxY, bb.maxZ)
          : new AABB(bb.maxX, bb.minY, bb.minZ, bb.maxX + dx, bb.maxY, bb.maxZ);
      if (hasBlockCollision(ship, probe)) { dx = 0; linearVelocity = new Vec3(0, linearVelocity.y, linearVelocity.z); }
    }

    if (dz != 0)
    {
      AABB probe = (dz < 0)
          ? new AABB(bb.minX, bb.minY, bb.minZ + dz, bb.maxX, bb.maxY, bb.minZ)
          : new AABB(bb.minX, bb.minY, bb.maxZ, bb.maxX, bb.maxY, bb.maxZ + dz);
      if (hasBlockCollision(ship, probe)) { dz = 0; linearVelocity = new Vec3(linearVelocity.x, linearVelocity.y, 0); }
    }

    ship.setPos(ship.getX() + dx, ship.getY() + dy, ship.getZ() + dz);
  }

  private static boolean hasBlockCollision(ShipEntity ship, AABB probe)
  {
    return ship.level().getBlockCollisions(ship, probe).iterator().hasNext();
  }

  private static Quaternionf angularVelocityToQuaternion(Vec3 av)
  {
    double len = av.length();
    if (len < 1e-9) return new Quaternionf();
    double halfAngle = len * 0.5;
    float  s = (float)(Math.sin(halfAngle) / len);
    return new Quaternionf((float)(av.x*s), (float)(av.y*s), (float)(av.z*s), (float)Math.cos(halfAngle));
  }

  public boolean isMoving() { return linearVelocity.lengthSqr() > 1e-6 || angularVelocity.lengthSqr() > 1e-8; }

  private static float maxSpeedForClass(String cls)
  {
    return switch (cls == null ? "" : cls.toLowerCase(java.util.Locale.ROOT))
    {
      case "heavy_fighter" -> 2.8f;
      case "corvette" -> 3.2f;
      case "frigate" -> 2.0f;
      case "cargo" -> 1.2f;
      case "capital" -> 0.6f;
      default -> 4.0f;
    };
  }

  public void save(CompoundTag tag)
  {
    tag.putDouble("phsLvX", linearVelocity.x);  tag.putDouble("phsLvY", linearVelocity.y);  tag.putDouble("phsLvZ", linearVelocity.z);
    tag.putDouble("phsAvX", angularVelocity.x); tag.putDouble("phsAvY", angularVelocity.y); tag.putDouble("phsAvZ", angularVelocity.z);
  }

  public void load(CompoundTag tag)
  {
    if (!tag.contains("phsLvX")) return;
    linearVelocity  = new Vec3(tag.getDouble("phsLvX"), tag.getDouble("phsLvY"), tag.getDouble("phsLvZ"));
    angularVelocity = new Vec3(tag.getDouble("phsAvX"), tag.getDouble("phsAvY"), tag.getDouble("phsAvZ"));
  }

  public Vec3 getLinearVelocity()        { return linearVelocity;  }
  public Vec3 getAngularVelocity()       { return angularVelocity; }
  public void setLinearVelocity(Vec3 v)  { linearVelocity  = v;   }
  public void setAngularVelocity(Vec3 v) { angularVelocity = v;   }
}