package cute.ame.auralithpioneerinitiative.Space.CelestialBody;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class CelestialBody
{
    private final Vec3 pos;
    private final float size;
    private static final double DISTANCE_THRESHOLD = 350.0;

    public CelestialBody(Vec3 pos, float size)
    {
        this.pos = pos;
        this.size = size;
    }

    public void render(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, PoseStack poseStack)
    {
        renderCustomCube(poseStack, camera);
    }

    public void renderCustomCube(PoseStack poseStack, Camera camera) {
        poseStack.pushPose();
        poseStack.setIdentity();


        Vec3 cameraPos = camera.getPosition();

        double relX = this.pos.x - cameraPos.x;
        double relY = this.pos.y - cameraPos.y;
        double relZ = this.pos.z - cameraPos.z;

        double distance = Math.sqrt(relX * relX + relY * relY + relZ * relZ);

        float effectiveSize;
        double renderX, renderY, renderZ;

        if (distance > DISTANCE_THRESHOLD) {
            double normalizedX = relX / distance;
            double normalizedY = relY / distance;
            double normalizedZ = relZ / distance;

            renderX = normalizedX * DISTANCE_THRESHOLD;
            renderY = normalizedY * DISTANCE_THRESHOLD;
            renderZ = normalizedZ * DISTANCE_THRESHOLD;

            effectiveSize = (float) (this.size * DISTANCE_THRESHOLD / distance);
        } else {
            renderX = relX;
            renderY = relY;
            renderZ = relZ;
            effectiveSize = this.size;
        }

        poseStack.translate(renderX, renderY, renderZ);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(770, 1);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        float[][] vertices = {
                {-effectiveSize, -effectiveSize, -effectiveSize},
                { effectiveSize, -effectiveSize, -effectiveSize},
                { effectiveSize,  effectiveSize, -effectiveSize},
                {-effectiveSize,  effectiveSize, -effectiveSize},
                {-effectiveSize, -effectiveSize,  effectiveSize},
                { effectiveSize, -effectiveSize,  effectiveSize},
                { effectiveSize,  effectiveSize,  effectiveSize},
                {-effectiveSize,  effectiveSize,  effectiveSize}
        };

        int[][] indices = {
                {0, 1, 2}, {0, 2, 3},
                {4, 7, 6}, {4, 6, 5},
                {3, 2, 6}, {3, 6, 7},
                {0, 4, 5}, {0, 5, 1},
                {1, 5, 6}, {1, 6, 2},
                {0, 3, 7}, {0, 7, 4}
        };

        for (int[] tri : indices)
            for (int idx : tri)
            {
                float[] v = vertices[idx];
                bufferBuilder.addVertex(matrix, v[0], v[1], v[2]).setColor(1, .5f, 0, 1);
            }

        BufferUploader.drawWithShader(bufferBuilder.build());

        poseStack.popPose();
    }
}