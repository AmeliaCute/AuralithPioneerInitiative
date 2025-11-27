package cute.ame.auralithpioneerinitiative.Space;

import com.mojang.blaze3d.vertex.PoseStack;
import cute.ame.auralithpioneerinitiative.Registries.PlanetBiomeRegistry;
import cute.ame.auralithpioneerinitiative.Space.Body.CelestialSun;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class CelestialSystem
{
    private CelestialSun primarySun = null;

    public CelestialSystem()
    {
        initializeCosmicHorizonsStyle();
    }

    private void initializeCosmicHorizonsStyle()
    {
        // Main sun - warm orange glow
        primarySun = new CelestialSun(
                new Vec3(0, 300, 0),
                40,
                new Vector3f(1.0f, 0.85f, 0.6f),
                new Vector3f(1.0f, 0.6f, 0.3f)
        );
        primarySun.setGlowIntensity(1.2f);
        primarySun.setGlowLayers(8);
        primarySun.setPulse(0.3f, 0.08f);

        updateLightSources();
    }

    public void updateLightSources()
    {
        if(primarySun == null) return;

    }

    public void render(PoseStack poseStack, Camera camera)
    {
        // Render sun first (no depth test)
        if(primarySun != null) {
            primarySun.render(poseStack, camera);
        }

    }
}