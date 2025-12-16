package cute.ame.auralithpioneerinitiative.Space;

import com.mojang.blaze3d.vertex.PoseStack;
import cute.ame.auralithpioneerinitiative.Space.Body.CelestialBlackHole;
import cute.ame.auralithpioneerinitiative.Space.Body.CelestialBodyBase;
import cute.ame.auralithpioneerinitiative.Space.Body.CelestialPlanet;
import cute.ame.auralithpioneerinitiative.Space.Body.CelestialSun;
import cute.ame.auralithpioneerinitiative.Space.Body.Component.OrbitComponent;
import net.minecraft.client.Camera;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CelestialSystem
{
    private final List<CelestialBodyBase> objets = new ArrayList<>();
    private CelestialSun primarySun = null;
    private CelestialPlanet mainPlanet = null;
    private CelestialPlanet overworldPlanet = null;

    public CelestialSystem()
    {
        primarySun = new CelestialSun(
                new Vec3(0, 0, 0),
                1000,
                new Vector3f(1.0f, 0.85f, 0.6f),
                new Vector3f(1.0f, 0.6f, 0.3f)
        );
        primarySun.setGlowIntensity(2f);
        primarySun.setGlowLayers(32);
        primarySun.setPulse(0.3f, 0.08f);
        objets.add(primarySun);

        CelestialPlanet planet = new CelestialPlanet(
                new Vec3(8000, -1600, 0), // Starting position
                400,
                new Vector3f(0.3f, 0.5f, 0.8f),
                0.0f,
                0.5f
        );
        planet.addComponent(new OrbitComponent(primarySun, 8000, 1.0, 0.0));
        objets.add(planet);
        mainPlanet = planet;
        overworldPlanet = planet;
        DimensionPlanetRegistry.bindDimensionToPlanet(Level.OVERWORLD, planet);

        CelestialPlanet planetShadowTest = new CelestialPlanet(
                new Vec3(20000, 0, -7000),
                500,
                new Vector3f(0.8f, 0.4f, 0.3f),
                0.2f,
                0.6f
        );
        planetShadowTest.addComponent(new OrbitComponent(
                primarySun, 27000, 1.0, Math.PI / 4, Math.toRadians(15), 0.1));
        objets.add(planetShadowTest);

        CelestialPlanet moon = new CelestialPlanet(
                new Vec3(9500, 0, 0),
                150,
                new Vector3f(0.6f, 0.6f, 0.6f),
                0.0f,
                0.8f
        );
        moon.addComponent(new OrbitComponent(planet, 1500, 1, 0.0));
        objets.add(moon);

        CelestialBlackHole blueBlackHole = new CelestialBlackHole(
                new Vec3(-12000, 0, 0),
                3,
                new Vector3f(100, 150, 255),
                0.3f,
                180f,
                25f,
                1.0f,
                100000f
        );
        objets.add(blueBlackHole);

        updateLightSources();
        updateShadowCasters();
    }

    public void updateLightSources()
    {
        if(primarySun == null) return;

        for(var body : objets) body.setLightSourcePos(primarySun.getPos());
    }

    public void updateShadowCasters()
    {
        for(var body : objets)
        if(body instanceof CelestialPlanet planet)
        {
            List<CelestialBodyBase> casters = new ArrayList<>();

            for(var otherBody : objets) if(otherBody != body) casters.add(otherBody);
            planet.setShadowCasters(casters);
        }
    }

    public void tick()
    {
        for(var body : objets)
            body.tick();

        updateLightSources();
    }

    /**
     * Render the system from a specific planet's perspective
     * @param viewPlanet The planet to view from (null = no offset)
     */
    public void render(PoseStack poseStack, Camera camera, CelestialPlanet viewPlanet)
    {
        Vec3 cameraPos = camera.getPosition();
        Vec3 viewOffset;

        if (viewPlanet != null)
        {
            viewOffset = viewPlanet.getPos().scale(-1);
        } else {
            viewOffset = Vec3.ZERO;
        }

        List<CelestialBodyBase> sortedBodies = new ArrayList<>(objets);
        sortedBodies.sort(Comparator.comparingDouble(body ->
        {
            Vec3 bodyPos = body.getPos().add(viewOffset);
            double dx = bodyPos.x - cameraPos.x;
            double dy = bodyPos.y - cameraPos.y;
            double dz = bodyPos.z - cameraPos.z;
            return -(dx * dx + dy * dy + dz * dz);
        }));

        poseStack.pushPose();

        if (viewPlanet != null)
        {
            poseStack.translate(viewOffset.x, viewOffset.y, viewOffset.z);
        }

        for(var body : sortedBodies)
            if (body != viewPlanet)
                body.render(poseStack, camera);

        poseStack.popPose();
    }

    /**
     * Render from space dimension (no planet perspective)
     */
    public void render(PoseStack poseStack, Camera camera)
    {
        render(poseStack, camera, null);
    }

    public CelestialPlanet getMainPlanet()
    {
        return mainPlanet;
    }

    public CelestialPlanet getOverworldPlanet()
    {
        return overworldPlanet;
    }

    public CelestialSun getPrimarySun()
    {
        return primarySun;
    }

    public List<CelestialBodyBase> getAllBodies()
    {
        return new ArrayList<>(objets);
    }

    public void addBody(CelestialBodyBase body)
    {
        if (!objets.contains(body))
        {
            objets.add(body);
            updateLightSources();
            updateShadowCasters();
        }
    }

    public void removeBody(CelestialBodyBase body)
    {
        objets.remove(body);
        updateShadowCasters();
    }
}