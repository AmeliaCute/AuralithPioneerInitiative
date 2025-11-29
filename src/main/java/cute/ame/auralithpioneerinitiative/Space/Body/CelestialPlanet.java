package cute.ame.auralithpioneerinitiative.Space.Body;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import cute.ame.auralithpioneerinitiative.Space.Rendering.Geometry.AuralithMesh;
import cute.ame.auralithpioneerinitiative.Space.Rendering.Geometry.GeometryGenerator;
import cute.ame.auralithpioneerinitiative.Space.Rendering.Custom.PBR.PBRRenderer;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class CelestialPlanet extends CelestialBodyBase {

    protected Vector3f albedo;
    protected float metallic;
    protected float roughness;
    protected float ambientOcclusion;
    protected AuralithMesh mesh;
    protected int subdivisions;
    protected float rotationSpeed;
    protected float rotationOffset;

    protected List<CelestialBodyBase> shadowCasters = new ArrayList<>();

    public CelestialPlanet(Vec3 pos, float size, Vector3f albedo, float metallic, float roughness) {
        super(pos, size);
        this.albedo = new Vector3f(albedo);
        this.metallic = Math.max(0.0f, Math.min(1.0f, metallic));
        this.roughness = Math.max(0.0f, Math.min(1.0f, roughness));
        this.ambientOcclusion = 1.0f;
        this.subdivisions = 1;
        this.rotationSpeed = 0.05f;
        this.rotationOffset = 0.0f;
        generateMesh();
    }

    protected void generateMesh() {
        mesh = GeometryGenerator.generateCube(size, subdivisions, false);
    }

    public void setShadowCasters(List<CelestialBodyBase> casters) {
        this.shadowCasters = casters;
    }

    @Override
    protected void renderBody(PoseStack poseStack, Camera camera, double relX, double relY, double relZ) {
        poseStack.pushPose();

        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Vec3 cameraPos = camera.getPosition();

        Vector3f lightPos = lightSourcePos != null ?
                new Vector3f(
                        (float)(lightSourcePos.x - cameraPos.x),
                        (float)(lightSourcePos.y - cameraPos.y),
                        (float)(lightSourcePos.z - cameraPos.z)
                ) :
                new Vector3f(0, 300, 0);

        Vector3f lightColor = new Vector3f(1.0f, 0.95f, 0.9f);

        Vector3f planetCenter = new Vector3f(
                (float)(pos.x - cameraPos.x),
                (float)(pos.y - cameraPos.y),
                (float)(pos.z - cameraPos.z)
        );

        List<Vector3f> shadowCasterPositions = new ArrayList<>();
        List<Float> shadowCasterRadii = new ArrayList<>();

        for (CelestialBodyBase caster : shadowCasters) {
            if (caster == this || caster.emitLight()) continue;

            Vec3 casterPos = caster.getPos();
            Vector3f casterPosRelative = new Vector3f(
                    (float)(casterPos.x - cameraPos.x),
                    (float)(casterPos.y - cameraPos.y),
                    (float)(casterPos.z - cameraPos.z)
            );

            shadowCasterPositions.add(casterPosRelative);
            shadowCasterRadii.add(caster.size);
        }

        PBRRenderer.getInstance().renderMesh(
                mesh, poseStack, camera,
                lightPos, lightColor,
                albedo, metallic, roughness, ambientOcclusion,
                planetCenter,
                shadowCasterPositions, shadowCasterRadii
        );

        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    public void setAlbedo(Vector3f color) {
        this.albedo = new Vector3f(color);
    }

    public void setMetallic(float metallic) {
        this.metallic = Math.max(0.0f, Math.min(1.0f, metallic));
    }

    public void setRoughness(float roughness) {
        this.roughness = Math.max(0.0f, Math.min(1.0f, roughness));
    }

    public void setAO(float ao) {
        this.ambientOcclusion = Math.max(0.0f, Math.min(1.0f, ao));
    }

    public void setRotationSpeed(float speed) {
        this.rotationSpeed = speed;
    }
}