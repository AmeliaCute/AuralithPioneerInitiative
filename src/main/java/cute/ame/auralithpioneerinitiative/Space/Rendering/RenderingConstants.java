package cute.ame.auralithpioneerinitiative.Space.Rendering;

public final class RenderingConstants
{
    // === Dynamic Lighting Limits ===
    /** Maximum number of dynamic point lights per shader */
    public static final int MAX_POINT_LIGHTS = 16;

    /** Maximum number of dynamic spotlights per shader */
    public static final int MAX_SPOTLIGHTS = 16;

    /** Maximum number of aerial/volumetric lights per shader */
    public static final int MAX_AERIAL_LIGHTS = 16;

    /** Maximum number of shadow casting objects */
    public static final int MAX_SHADOW_CASTERS = 10;

    // === Default Light Parameters ===
    /** Default linear attenuation coefficient for point lights */
    public static final float DEFAULT_LINEAR_ATTENUATION = 0.09f;

    /** Default quadratic attenuation coefficient for point lights */
    public static final float DEFAULT_QUADRATIC_ATTENUATION = 0.032f;

    // === PBR Material Defaults ===
    /** Default ambient occlusion value (1.0 = no occlusion) */
    public static final float DEFAULT_AO = 1.0f;

    /** Default metallic value (0.0 = dielectric, 1.0 = metal) */
    public static final float DEFAULT_METALLIC = 0.0f;

    /** Default roughness value (0.0 = smooth, 1.0 = rough) */
    public static final float DEFAULT_ROUGHNESS = 0.5f;

    /** Minimum metallic value (clamped) */
    public static final float MIN_METALLIC = 0.0f;

    /** Maximum metallic value (clamped) */
    public static final float MAX_METALLIC = 1.0f;

    /** Minimum roughness value (clamped) */
    public static final float MIN_ROUGHNESS = 0.0f;

    /** Maximum roughness value (clamped) */
    public static final float MAX_ROUGHNESS = 1.0f;

    // === Geometry Generation ===
    /** Default subdivision level for sphere generation */
    @Deprecated(since = "WHY DO U NEED A SPHERE?")
    public static final int DEFAULT_SPHERE_SUBDIVISIONS = 3;

    /** Default subdivision level for cube generation */
    public static final int DEFAULT_CUBE_SUBDIVISIONS = 1;

    /** Minimum subdivision level (performance guard) */
    public static final int MIN_SUBDIVISIONS = 0;

    /** Maximum subdivision level (performance guard) */
    public static final int MAX_SUBDIVISIONS = 5;

    // === Component Priorities ===
    /** Default render priority for components */
    public static final int DEFAULT_COMPONENT_PRIORITY = 0;

    /** Priority for dynamic light components (render early) */
    public static final int LIGHT_COMPONENT_PRIORITY = 100;

    /** Priority for effect components (render late) */
    public static final int EFFECT_COMPONENT_PRIORITY = -100;

    // === Color Constants ===
    /** Default sun core color (warm white) */
    public static final float[] DEFAULT_SUN_CORE_COLOR = {1.0f, 0.85f, 0.6f};

    /** Default sun glow color (orange) */
    public static final float[] DEFAULT_SUN_GLOW_COLOR = {1.0f, 0.6f, 0.3f};

    /** Default sun light color (slightly warm) */
    public static final float[] DEFAULT_SUN_LIGHT_COLOR = {1.0f, 0.95f, 0.9f};

    /** Pure white color */
    public static final float[] WHITE = {1.0f, 1.0f, 1.0f};

    static
    {
        assert MAX_POINT_LIGHTS > 0 : "MAX_POINT_LIGHTS must be positive";
        assert MAX_SPOTLIGHTS > 0 : "MAX_SPOTLIGHTS must be positive";
        assert MAX_SHADOW_CASTERS > 0 : "MAX_SHADOW_CASTERS must be positive";
        assert DEFAULT_LINEAR_ATTENUATION >= 0 : "Attenuation cannot be negative";
        assert DEFAULT_QUADRATIC_ATTENUATION >= 0 : "Attenuation cannot be negative";
        assert MIN_SUBDIVISIONS <= MAX_SUBDIVISIONS : "Invalid subdivision range";
    }
}