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

    public CelestialBody(Vec3 pos, float size)
    {
        this.pos = pos;
        this.size = size;
    }

    public void render(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, PoseStack poseStack)
    {
        renderCustomCube(poseStack,camera,projectionMatrix);
    }

    protected void renderCustomCube(PoseStack poseStack, Camera camera, Matrix4f projectionMatrix) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Vec3 cameraPos = camera.getPosition();

        double relX = this.pos.x - cameraPos.x;
        double relY = this.pos.y - cameraPos.y;
        double relZ = this.pos.z - cameraPos.z;

        poseStack.pushPose();
        poseStack.translate(relX, relY, relZ);

        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        float r = 1.0F, g = 0.5F, b = 0.0F, a = 0.8F;

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

        for (int[] tri : indices)
            for (int idx : tri)
            {
                float[] v = vertices[idx];
                bufferBuilder.addVertex(matrix, v[0], v[1], v[2]).setColor(r, g, b, a);
            }


        BufferUploader.drawWithShader(bufferBuilder.build());
        poseStack.popPose();

        RenderSystem.disableBlend();
    }

}
