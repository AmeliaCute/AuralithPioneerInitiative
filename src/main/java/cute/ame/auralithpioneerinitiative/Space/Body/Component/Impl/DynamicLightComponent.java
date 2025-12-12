package cute.ame.auralithpioneerinitiative.Space.Body.Component.Impl;

import com.mojang.blaze3d.vertex.PoseStack;
import cute.ame.auralithpioneerinitiative.Space.Body.CelestialBodyBase;
import cute.ame.auralithpioneerinitiative.Space.Body.Component.CelestialComponent;
import cute.ame.auralithpioneerinitiative.Space.Rendering.Custom.PostProcess.LightingPostProcessor;
import net.minecraft.client.Camera;
import org.joml.Vector3f;


public class DynamicLightComponent implements CelestialComponent {

    public enum LightType {
        POINT,
        SPOT,
        AERIAL
    }

    private final LightType type;
    private final Vector3f color;
    private final float brightness;
    private final float range;

    private final float linear;
    private final float quadratic;

    private Vector3f direction;
    private final float innerConeAngle;
    private final float outerConeAngle;

    private final float volumetricIntensity;
    private boolean enabled = true;

    public static DynamicLightComponent createPointLight(Vector3f color, float brightness, float range)
    {
        return new DynamicLightComponent(LightType.POINT, color, brightness, range, 0.09f, 0.032f, null, 0, 0, 0);
    }

    public static DynamicLightComponent createSpotlight(Vector3f color, float brightness, float range, Vector3f direction, float innerAngle, float outerAngle)
    {
        return new DynamicLightComponent(LightType.SPOT, color, brightness, range, 0.09f, 0.032f, direction, innerAngle, outerAngle, 0);
    }

    public static DynamicLightComponent createAerialLight(Vector3f color, float brightness, float range, Vector3f direction, float innerAngle, float outerAngle, float volumetric)
    {
        return new DynamicLightComponent(LightType.AERIAL, color, brightness, range, 0.09f, 0.032f, direction, innerAngle, outerAngle, volumetric);
    }

    private DynamicLightComponent(LightType type, Vector3f color, float brightness, float range, float linear, float quadratic, Vector3f direction, float innerCone, float outerCone, float volumetric)
    {
        this.type = type;
        this.color = new Vector3f(color);
        this.brightness = brightness;
        this.range = range;
        this.linear = linear;
        this.quadratic = quadratic;
        this.direction = direction != null ? new Vector3f(direction).normalize() : new Vector3f(0, -1, 0);
        this.innerConeAngle = innerCone;
        this.outerConeAngle = outerCone;
        this.volumetricIntensity = volumetric;
    }

    @Override
    public void render(CelestialBodyBase body, PoseStack poseStack, Camera camera, double relX, double relY, double relZ)
    {
        if (!enabled) return;

        Vector3f worldPos = new Vector3f((float) body.getPos().x, (float) body.getPos().y, (float) body.getPos().z);
        LightingPostProcessor processor = LightingPostProcessor.getInstance();

        switch (type)
        {
            case POINT:
                processor.addPointLight(worldPos, color, brightness, range, linear, quadratic);
                break;

            case SPOT:
                processor.addSpotlight(worldPos, direction, color, brightness, range, innerConeAngle, outerConeAngle);
                break;

            case AERIAL:
                processor.addAerialLight(worldPos, direction, color, brightness, range, innerConeAngle, outerConeAngle, volumetricIntensity);
                break;
        }
    }

    @Override
    public int getRenderPriority() {
        return 100;
    }

    public void setDirection(Vector3f dir)
    {
        this.direction = new Vector3f(dir).normalize();
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public LightType getType()
    {
        return type;
    }

    public Vector3f getColor()
    {
        return new Vector3f(color);
    }

    public float getBrightness()
    {
        return brightness;
    }

    public float getRange()
    {
        return range;
    }
}