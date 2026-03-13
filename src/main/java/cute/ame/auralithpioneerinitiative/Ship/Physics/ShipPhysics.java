package cute.ame.auralithpioneerinitiative.Ship.Physics;

import cute.ame.auralithpioneerinitiative.API.AuralithAPI;
import cute.ame.auralithpioneerinitiative.Ship.Data.ShipDefinition;
import cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity;
import net.minecraft.nbt.CompoundTag;
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

    thrustAccel = Math.max(0.005f, 60_000f / (mass * 1000f));
    thrustAccel = Math.min(thrustAccel, 0.25f);

    maxSpeed = maxSpeedForClass(def.shipClass());

    int[] sz  = def.computeSize();
    float dia = (float) Math.sqrt(sz[0] * sz[0] + sz[1] * sz[1] + sz[2] * sz[2]);
    float moi = Math.max(1f, mass * (dia * dia) / 20f);
    angularAccel = Math.max(0.0005f, Math.min(0.02f, 1_000f / moi));
  }

  public void integrate(ShipEntity ship, FlightInput input)
  {
    float boostMul = input.boosting() ? 3.0f : 1.0f;
    Vector3f localT = new Vector3f(input.thrustX() * thrustAccel * boostMul, input.thrustY() * thrustAccel * boostMul, input.thrustZ() * thrustAccel * boostMul);

    ship.getShipRotation().transform(localT);
    Vec3 worldThrust = new Vec3(localT.x, localT.y, localT.z);

    float g = AuralithAPI.getGravityFor(ship.level().dimension());
    worldThrust = worldThrust.add(0.0, -g * 0.002, 0.0);

    linearVelocity = linearVelocity.add(worldThrust);

    double linearDrag = (g > 0.0f) ? 0.025 : 0.002;
    linearVelocity = linearVelocity.scale(1.0 - linearDrag);

    double speedCap = input.boosting() ? maxSpeed * 3.0 : maxSpeed;
    double speedSq = linearVelocity.lengthSqr();
    if (speedSq > speedCap * speedCap) linearVelocity = linearVelocity.normalize().scale(speedCap);

    Vec3 torque = new Vec3(input.rotPitch() * angularAccel, input.rotYaw()   * angularAccel, input.rotRoll()  * angularAccel);
    angularVelocity = angularVelocity.add(torque).scale(1.0 - ANGULAR_DRAG);
    Quaternionf dq = angularVelocityToQuaternion(angularVelocity);
    Quaternionf newRot = new Quaternionf(ship.getShipRotation()).mul(dq).normalize();
    ship.setShipRotation(newRot);

    ship.noPhysics = false;
    ship.move(net.minecraft.world.entity.MoverType.SELF, new Vec3(linearVelocity.x, linearVelocity.y, linearVelocity.z));
    ship.noPhysics = true;

    if (ship.verticalCollision) linearVelocity = new Vec3(linearVelocity.x, 0.0, linearVelocity.z);
    if (ship.horizontalCollision) linearVelocity = new Vec3(0.0, linearVelocity.y, 0.0);
  }

  private static Quaternionf angularVelocityToQuaternion(Vec3 av)
  {
    double len = av.length();
    if (len < 1e-9) return new Quaternionf();
    double halfAngle = len * 0.5;
    float s = (float) (Math.sin(halfAngle) / len);
    return new Quaternionf(
        (float) (av.x * s),
        (float) (av.y * s),
        (float) (av.z * s),
        (float)  Math.cos(halfAngle)
    );
  }

  public boolean isMoving()
  {
    return linearVelocity.lengthSqr()  > 1e-6 || angularVelocity.lengthSqr() > 1e-8;
  }

  private static float maxSpeedForClass(String cls)
  {
    return switch (cls == null ? "" : cls.toLowerCase(java.util.Locale.ROOT))
    {
      case "interceptor" -> 4.0f; // redundant
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

  public Vec3 getLinearVelocity()  { return linearVelocity;  }
  public Vec3 getAngularVelocity() { return angularVelocity; }
  public void setLinearVelocity(Vec3 v)  { linearVelocity  = v; }
  public void setAngularVelocity(Vec3 v) { angularVelocity = v; }
}