package cute.ame.auralithpioneerinitiative.Space.Body.Component.Impl;

import cute.ame.auralithpioneerinitiative.Space.Body.CelestialBodyBase;
import cute.ame.auralithpioneerinitiative.Space.Body.Component.CelestialComponent;
import cute.ame.auralithpioneerinitiative.Space.CelestialSystem;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Component that applies gravitational forces from other bodies
 * Lightweight simulation using simplified physics
 */
public class GravitationalComponent implements CelestialComponent
{
    private Vec3 velocity = Vec3.ZERO;
    private double mass;
    private CelestialSystem system;
    private List<CelestialBodyBase> ignoredBodies = new ArrayList<>();

    private static final double G = 0.00001;
    private static final double MAX_VELOCITY = 10.0;

    /**
     * @param mass The mass of this body (affects gravitational pull strength)
     * @param system The celestial system (to find other bodies)
     */
    public GravitationalComponent(double mass, CelestialSystem system)
    {
        this.mass = mass;
        this.system = system;
    }

    @Override
    public void tick(CelestialBodyBase body)
    {
        if (system == null) return;

        Vec3 acceleration = Vec3.ZERO;
        Vec3 bodyPos = body.getPos();

        for (CelestialBodyBase other : system.getAllBodies())
        {
            if (other == body || ignoredBodies.contains(other)) continue;

            Vec3 otherPos = other.getPos();
            Vec3 direction = otherPos.subtract(bodyPos);
            double distance = direction.length();

            if (distance < 100) continue;

            double otherMass = Math.pow(other.getSize(), 3); // Volume-based mass
            double forceMagnitude = G * otherMass / (distance * distance);

            Vec3 accel = direction.normalize().scale(forceMagnitude / mass);
            acceleration = acceleration.add(accel);
        }

        velocity = velocity.add(acceleration);

        double speed = velocity.length();
        if (speed > MAX_VELOCITY) velocity = velocity.normalize().scale(MAX_VELOCITY);

        Vec3 newPos = bodyPos.add(velocity);
        updateBodyPosition(body, newPos);
    }

    @Override
    public void render(CelestialBodyBase body, com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.Camera camera, double relX, double relY, double relZ)
    {
    }

    @Override
    public int getRenderPriority()
    {
        return -1000;
    }

    private void updateBodyPosition(CelestialBodyBase body, Vec3 newPos)
    {
        try
        {
            java.lang.reflect.Field posField = CelestialBodyBase.class.getDeclaredField("pos");
            posField.setAccessible(true);
            posField.set(body, newPos);
        }
        catch (Exception e)
        {
            System.err.println("Failed to update gravitational position: " + e.getMessage());
        }
    }

    /**
     * Set initial velocity for the body
     */
    public void setVelocity(Vec3 velocity)
    {
        this.velocity = velocity;
    }

    /**
     * Get current velocity
     */
    public Vec3 getVelocity()
    {
        return velocity;
    }

    /**
     * Add a body to ignore in gravity calculations (e.g., the body it's orbiting)
     */
    public void ignoreBody(CelestialBodyBase body)
    {
        if (!ignoredBodies.contains(body))
        {
            ignoredBodies.add(body);
        }
    }

    /**
     * Calculate and set orbital velocity for a circular orbit around a body
     */
    public void setCircularOrbitVelocity(CelestialBodyBase body, CelestialBodyBase center)
    {
        Vec3 bodyPos = body.getPos();
        Vec3 centerPos = center.getPos();
        Vec3 direction = bodyPos.subtract(centerPos);
        double distance = direction.length();

        if (distance < 1) return;

        double centerMass = Math.pow(center.getSize(), 3);
        double orbitalSpeed = Math.sqrt(G * centerMass / distance);

        Vec3 perpendicular = new Vec3(-direction.z, 0, direction.x).normalize();
        velocity = perpendicular.scale(orbitalSpeed);
    }
}