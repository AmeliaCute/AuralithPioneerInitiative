package cute.ame.auralithpioneerinitiative.Space.Body;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import cute.ame.auralithpioneerinitiative.Space.Rendering.Geometry.AuralithMesh;
import cute.ame.auralithpioneerinitiative.Space.Rendering.Geometry.GeometryGenerator;
import cute.ame.auralithpioneerinitiative.Space.Rendering.Custom.PBR.PBRRenderer;
import cute.ame.auralithpioneerinitiative.Space.Rendering.RenderingConstants;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CelestialPlanet extends CelestialBodyBase {

    protected Vector3f albedo;
    protected float metallic;
    protected float roughness;
    protected float ambientOcclusion;

    protected AuralithMesh mesh;
    protected int subdivisions;
    protected boolean meshDirty = true;

    protected List<CelestialBodyBase> shadowCasters = new ArrayList<>();

    private final List<Vector3f> cachedShadowPositions = new ArrayList<>();
    private final List<Float> cachedShadowRadii = new ArrayList<>();
    private final Vector3f tempVec = new Vector3f();

    public CelestialPlanet(Vec3 pos, float size, Vector3f albedo, float metallic, float roughness)
    {
        super(pos, size);
        this.albedo = new Vector3f(albedo);
        this.metallic = clamp(metallic, RenderingConstants.MIN_METALLIC, RenderingConstants.MAX_METALLIC);
        this.roughness = clamp(roughness, RenderingConstants.MIN_ROUGHNESS, RenderingConstants.MAX_ROUGHNESS);
        this.ambientOcclusion = RenderingConstants.DEFAULT_AO;
        this.subdivisions = RenderingConstants.DEFAULT_CUBE_SUBDIVISIONS;

        generateMesh();
    }

    protected void generateMesh()
    {
        if (mesh != null) mesh.cleanup();

        mesh = GeometryGenerator.generateCube(size, subdivisions, false);
        mesh.prepareGPUResources();
        meshDirty = false;
    }

    public void setShadowCasters(List<CelestialBodyBase> casters)
    {
        this.shadowCasters.clear();
        if (casters != null) for (CelestialBodyBase caster : casters) if (caster != this && !caster.emitLight()) this.shadowCasters.add(caster);
    }

    @Override
    protected void renderBody(PoseStack poseStack, Camera camera, double relX, double relY, double relZ)
    {
        if (meshDirty) generateMesh();

        if (mesh == null || !mesh.isReadyToRender())
        {
            System.err.println("[CelestialPlanet] Mesh not ready for rendering");
            return;
        }

        poseStack.pushPose();

        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Vec3 cameraPos = camera.getPosition();

        Vector3f lightPos = calculateLightPosition(cameraPos);
        Vector3f lightColor = new Vector3f(RenderingConstants.DEFAULT_SUN_LIGHT_COLOR[0], RenderingConstants.DEFAULT_SUN_LIGHT_COLOR[1], RenderingConstants.DEFAULT_SUN_LIGHT_COLOR[2]);
        Vector3f planetCenter = new Vector3f((float)(pos.x - cameraPos.x), (float)(pos.y - cameraPos.y), (float)(pos.z - cameraPos.z));

        updateShadowCasterCache(cameraPos);
        PBRRenderer.getInstance().renderMesh(mesh, poseStack, camera,lightPos, lightColor,albedo, metallic, roughness, ambientOcclusion, planetCenter, cachedShadowPositions, cachedShadowRadii);

        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    private Vector3f calculateLightPosition(Vec3 cameraPos) {
        if (lightSourcePos != null) return tempVec.set((float)(lightSourcePos.x - cameraPos.x),(float)(lightSourcePos.y - cameraPos.y),(float)(lightSourcePos.z - cameraPos.z));
        return tempVec.set(0, 300, 0);
    }

    private void updateShadowCasterCache(Vec3 cameraPos)
    {
        cachedShadowPositions.clear();
        cachedShadowRadii.clear();

        for (CelestialBodyBase caster : shadowCasters)
        {
            Vec3 casterPos = caster.getPos();
            cachedShadowPositions.add(new Vector3f((float)(casterPos.x - cameraPos.x),(float)(casterPos.y - cameraPos.y),(float)(casterPos.z - cameraPos.z)));
            cachedShadowRadii.add(caster.getSize());
        }
    }

    public void setAlbedo(Vector3f color)
    {
        this.albedo.set(color);
    }

    public void setMetallic(float metallic)
    {
        this.metallic = clamp(metallic, RenderingConstants.MIN_METALLIC, RenderingConstants.MAX_METALLIC);
    }

    public void setRoughness(float roughness)
    {
        this.roughness = clamp(roughness, RenderingConstants.MIN_ROUGHNESS, RenderingConstants.MAX_ROUGHNESS);
    }

    public void setAO(float ao)
    {
        this.ambientOcclusion = clamp(ao, 0.0f, 1.0f);
    }

    public void setSubdivisions(int subdivisions)
    {
        int clamped = clamp(subdivisions,RenderingConstants.MIN_SUBDIVISIONS,RenderingConstants.MAX_SUBDIVISIONS);

        if (this.subdivisions != clamped)
        {
            this.subdivisions = clamped;
            this.meshDirty = true;
        }
    }

    public Vector3f getAlbedo()
    {
        return new Vector3f(albedo);
    }

    public float getMetallic()
    {
        return metallic;
    }

    public float getRoughness()
    {
        return roughness;
    }

    public float getAO()
    {
        return ambientOcclusion;
    }

    public int getSubdivisions()
    {
        return subdivisions;
    }

    public List<CelestialBodyBase> getShadowCasters()
    {
        return Collections.unmodifiableList(shadowCasters);
    }

    public void cleanup()
    {
        if (mesh != null)
        {
            mesh.cleanup();
            mesh = null;
        }
    }

    private static float clamp(float value, float min, float max)
    {
        return Math.max(min, Math.min(max, value));
    }

    private static int clamp(int value, int min, int max)
    {
        return Math.max(min, Math.min(max, value));
    }
}