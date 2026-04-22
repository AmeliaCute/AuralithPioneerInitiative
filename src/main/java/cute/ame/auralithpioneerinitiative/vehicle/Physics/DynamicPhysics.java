package cute.ame.auralithpioneerinitiative.vehicle.Physics;

import cute.ame.auralithpioneerinitiative.vehicle.Entity.AbstractVehicleEntity;
import cute.ame.auralithpioneerinitiative.vehicle.Input.FlightInput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class DynamicPhysics
{
  private Vec3 linearVelocity  = Vec3.ZERO;
  private Vec3 angularVelocity = Vec3.ZERO;

  public void integrate(AbstractVehicleEntity vehicle, VehicleCapabilities caps, FlightInput input, float gravityScale)
  {
    float boostMul = input.boosting() ? 2.5f : 1.0f;
    float mass = Math.max(1f, caps.totalMass);
    float moi = Math.max(1f, caps.momentOfInertia);

    Vec3 netForce = Vec3.ZERO;
    Vec3 netTorque = Vec3.ZERO;

    if (!caps.thrusters.isEmpty() && caps.hasFuel())
    {
      Vec3 forwardDir = transformDir(vehicle, new Vector3f(0, 0, -1));
      Vec3 rightDir = transformDir(vehicle, new Vector3f(1, 0, 0));
      Vec3 upDir = transformDir(vehicle, new Vector3f(0, 1, 0));

      for (VehicleCapabilities.ThrusterMount t : caps.thrusters)
      {
        float throttle = computeThrottle(t, input, forwardDir, rightDir, upDir, boostMul);
        if (throttle <= 0) continue;

        Vector3f thrustDirWorld = new Vector3f(t.direction());
        vehicle.getVehicleRotation().transform(thrustDirWorld);
        Vec3 forceW = new Vec3(thrustDirWorld.x * t.force() * throttle / mass, thrustDirWorld.y * t.force() * throttle / mass, thrustDirWorld.z * t.force() * throttle / mass);
        netForce = netForce.add(forceW);

        Vec3 arm = t.torqueArm(caps.centerOfMass);
        Vec3 force = new Vec3(thrustDirWorld.x * t.force() * throttle, thrustDirWorld.y * t.force() * throttle, thrustDirWorld.z * t.force() * throttle);
        netTorque = netTorque.add(cross(arm, force).scale(1.0 / moi));

        caps.currentFuel = Math.max(0f, caps.currentFuel - t.fuelRate() * throttle);
      }
    }

    if (caps.totalGyroTorque > 0)
    {
      float gyroScale = caps.totalGyroTorque / moi * 0.01f;
      netTorque = netTorque.add(input.rotPitch() * gyroScale, input.rotYaw()   * gyroScale, input.rotRoll()  * gyroScale);
    }
    else
      netTorque = netTorque.add(input.rotPitch() * caps.estimatedAngularAccel, input.rotYaw()   * caps.estimatedAngularAccel, input.rotRoll()  * caps.estimatedAngularAccel);

    double g = gravityScale * 0.002;
    netForce = netForce.add(0, -g, 0);

    double linearDrag = gravityScale > 0.1f ? 0.025 : 0.002;
    linearVelocity = linearVelocity.add(netForce).scale(1.0 - linearDrag);

    double speedCap = caps.estimatedMaxSpeed * (input.boosting() ? 2.5 : 1.0);
    if (linearVelocity.lengthSqr() > speedCap * speedCap) linearVelocity = linearVelocity.normalize().scale(speedCap);

    double angularDrag = 0.10;
    angularVelocity = angularVelocity.add(netTorque).scale(1.0 - angularDrag);

    double maxAngVel = 0.15;
    if (angularVelocity.lengthSqr() > maxAngVel * maxAngVel) angularVelocity = angularVelocity.normalize().scale(maxAngVel);

    Quaternionf dq = angularVelocityToQuat(angularVelocity);
    vehicle.setVehicleRotation(new Quaternionf(vehicle.getVehicleRotation()).mul(dq).normalize());

    vehicle.setPos(vehicle.getX() + linearVelocity.x, vehicle.getY() + linearVelocity.y, vehicle.getZ() + linearVelocity.z);
    vehicle.setDeltaMovement(linearVelocity);
  }

  private float computeThrottle(VehicleCapabilities.ThrusterMount t, FlightInput input, Vec3 forwardDir, Vec3 rightDir, Vec3 upDir, float boostMul)
  {
    Vector3f dir = t.direction();
    Vec3 dirVec = new Vec3(dir.x, dir.y, dir.z);

    float fwdContrib = (float) dirVec.dot(forwardDir)  * (-input.thrustZ());
    float rightContrib = (float) dirVec.dot(rightDir) * input.thrustX();
    float upContrib = (float) dirVec.dot(upDir) * input.thrustY();

    float total = Math.max(0f, fwdContrib + rightContrib + upContrib);
    return Math.min(1f, total * boostMul);
  }

  public boolean isMoving() {
    return linearVelocity.lengthSqr() > 1e-6 || angularVelocity.lengthSqr() > 1e-8;
  }

  public Vec3 getLinearVelocity() { return linearVelocity; }
  public Vec3 getAngularVelocity() { return angularVelocity; }
  public void setLinearVelocity(Vec3 v) { linearVelocity  = v; }
  public void setAngularVelocity(Vec3 v) { angularVelocity = v; }

  public void save(CompoundTag tag)
  {
    tag.putDouble("dvLvX", linearVelocity.x);
    tag.putDouble("dvLvY", linearVelocity.y);
    tag.putDouble("dvLvZ", linearVelocity.z);
    tag.putDouble("dvAvX", angularVelocity.x);
    tag.putDouble("dvAvY", angularVelocity.y);
    tag.putDouble("dvAvZ", angularVelocity.z);
  }

  public void load(CompoundTag tag)
  {
    if (!tag.contains("dvLvX")) return;
    linearVelocity  = new Vec3(tag.getDouble("dvLvX"), tag.getDouble("dvLvY"), tag.getDouble("dvLvZ"));
    angularVelocity = new Vec3(tag.getDouble("dvAvX"), tag.getDouble("dvAvY"), tag.getDouble("dvAvZ"));
  }

  private static Vec3 transformDir(AbstractVehicleEntity v, Vector3f localDir)
  {
    Vector3f r = new Vector3f(localDir);
    v.getVehicleRotation().transform(r);
    return new Vec3(r.x, r.y, r.z);
  }

  private static Vec3 cross(Vec3 a, Vec3 b)
  {
    return new Vec3(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
  }

  private static Quaternionf angularVelocityToQuat(Vec3 av)
  {
    double len = av.length();
    if (len < 1e-9) return new Quaternionf();

    double halfAngle = len * 0.5;
    float s = (float)(Math.sin(halfAngle) / len);
    return new Quaternionf((float)(av.x*s),(float)(av.y*s),(float)(av.z*s),(float)Math.cos(halfAngle));
  }
}
