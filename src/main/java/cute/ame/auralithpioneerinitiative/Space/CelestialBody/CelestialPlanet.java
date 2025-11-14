package cute.ame.auralithpioneerinitiative.Space.CelestialBody;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import cute.ame.auralithpioneerinitiative.Registries.ShaderRegistries;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class CelestialPlanet extends CelestialBodyBase
{
    private static final float[][] CUBE_VERTICES = {
            {-1f, -1f, -1f},
            { 1f, -1f, -1f},
            { 1f,  1f, -1f},
            {-1f,  1f, -1f},
            {-1f, -1f,  1f},
            { 1f, -1f,  1f},
            { 1f,  1f,  1f},
            {-1f,  1f,  1f}
    };

    private static final float[][] VERTEX_NORMALS = {
            {-0.577f, -0.577f, -0.577f},
            { 0.577f, -0.577f, -0.577f},
            { 0.577f,  0.577f, -0.577f},
            {-0.577f,  0.577f, -0.577f},
            {-0.577f, -0.577f,  0.577f},
            { 0.577f, -0.577f,  0.577f},
            { 0.577f,  0.577f,  0.577f},
            {-0.577f,  0.577f,  0.577f}
    };

    private static final int[] CUBE_INDICES = {
            0, 3, 2,  0, 2, 1,  // Front
            4, 5, 6,  4, 6, 7,  // Back
            3, 7, 6,  3, 6, 2,  // Top
            0, 1, 5,  0, 5, 4,  // Bottom
            1, 2, 6,  1, 6, 5,  // Right
            0, 4, 7,  0, 7, 3   // Left
    };

    public CelestialPlanet(Vec3 pos, float size) {
        super(pos, size);
    }

    @Override
    public void render(PoseStack poseStack, Camera camera)
    {
        poseStack.pushPose();
        poseStack.setIdentity();

        Vec3 cameraPos = camera.getPosition();
        double relX = this.pos.x - cameraPos.x;
        double relY = this.pos.y - cameraPos.y;
        double relZ = this.pos.z - cameraPos.z;

        float size = processRelativePosAndSize(poseStack, camera, relX, relY, relZ);
        renderPlanetCube(size, poseStack, camera, relX, relY, relZ);

        renderExtra(poseStack, camera, relX, relY, relZ);

        poseStack.popPose();
    }

    protected void renderPlanetCube(float size, PoseStack poseStack, Camera camera, double relX, double relY, double relZ)
    {
        renderPlanetTexture(size, poseStack, camera, relX, relY, relZ);
        //renderAtmosphere(size * 1.05f, poseStack, camera, relX, relY, relZ);
    }

    protected void renderPlanetTexture(float size, PoseStack poseStack, Camera camera, double relX, double relY, double relZ)
    {
        ShaderInstance shader = ShaderRegistries.FRESNEL_ATMOSPHERE_SHADER;
        if (shader == null) return;

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.setShader(() -> shader);

        shader.safeGetUniform("uFresnelPower").set(0.0f);
        shader.safeGetUniform("uAtmosphereIntensity").set(0.0f);

        Vec3 cameraPos = camera.getPosition();
        shader.safeGetUniform("uCameraPos").set(
                (float)cameraPos.x,
                (float)cameraPos.y,
                (float)cameraPos.z
        );
        shader.safeGetUniform("uPlanetPos").set(
                (float)this.pos.x,
                (float)this.pos.y,
                (float)this.pos.z
        );

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

            float[] planetColor = getPlanetColor(vertex[0], vertex[1], vertex[2]);

            bufferBuilder.addVertex(matrix, vx, vy, vz)
                    .setColor(planetColor[0], planetColor[1], planetColor[2], 1f)
                    .setNormal(normalMatrix, normal[0], normal[1], normal[2]);
        }

        BufferUploader.drawWithShader(bufferBuilder.build());
    }

    protected void renderAtmosphere(float size, PoseStack poseStack, Camera camera, double relX, double relY, double relZ)
    {
        ShaderInstance shader = ShaderRegistries.FRESNEL_ATMOSPHERE_SHADER;
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
            if (shader.safeGetUniform("uViewDirection") != null) {
                shader.safeGetUniform("uViewDirection").set(
                        (float)relX * invDist,
                        (float)relY * invDist,
                        (float)relZ * invDist
                );
            }
        }

        shader.safeGetUniform("uFresnelPower").set(3.0f);
        shader.safeGetUniform("uAtmosphereIntensity").set(1.5f);

        Vec3 cameraPos = camera.getPosition();
        shader.safeGetUniform("uCameraPos").set(
                (float)cameraPos.x,
                (float)cameraPos.y,
                (float)cameraPos.z
        );

        shader.safeGetUniform("uPlanetPos").set(
                (float)this.pos.x,
                (float)this.pos.y,
                (float)this.pos.z
        );

        Matrix4f matrix = poseStack.last().pose();
        PoseStack.Pose normalMatrix = poseStack.last();

        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(
                VertexFormat.Mode.TRIANGLES,
                DefaultVertexFormat.POSITION_COLOR_NORMAL
        );

        for (int vertexIndex : CUBE_INDICES) {
            float[] vertex = CUBE_VERTICES[vertexIndex];
            float[] normal = VERTEX_NORMALS[vertexIndex];

            float vx = vertex[0] * size;
            float vy = vertex[1] * size;
            float vz = vertex[2] * size;

            bufferBuilder.addVertex(matrix, vx, vy, vz)
                    .setColor(0.3f, 0.6f, 1.0f, 0.5f)
                    .setNormal(normalMatrix, normal[0], normal[1], normal[2]);
        }

        BufferUploader.drawWithShader(bufferBuilder.build());
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    protected float[] getPlanetColor(float x, float y, float z) {
        float len = (float)Math.sqrt(x*x + y*y + z*z);
        float nx = x / len;
        float ny = y / len;
        float nz = z / len;

        float variation = (nx + ny + nz) * 0.5f + 0.5f;

        float r = 0.8f + variation * 0.2f;
        float g = 0.4f + variation * 0.1f;
        float b = 0.1f + variation * 0.05f;

        return new float[]{r, g, b};
    }
}