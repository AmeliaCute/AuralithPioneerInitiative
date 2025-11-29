package cute.ame.auralithpioneerinitiative.Space.Body.Component.Impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import cute.ame.auralithpioneerinitiative.Space.Body.CelestialBodyBase;
import cute.ame.auralithpioneerinitiative.Space.Body.Component.CelestialComponent;
import cute.ame.auralithpioneerinitiative.Space.Rendering.Geometry.AuralithMesh;
import cute.ame.auralithpioneerinitiative.Space.Rendering.Geometry.AuralithTriangle;
import cute.ame.auralithpioneerinitiative.Space.Rendering.Geometry.GeometryGenerator;
import cute.ame.auralithpioneerinitiative.Space.Rendering.Custom.PBR.PBRRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class AtmosphereComponent implements CelestialComponent
{

    protected Vector3f atmosphereColor;
    protected float atmosphereThickness;
    protected float atmosphereIntensity;
    protected int atmosphereLayers;

    protected AuralithMesh atmosphereMesh;
    protected int meshSubdivisions;

    protected float metallic;
    protected float roughness;
    protected float ambientOcclusion;

    public AtmosphereComponent(Vector3f atmosphereColor, float thickness, float intensity, int layers)
    {
        this.atmosphereColor = new Vector3f(atmosphereColor);
        this.atmosphereThickness = thickness;
        this.atmosphereIntensity = intensity;
        this.atmosphereLayers = Math.max(1, Math.min(32, layers));
        this.meshSubdivisions = 1;

        this.metallic = 0.0f;
        this.roughness = 0.5f;
        this.ambientOcclusion = 1.0f;
    }

    public AtmosphereComponent(Vector3f atmosphereColor, float thickness, float intensity)
    {
        this(atmosphereColor, thickness, intensity, 32);
    }

    @Override
    public void onAttach(CelestialBodyBase body)
    {
        generateMesh(body.getSize());
    }

    protected void generateMesh(float baseSize)
    {
        atmosphereMesh = GeometryGenerator.generateCube(baseSize, meshSubdivisions, true);
    }

    @Override
    public void render(CelestialBodyBase body, PoseStack poseStack, Camera camera, double relX, double relY, double relZ)
    {
        if (atmosphereMesh == null) {
            generateMesh(body.getSize());
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();

        for (int i = atmosphereLayers; i > 0; --i)
        {
            float t = (float)i / (atmosphereLayers + 1);
            float layerScale = (1.0f + (t * atmosphereThickness * 0.25f));
            float opacityFalloff = 1.0f - t;
            float layerOpacity = opacityFalloff * (atmosphereIntensity * 0.05f);

            renderAtmosphereLayer(body, poseStack, camera, layerScale, layerOpacity, t);
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    protected void renderAtmosphereLayer(CelestialBodyBase body, PoseStack poseStack, Camera camera, float scale, float opacity, float t)
    {
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        Vec3 cameraPos = camera.getPosition();
        Vec3 lightSourcePos = body.getLightSourcePos();

        Vector3f lightPos = lightSourcePos != null ?
                new Vector3f(
                        (float)(lightSourcePos.x - cameraPos.x),
                        (float)(lightSourcePos.y - cameraPos.y),
                        (float)(lightSourcePos.z - cameraPos.z)
                ) :
                new Vector3f(0, 300, 0);

        Vector3f lightColor = new Vector3f(1.0f, 0.95f, 0.9f);

        Vec3 bodyPos = body.getPos();
        Vector3f planetCenter = new Vector3f(
                (float)(bodyPos.x - cameraPos.x),
                (float)(bodyPos.y - cameraPos.y),
                (float)(bodyPos.z - cameraPos.z)
        );

        Vector3f albedoWithOpacity = new Vector3f(atmosphereColor).mul(Math.min(opacity * 3.0f, 1.0f));

        PBRRenderer.getInstance().renderMesh(
                atmosphereMesh, poseStack, camera,
                lightPos, lightColor,
                albedoWithOpacity, metallic, roughness, ambientOcclusion,
                planetCenter,
                new ArrayList<>(), new ArrayList<>()
        );

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    @Override
    public int getRenderPriority() { return 100; }

    public void setAtmosphereColor(Vector3f color) { this.atmosphereColor = new Vector3f(color); }

    public void setThickness(float thickness) { this.atmosphereThickness = Math.max(0.0f, thickness); }

    public void setIntensity(float intensity) { this.atmosphereIntensity = Math.max(0.0f, Math.min(10.0f, intensity)); }

    public void setLayers(int layers) { this.atmosphereLayers = Math.max(1, Math.min(32, layers)); }

    public void setMeshSubdivisions(int subdivisions)
    {
        this.meshSubdivisions = Math.max(1, subdivisions);

        if (atmosphereMesh != null)
            generateMesh(atmosphereMesh.triangles.get(0).v1().position().length());
    }

    public void setPBRParameters(float metallic, float roughness, float ambientOcclusion) {
        this.metallic = Math.max(0.0f, Math.min(1.0f, metallic));
        this.roughness = Math.max(0.0f, Math.min(1.0f, roughness));
        this.ambientOcclusion = Math.max(0.0f, Math.min(1.0f, ambientOcclusion));
    }

    public void setMetallic(float metallic) {
        this.metallic = Math.max(0.0f, Math.min(1.0f, metallic));
    }

    public void setRoughness(float roughness) {
        this.roughness = Math.max(0.0f, Math.min(1.0f, roughness));
    }

    public void setAmbientOcclusion(float ao) {
        this.ambientOcclusion = Math.max(0.0f, Math.min(1.0f, ao));
    }
}