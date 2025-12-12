package cute.ame.auralithpioneerinitiative.Space.Body;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import cute.ame.auralithpioneerinitiative.Space.Rendering.Custom.BlackHole.BlackHoleRenderer;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class CelestialBlackHole extends CelestialBodyBase {

    protected Vector3f color;
    protected float speed;
    protected float intensity;
    protected float steps;
    protected float scale;
    protected float renderDistance;

    public CelestialBlackHole(Vec3 pos, float size)
    {
        this(pos, size, new Vector3f(-1, -1, -1), 0.5f, 200f, 20f, 1.0f, 100000f);
    }

    public CelestialBlackHole(Vec3 pos, float size, Vector3f color, float speed, float intensity, float steps, float scale, float renderDistance)
    {
        super(pos, size);
        this.color = new Vector3f(color);
        this.speed = speed;
        this.intensity = Math.max(0, Math.min(255, intensity));
        this.steps = Math.max(1, steps);
        this.scale = scale;
        this.renderDistance = renderDistance;
    }

    @Override
    protected void renderBody(PoseStack poseStack, Camera camera, double relX, double relY, double relZ)
    {
        poseStack.pushPose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        BlackHoleRenderer renderer = BlackHoleRenderer.getInstance();
        if (!renderer.isInitialized()) renderer.init();

        Vec3 cameraPos = camera.getPosition();
        renderer.render(poseStack, camera, pos, cameraPos, size, color, speed, intensity, steps, scale, renderDistance);

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    public Vector3f getColor()
    {
        return new Vector3f(color);
    }

    public void setColor(Vector3f color)
    {
        this.color.set(color);
    }

    public float getSpeed()
    {
        return speed;
    }

    public void setSpeed(float speed)
    {
        this.speed = speed;
    }

    public float getIntensity()
    {
        return intensity;
    }

    public void setIntensity(float intensity)
    {
        this.intensity = Math.max(0, Math.min(255, intensity));
    }

    public float getSteps()
    {
        return steps;
    }

    public void setSteps(float steps)
    {
        this.steps = Math.max(1, steps);
    }

    public float getScale()
    {
        return scale;
    }

    public void setScale(float scale)
    {
        this.scale = scale;
    }

    public float getRenderDistance()
    {
        return renderDistance;
    }

    public void setRenderDistance(float renderDistance)
    {
        this.renderDistance = renderDistance;
    }

    public void cleanup()
    {
    }
}