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
    protected static final Vector3f atmosphereColor = new Vector3f(.1f, .1f, .5f);
    protected static final float atmosphereOpacity = 0.7f;
    protected static final float fresnelPower = 1.0f;
    protected static final float fresnelIntensity = 1.25f;

    public CelestialPlanet(Vec3 pos, float size) {
        super(pos, size);
    }

    @Override
    public void render( PoseStack poseStack, Camera camera)
    {
        poseStack.pushPose();
        poseStack.setIdentity();

        Vec3 cameraPos = camera.getPosition();
        double relX = this.pos.x - cameraPos.x;
        double relY = this.pos.y - cameraPos.y;
        double relZ = this.pos.z - cameraPos.z;

        float size = processRelativePosAndSize(poseStack, camera, relX, relY, relZ);
        renderPlanetCube(size, poseStack, relX, relY, relZ);

        renderExtra(poseStack, camera, relX, relY, relZ);

        poseStack.popPose();
    }

    protected void renderPlanetCube(float size, PoseStack poseStack, double relX, double relY, double relZ)
    {
        ShaderInstance shader = ShaderRegistries.FRESNEL_ATMOSPHERE_SHADER;
        if (shader == null) return;

        RenderSystem.setShader(() -> shader);

        shader.safeGetUniform("uAtmosphereColor").set(atmosphereColor.x, atmosphereColor.y, atmosphereColor.z);
        shader.safeGetUniform("uDirection").set(0f, 0f, 0f);
        shader.safeGetUniform("uOpacity").set(atmosphereOpacity);
        shader.safeGetUniform("uFresnelPower").set(fresnelPower);
        shader.safeGetUniform("uFresnelIntensity").set(fresnelIntensity);
        shader.safeGetUniform("ModelViewMat").set(poseStack.last().pose());
        shader.safeGetUniform("ProjMat").set(RenderSystem.getProjectionMatrix());

        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        float[][] vertices = {
                {-size, -size, -size},
                { size, -size, -size},
                { size,  size, -size},
                {-size,  size, -size},
                {-size, -size,  size},
                { size, -size,  size},
                { size,  size,  size},
                {-size,  size,  size}
        };

        int[][] indices = {
                {0, 1, 2}, {0, 2, 3},
                {4, 7, 6}, {4, 6, 5},
                {3, 2, 6}, {3, 6, 7},
                {0, 4, 5}, {0, 5, 1},
                {1, 5, 6}, {1, 6, 2},
                {0, 3, 7}, {0, 7, 4}
        };

        for (int[] tri : indices) {
            float[] v0 = vertices[tri[0]];
            float[] v1 = vertices[tri[1]];
            float[] v2 = vertices[tri[2]];

            float ux = v1[0] - v0[0];
            float uy = v1[1] - v0[1];
            float uz = v1[2] - v0[2];

            float vx = v2[0] - v0[0];
            float vy = v2[1] - v0[1];
            float vz = v2[2] - v0[2];

            float nx = uy * vz - uz * vy;
            float ny = uz * vx - ux * vz;
            float nz = ux * vy - uy * vx;

            float len = (float)Math.sqrt(nx*nx + ny*ny + nz*nz);
            if (len == 0) len = 1f;
            nx /= len; ny /= len; nz /= len;

            bufferBuilder.addVertex(matrix, v0[0], v0[1], v0[2]).setColor(1f, .5f, 0f, 1f).setNormal(nx, ny, nz);
            bufferBuilder.addVertex(matrix, v1[0], v1[1], v1[2]).setColor(1f, .5f, 0f, 1f).setNormal(nx, ny, nz);
            bufferBuilder.addVertex(matrix, v2[0], v2[1], v2[2]).setColor(1f, .5f, 0f, 1f).setNormal(nx, ny, nz);
        }

        BufferUploader.drawWithShader(bufferBuilder.build());
    }
}