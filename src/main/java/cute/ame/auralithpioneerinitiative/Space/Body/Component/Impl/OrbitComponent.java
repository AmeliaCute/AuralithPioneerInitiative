package cute.ame.auralithpioneerinitiative.Space.Body.Component;

import cute.ame.auralithpioneerinitiative.Space.Body.CelestialBodyBase;
import net.minecraft.world.phys.Vec3;

/**
 * Component that makes a celestial body orbit around another body
 */
public class OrbitComponent implements CelestialComponent
{
    private CelestialBodyBase orbitCenter;
    private double orbitalRadius;
    private double orbitalSpeed; // radians per tick
    private double currentAngle;
    private double inclination; // orbital plane tilt in radians
    private double eccentricity; // 0 = circle, <1 = ellipse
    private Vec3 orbitOffset; // offset from orbit center

    /**
     * @param orbitCenter The body to orbit around
     * @param orbitalRadius Distance from orbit center (semi-major axis)
     * @param minecraftDaysPerOrbit How many Minecraft days for one full orbit (24000 ticks = 1 day)
     * @param startAngle Starting angle in radians
     * @param inclination Orbital plane tilt in radians (0 = flat)
     * @param eccentricity Orbit shape (0 = circle, 0-1 = ellipse)
     */
    public OrbitComponent(CelestialBodyBase orbitCenter, double orbitalRadius, double minecraftDaysPerOrbit, double startAngle, double inclination, double eccentricity)
    {
        this.orbitCenter = orbitCenter;
        this.orbitalRadius = orbitalRadius;
        this.currentAngle = startAngle;
        this.inclination = inclination;
        this.eccentricity = Math.max(0.0, Math.min(0.99, eccentricity));
        this.orbitOffset = Vec3.ZERO;

        double ticksPerOrbit = minecraftDaysPerOrbit * 24000.0;
        this.orbitalSpeed = (2.0 * Math.PI) / ticksPerOrbit;
    }

    /**
     * Simplified constructor for circular orbit
     */
    public OrbitComponent(CelestialBodyBase orbitCenter, double orbitalRadius, double minecraftDaysPerOrbit, double startAngle)
    {
        this(orbitCenter, orbitalRadius, minecraftDaysPerOrbit, startAngle, 0.0, 0.0);
    }

    @Override
    public void tick(CelestialBodyBase body)
    {
        if (orbitCenter == null) return;

        currentAngle += orbitalSpeed;
        if (currentAngle > 2.0 * Math.PI) currentAngle -= 2.0 * Math.PI;

        double r = orbitalRadius * (1.0 - eccentricity * eccentricity) / (1.0 + eccentricity * Math.cos(currentAngle));

        double x = r * Math.cos(currentAngle);
        double z = r * Math.sin(currentAngle);

        double y = z * Math.sin(inclination);
        z = z * Math.cos(inclination);

        Vec3 centerPos = orbitCenter.getPos();
        Vec3 newPos = centerPos.add(x + orbitOffset.x, y + orbitOffset.y, z + orbitOffset.z);

        updateBodyPosition(body, newPos);
    }

    /**
     * Use reflection to update the final pos field
     */
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
            System.err.println("Failed to update orbital position: " + e.getMessage());
        }
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

    public void setOrbitCenter(CelestialBodyBase center)
    {
        this.orbitCenter = center;
    }

    public void setOrbitalRadius(double radius)
    {
        this.orbitalRadius = radius;
    }

    public void setOrbitalSpeed(double minecraftDaysPerOrbit)
    {
        double ticksPerOrbit = minecraftDaysPerOrbit * 24000.0;
        this.orbitalSpeed = (2.0 * Math.PI) / ticksPerOrbit;
    }

    public void setInclination(double radians)
    {
        this.inclination = radians;
    }

    public void setEccentricity(double eccentricity)
    {
        this.eccentricity = Math.max(0.0, Math.min(0.99, eccentricity));
    }

    public void setOrbitOffset(Vec3 offset)
    {
        this.orbitOffset = offset;
    }

    public double getCurrentAngle()
    {
        return currentAngle;
    }

    public double getOrbitalRadius()
    {
        return orbitalRadius;
    }

    public CelestialBodyBase getOrbitCenter()
    {
        return orbitCenter;
    }
}