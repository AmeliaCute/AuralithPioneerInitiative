package cute.ame.auralithpioneerinitiative.Space.Body;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import cute.ame.auralithpioneerinitiative.Registries.PlanetBiomeRegistry;
import cute.ame.auralithpioneerinitiative.Registries.ShaderRegistry;
import cute.ame.auralithpioneerinitiative.Space.Biome.PlanetBiome;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class CelestialPlanet extends CelestialBodyBase
{
    private static final float[][] CUBE_VERTICES = {
            {-1f, -1f, -1f}, { 1f, -1f, -1f}, { 1f,  1f, -1f}, {-1f,  1f, -1f},
            {-1f, -1f,  1f}, { 1f, -1f,  1f}, { 1f,  1f,  1f}, {-1f,  1f,  1f}
    };
    private static final float[][] VERTEX_NORMALS = {
            {-0.577f, -0.577f, -0.577f}, { 0.577f, -0.577f, -0.577f},
            { 0.577f,  0.577f, -0.577f}, {-0.577f,  0.577f, -0.577f},
            {-0.577f, -0.577f,  0.577f}, { 0.577f, -0.577f,  0.577f},
            { 0.577f,  0.577f,  0.577f}, {-0.577f,  0.577f,  0.577f}
    };
    private static final int[] CUBE_INDICES = {
            0, 3, 2,  0, 2, 1,  // Front
            4, 5, 6,  4, 6, 7,  // Back
            3, 7, 6,  3, 6, 2,  // Top
            0, 1, 5,  0, 5, 4,  // Bottom
            1, 2, 6,  1, 6, 5,  // Right
            0, 4, 7,  0, 7, 3   // Left
    };
    protected static final int pixelSize = 2;

    protected final PlanetBiome biomeType;
    protected float time = 0.0f;

    public CelestialPlanet(Vec3 pos, float size)
    {
        super(pos, size);
        biomeType = PlanetBiomeRegistry.BIOME_VOLCANIC.get();
    }

    public CelestialPlanet(Vec3 pos, float size, PlanetBiome biomeType)
    {
        super(pos, size);
        this.biomeType = biomeType;
    }


    @Override
    public void render(PoseStack poseStack, Camera camera)
    {
        time += 0.016f;

        poseStack.pushPose();
        poseStack.setIdentity();

        Vec3 cameraPos = camera.getPosition();
        double relX = this.pos.x - cameraPos.x;
        double relY = this.pos.y - cameraPos.y;
        double relZ = this.pos.z - cameraPos.z;

        float size = processRelativePosAndSize(poseStack, camera, relX, relY, relZ);

        renderPlanetSurface(size, poseStack, camera, relX, relY, relZ);

        if (biomeType.hasClouds()) {
            renderClouds(size * 1.025f, poseStack, camera, relX, relY, relZ);
        }

        if (biomeType.hasAtmosphere()) {
            renderAtmosphere(size * 1.05f, poseStack, camera, relX, relY, relZ);
        }

        renderExtra(poseStack, camera, relX, relY, relZ);

        poseStack.popPose();
    }

    protected void renderPlanetSurface(float size, PoseStack poseStack, Camera camera, double relX, double relY, double relZ)
    {
        ShaderInstance shader = ShaderRegistry.PLANET_SURFACE_SHADER;
        if (shader == null) {
            return;
        }

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.setShader(() -> shader);

        Vec3 cameraPos = camera.getPosition();
        shader.safeGetUniform("uCameraPos").set((float)cameraPos.x, (float)cameraPos.y, (float)cameraPos.z);
        shader.safeGetUniform("uPlanetPos").set((float)this.pos.x, (float)this.pos.y, (float)this.pos.z);
        shader.safeGetUniform("uPixelSize").set(pixelSize);
        shader.safeGetUniform("uBiomeType").set(biomeType.id());
        shader.safeGetUniform("uSeed").set(biomeType.seed());
        shader.safeGetUniform("uIceCoverage").set(biomeType.iceCoverage());
        shader.safeGetUniform("uLandRatio").set(biomeType.landRatio());

        renderCube(size, poseStack, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    protected void renderClouds(float size, PoseStack poseStack, Camera camera, double relX, double relY, double relZ)
    {
        ShaderInstance shader = ShaderRegistry.PLANET_CLOUDS_SHADER;
        if (shader == null) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(() -> shader);

        Vec3 cameraPos = camera.getPosition();
        shader.safeGetUniform("uCameraPos").set((float)cameraPos.x, (float)cameraPos.y, (float)cameraPos.z);
        shader.safeGetUniform("uPlanetPos").set((float)this.pos.x, (float)this.pos.y, (float)this.pos.z);
        shader.safeGetUniform("uTime").set(time);
        shader.safeGetUniform("uPixelSize").set(pixelSize);
        shader.safeGetUniform("uBiomeType").set(biomeType.id());
        shader.safeGetUniform("uSeed").set(biomeType.seed());
        shader.safeGetUniform("uCloudCoverage").set(biomeType.cloudCoverage());
        shader.safeGetUniform("uCloudLayers").set(biomeType.cloudLayers());
        shader.safeGetUniform("uCloudSpeed").set(biomeType.cloudSpeed());
        shader.safeGetUniform("uCloudColor").set(biomeType.cloudColor()[0], biomeType.cloudColor()[1], biomeType.cloudColor()[2], biomeType.cloudColor()[3]);

        renderCube(size, poseStack, 1.0f, 1.0f, 1.0f, 1.0f);

        RenderSystem.depthMask(true);
    }

    protected void renderAtmosphere(float size, PoseStack poseStack, Camera camera, double relX, double relY, double relZ)
    {
        ShaderInstance shader = ShaderRegistry.FRESNEL_ATMOSPHERE_SHADER;
        if (shader == null) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(() -> shader);

        float dist = (float)Math.sqrt(relX*relX + relY*relY + relZ*relZ);
        if (dist > 0.001f) {
            float invDist = 1.0f / dist;
            shader.safeGetUniform("uViewDirection").set(
                    (float)relX * invDist,
                    (float)relY * invDist,
                    (float)relZ * invDist
            );
        }

        shader.safeGetUniform("uFresnelPower").set(biomeType.fresnelPower());
        shader.safeGetUniform("uAtmosphereIntensity").set(biomeType.atmosphereIntensity());

        Vec3 cameraPos = camera.getPosition();
        shader.safeGetUniform("uCameraPos").set((float)cameraPos.x, (float)cameraPos.y, (float)cameraPos.z);
        shader.safeGetUniform("uPlanetPos").set((float)this.pos.x, (float)this.pos.y, (float)this.pos.z);
        shader.safeGetUniform("uAtmosphereColor").set(
                biomeType.atmosphereColor()[0], biomeType.atmosphereColor()[1], biomeType.atmosphereColor()[2], biomeType.atmosphereColor()[3]
        );

        renderCube(size, poseStack, 0.3f, 0.6f, 1.0f, 0.5f);

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private void renderCube(float size, PoseStack poseStack, float r, float g, float b, float a)
    {
        Matrix4f matrix = poseStack.last().pose();
        PoseStack.Pose normalMatrix = poseStack.last();

        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(
                VertexFormat.Mode.TRIANGLES,
                DefaultVertexFormat.POSITION_COLOR_NORMAL
        );

        for (int vertexIndex : CUBE_INDICES)
        {
            float[] vertex = CUBE_VERTICES[vertexIndex];
            float[] normal = VERTEX_NORMALS[vertexIndex];

            float vx = vertex[0] * size;
            float vy = vertex[1] * size;
            float vz = vertex[2] * size;

            bufferBuilder.addVertex(matrix, vx, vy, vz)
                    .setColor(r, g, b, a)
                    .setNormal(normalMatrix, normal[0], normal[1], normal[2]);
        }

        BufferUploader.drawWithShader(bufferBuilder.build());
    }
}