package cute.ame.auralithpioneerinitiative.vehicle.Physics;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.List;

public final class VehicleCapabilities
{
  public final float totalMass;
  public final Vec3 centerOfMass;
  public final float momentOfInertia;

  public final List<ThrusterMount> thrusters;
  public final float maxTotalThrust;

  public final List<SuspensionMount> suspensions;

  public final float totalFuelCapacity;
  public float currentFuel;

  public final List<RocketStage> rocketStages;
  public final float totalGyroTorque;

  public final float estimatedMaxSpeed;
  public final float estimatedAngularAccel;

  VehicleCapabilities(float totalMass, Vec3 centerOfMass, float momentOfInertia, List<ThrusterMount> thrusters, float maxTotalThrust, List<SuspensionMount> suspensions, float totalFuelCapacity, float currentFuel, List<RocketStage> rocketStages, float totalGyroTorque)
  {
    this.totalMass = totalMass;
    this.centerOfMass = centerOfMass;
    this.momentOfInertia = momentOfInertia;
    this.thrusters = Collections.unmodifiableList(thrusters);
    this.maxTotalThrust = maxTotalThrust;
    this.suspensions = Collections.unmodifiableList(suspensions);
    this.totalFuelCapacity = totalFuelCapacity;
    this.currentFuel = currentFuel;
    this.rocketStages = Collections.unmodifiableList(rocketStages);
    this.totalGyroTorque = totalGyroTorque;

    float thrust = Math.max(maxTotalThrust, 1f);
    float mass = Math.max(totalMass, 1f);
    this.estimatedMaxSpeed = Math.min(8f, thrust / mass * 0.5f);
    this.estimatedAngularAccel = Math.min(0.05f, totalGyroTorque / (momentOfInertia * 10f));
  }

  public boolean hasThrusters() { return !thrusters.isEmpty(); }
  public boolean hasSuspensions() { return !suspensions.isEmpty(); }
  public boolean hasRocketMotors() { return !rocketStages.isEmpty(); }
  public boolean hasGyroscopes() { return totalGyroTorque > 0f; }
  public boolean hasFuel() { return currentFuel > 0f; }

  public float estimatedDeltaV()
  {
    if (rocketStages.isEmpty() || totalFuelCapacity <= 0) return 0f;

    float avgIsp = rocketStages.stream().flatMap(s -> s.motors().stream()).map(RocketMotorMount::isp).reduce(0f, Float::sum) / Math.max(1, rocketStages.stream().mapToInt(s -> s.motors().size()).sum());
    float wetMass = totalMass;
    float dryMass = Math.max(1f, totalMass - totalFuelCapacity * 0.8f);
    return avgIsp * 9.8f * (float) Math.log(wetMass / dryMass);
  }

  public record ThrusterMount(
      Vec3 localPos,
      Vector3f direction,   // déjà normalisé
      float force,
      float fuelRate,
      int groupId
  ) {
    public Vec3 computeForceVector(float throttle)
    {
      float f = force * throttle;
      return new Vec3(direction.x * f, direction.y * f, direction.z * f);
    }

    public Vec3 torqueArm(Vec3 centerOfMass) {
      return localPos.subtract(centerOfMass);
    }
  }

  public record SuspensionMount(
      Vec3  localPos,
      float restHeight,
      float stiffness,
      float damping
  ) {}

  public record RocketMotorMount(
      Vec3  localPos,
      float thrustKN,
      float isp,
      float fuelRate
  ) {}

  public record RocketStage(
      int stageIndex,
      List<RocketMotorMount> motors,
      float totalFuelCapacity
  ) {
    public float totalThrust()
    {
      return motors.stream().map(RocketMotorMount::thrustKN).reduce(0f, Float::sum) * 1000f;
    }

    public float totalFuelRate() {
      return motors.stream().map(RocketMotorMount::fuelRate).reduce(0f, Float::sum);
    }
  }

  public static VehicleCapabilities empty(float mass)
  {
    return new VehicleCapabilities(
        Math.max(1f, mass), Vec3.ZERO, Math.max(1f, mass),
        List.of(), 0f,
        List.of(),
        0f, 0f,
        List.of(),
        0f
    );
  }
}

