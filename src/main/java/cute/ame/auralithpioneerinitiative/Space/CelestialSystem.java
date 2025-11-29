package cute.ame.auralithpioneerinitiative.Space;

import com.mojang.blaze3d.vertex.PoseStack;
import cute.ame.auralithpioneerinitiative.Space.Body.CelestialBodyBase;
import cute.ame.auralithpioneerinitiative.Space.Body.CelestialPlanet;
import cute.ame.auralithpioneerinitiative.Space.Body.CelestialSun;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class CelestialSystem
{
    private final List<CelestialBodyBase> objets = new ArrayList<>();
    private CelestialSun primarySun = null;

    public CelestialSystem()
    {
        primarySun = new CelestialSun(
                new Vec3(0, 300, 0),
                40,
                new Vector3f(1.0f, 0.85f, 0.6f),
                new Vector3f(1.0f, 0.6f, 0.3f)
        );
        primarySun.setGlowIntensity(1.2f);
        primarySun.setGlowLayers(32);
        primarySun.setPulse(0.3f, 0.08f);

        CelestialPlanet planet = new CelestialPlanet(
                new Vec3(-200, 300, 200),
                40,
                new Vector3f(0.3f, 0.5f, 0.8f),
                0.0f,
                0.7f
        );
        objets.add(planet);

        CelestialPlanet planetShadowTest = new CelestialPlanet(
                new Vec3(-400, 300, 400),
                90,
                new Vector3f(0.3f, 0.5f, 0.8f),
                0.0f,
                0.7f
        );
        objets.add(planetShadowTest);

        updateLightSources();
        updateShadowCasters();
    }

    public void updateLightSources()
    {
        if(primarySun == null) return;

        for(var body : objets)
            body.setLightSourcePos(primarySun.getPos());
    }

    public void updateShadowCasters()
    {
        for(var body : objets)
        {
            if(body instanceof CelestialPlanet planet)
            {
                List<CelestialBodyBase> casters = new ArrayList<>();

                for(var otherBody : objets)
                {
                    if(otherBody != body)
                    {
                        casters.add(otherBody);
                    }
                }

                planet.setShadowCasters(casters);
            }
        }
    }

    public void render(PoseStack poseStack, Camera camera)
    {
        if(primarySun != null) {
            primarySun.render(poseStack, camera);
        }

        for(var body : objets)
            body.render(poseStack, camera);
    }
}