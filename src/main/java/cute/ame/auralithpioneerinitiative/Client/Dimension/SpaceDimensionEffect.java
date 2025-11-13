package cute.ame.auralithpioneerinitiative.Client.Dimension;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import cute.ame.auralithpioneerinitiative.Space.CelestialBody.CelestialBody;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class SpaceDimensionEffect extends DimensionSpecialEffects
{
    public static final CelestialBody TEST_CUBE = new CelestialBody(Vec3.ZERO, 80);

    public SpaceDimensionEffect()
    {
        super(
                Float.NaN,
                false,
                SkyType.NONE,
                false,
                false
        );
    }

    @Override
    public float getCloudHeight() {
        return Float.NaN;
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 vec3, float v) {
        return new Vec3(0,0,0);
    }

    @Override
    public boolean isFoggyAt(int i, int i1) {
        return false;
    }


    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        return false;
    }


}
