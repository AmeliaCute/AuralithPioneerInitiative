package cute.ame.auralithpioneerinitiative.SkyPlanet.Rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.platform.GlStateManager;
import cute.ame.auralithpioneerinitiative.SkyPlanet.Data.AtmosphereDefinition;
import net.minecraft.client.renderer.*;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public final class AtmosphereRenderer
{
    private AtmosphereRenderer() {}

    private static final float[][] CORNER_NORMALS;
    static
    {
        float[] axes = {-1f, 1f};
        int idx = 0;
        CORNER_NORMALS = new float[8][3];
        for (float ax : axes) for (float ay : axes) for (float az : axes)
        {
            float len = (float) Math.sqrt(ax*ax + ay*ay + az*az);
            CORNER_NORMALS[idx][0] = ax / len;
            CORNER_NORMALS[idx][1] = ay / len;
            CORNER_NORMALS[idx][2] = az / len;
            idx++;
        }
    }

    private static final int[][] FACE_QUAD_CORNERS =
    {
        {1, 5, 7, 3},
        {0, 2, 6, 4},
        {2, 3, 7, 6},
        {0, 4, 5, 1},
        {4, 6, 7, 5},
        {0, 1, 3, 2},
    };

    private static final float[][] CORNER_POS = new float[8][3];
    static
    {
        for (int i = 0; i < 8; i++)
        {
            CORNER_POS[i][0] = ((i & 1) != 0) ?  0.5f : -0.5f;
            CORNER_POS[i][1] = ((i & 2) != 0) ?  0.5f : -0.5f;
            CORNER_POS[i][2] = ((i & 4) != 0) ?  0.5f : -0.5f;
        }
    }

    public static void render(PoseStack poseStack, AtmosphereDefinition atmo, float camDirX, float camDirY, float camDirZ)
    {
        float baseScale = 1.0f;
        float maxScale = atmo.scale();
        float r = atmo.r(), g = atmo.g(), b = atmo.b();
        float op = atmo.opacity() * 0.25f;
        int layers = 4;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        RenderSystem.enableCull();
        GL11.glCullFace(GL11.GL_FRONT);


        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);


        for (int i = 0; i < layers; i++)
        {

            float t = (float) i / (layers - 1);
            float scale = baseScale + t * (maxScale - baseScale);
            float alpha = (op * t) / layers;


            poseStack.pushPose();
            poseStack.scale(scale, scale, scale);
            Matrix4f m = poseStack.last().pose();


            for (int[] quad : FACE_QUAD_CORNERS)
            for (int ci : quad)
            {
                float[] pos = CORNER_POS[ci];
                buf.addVertex(m, pos[0], pos[1], pos[2]).setColor(r, g, b, alpha);
            }
            poseStack.popPose();
        }


        BufferUploader.drawWithShader(buf.buildOrThrow());
        GL11.glCullFace(GL11.GL_BACK);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

    }
}