package cute.ame.auralithpioneerinitiative.Space.Body.Component.Impl;

import com.mojang.blaze3d.vertex.PoseStack;
import cute.ame.auralithpioneerinitiative.Space.Body.CelestialBodyBase;
import cute.ame.auralithpioneerinitiative.Space.Body.Component.CelestialComponent;
import cute.ame.auralithpioneerinitiative.Space.Rendering.Custom.PostProcess.LightingPostProcessor;
import cute.ame.auralithpioneerinitiative.Space.Rendering.RenderingConstants;
import net.minecraft.client.Camera;
import org.joml.Vector3f;

public class DynamicLightComponent implements CelestialComponent
{
    public enum LightType
    {
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

    /**
     * Creates an omnidirectional point light.
     *
     * @param color RGB color (0-1 range)
     * @param brightness Light intensity multiplier
     * @param range Maximum light distance
     */
    public static DynamicLightComponent createPointLight(Vector3f color, float brightness, float range)
    {
        return new DynamicLightComponent(LightType.POINT, color, brightness, range, RenderingConstants.DEFAULT_LINEAR_ATTENUATION, RenderingConstants.DEFAULT_QUADRATIC_ATTENUATION, null, 0, 0, 0);
    }

    /**
     * Creates a directional spotlight with cone.
     *
     * @param color RGB color (0-1 range)
     * @param brightness Light intensity multiplier
     * @param range Maximum light distance
     * @param direction Light direction (will be normalized)
     * @param innerAngle Inner cone angle in degrees (full brightness)
     * @param outerAngle Outer cone angle in degrees (fade to zero)
     */
    public static DynamicLightComponent createSpotlight(Vector3f color, float brightness, float range, Vector3f direction, float innerAngle, float outerAngle)
    {
        validateSpotlightAngles(innerAngle, outerAngle);
        return new DynamicLightComponent(LightType.SPOT, color , brightness, range, RenderingConstants.DEFAULT_LINEAR_ATTENUATION, RenderingConstants.DEFAULT_QUADRATIC_ATTENUATION, direction, innerAngle, outerAngle, 0);
    }

    /**
     * Creates an aerial/volumetric light with atmospheric scattering.
     *
     * @param color RGB color (0-1 range)
     * @param brightness Light intensity multiplier
     * @param range Maximum light distance
     * @param direction Light direction (will be normalized)
     * @param innerAngle Inner cone angle in degrees
     * @param outerAngle Outer cone angle in degrees
     * @param volumetric Volumetric scattering intensity (0-1)
     */
    public static DynamicLightComponent createAerialLight(Vector3f color, float brightness, float range, Vector3f direction, float innerAngle, float outerAngle, float volumetric)
    {
        validateSpotlightAngles(innerAngle, outerAngle);
        validateVolumetric(volumetric);
        return new DynamicLightComponent(LightType.AERIAL, color, brightness, range, RenderingConstants.DEFAULT_LINEAR_ATTENUATION, RenderingConstants.DEFAULT_QUADRATIC_ATTENUATION, direction, innerAngle, outerAngle, volumetric);
    }

    private DynamicLightComponent(LightType type, Vector3f color, float brightness, float range, float linear, float quadratic, Vector3f direction, float innerCone, float outerCone, float volumetric)
    {
        if (color == null) throw new IllegalArgumentException("Light color cannot be null");
        if (brightness < 0) throw new IllegalArgumentException("Brightness cannot be negative");
        if (range <= 0) throw new IllegalArgumentException("Range must be positive");

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
    public int getRenderPriority()
    {
        return RenderingConstants.LIGHT_COMPONENT_PRIORITY;
    }

    public void setDirection(Vector3f dir)
    {
        if (dir == null) throw new IllegalArgumentException("Direction cannot be null");
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

    public Vector3f getDirection()
    {
        return new Vector3f(direction);
    }

    public float getInnerConeAngle()
    {
        return innerConeAngle;
    }

    public float getOuterConeAngle()
    {
        return outerConeAngle;
    }

    public float getVolumetricIntensity()
    {
        return volumetricIntensity;
    }

    private static void validateSpotlightAngles(float innerAngle, float outerAngle)
    {
        if (innerAngle < 0 || innerAngle > 180) throw new IllegalArgumentException("Inner cone angle must be between 0 and 180 degrees");
        if (outerAngle < 0 || outerAngle > 180) throw new IllegalArgumentException("Outer cone angle must be between 0 and 180 degrees");
        if (innerAngle > outerAngle) throw new IllegalArgumentException("Inner cone angle must be less than or equal to outer cone angle");
    }

    private static void validateVolumetric(float volumetric)
    {
        if (volumetric < 0 || volumetric > 1) throw new IllegalArgumentException("Volumetric intensity must be between 0 and 1");
    }

    @Override
    public String toString()
    {
        return String.format("DynamicLightComponent{type=%s, color=%s, brightness=%.2f, range=%.1f, enabled=%s}", type, color, brightness, range, enabled);
    }
}