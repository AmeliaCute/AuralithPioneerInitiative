package cute.ame.auralithpioneerinitiative.Space.CelestialBody;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class CelestialBodyBase
{
    private static final double DISTANCE_THRESHOLD = 350.0;

    protected final Vec3 pos;
    protected final float size;

    protected Vec3 lightSourcePos = null;

    public CelestialBodyBase(Vec3 pos, float size)
    {
        this.pos = pos;
        this.size = size;
    }

    public void render(PoseStack poseStack, Camera camera)
    {
        poseStack.pushPose();
        poseStack.setIdentity();

        Vec3 cameraPos = camera.getPosition();
        double relX = this.pos.x - cameraPos.x;
        double relY = this.pos.y - cameraPos.y;
        double relZ = this.pos.z - cameraPos.z;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        float size = processRelativePosAndSize(poseStack, camera, relX, relY, relZ);
        renderCube(size, poseStack, relX, relY, relZ);

        renderExtra(poseStack, camera, relX, relY, relZ);

        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    public void renderExtra(PoseStack poseStack, Camera camera, double relX, double relY, double relZ)
    {}


    public  void setLightSourcePos(Vec3 lightSourcePos)
    {
        this.lightSourcePos = lightSourcePos;
    }

    public boolean emitLight()
    {
        return false;
    }

    public Vec3 getPos()
    {
        return pos;
    }

    protected float processRelativePosAndSize(PoseStack poseStack, Camera camera, double relX, double relY, double relZ)
    {
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
        return effectiveSize;
    }

    protected void renderCube(float size, PoseStack poseStack, double relX, double relY, double relZ)
    {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

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

        for (int[] tri : indices)
            for (int idx : tri)
            {
                float[] v = vertices[idx];
                bufferBuilder.addVertex(matrix, v[0], v[1], v[2]).setColor(1, .5f, 0, 1);
            }

        BufferUploader.drawWithShader(bufferBuilder.build());
    }
}