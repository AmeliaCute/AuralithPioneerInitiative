package cute.ame.auralithpioneerinitiative.Ship.Renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class ShipEntityRenderer extends EntityRenderer<ShipEntity>
{
    public ShipEntityRenderer(EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ShipEntity entity)
    {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public boolean shouldRender(ShipEntity entity, Frustum frustum, double camX, double camY, double camZ)
    {
        ShipClientCache.drainUploadQueue();

        BakedShipMesh mesh = ShipClientCache.getMesh(entity.getUUID());
        if (mesh == null || !mesh.isReady()) return false;

        AABB worldBounds = mesh.getLocalBounds().move(entity.position());
        return frustum.isVisible(worldBounds);
    }

    @Override
    public void render(ShipEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight)
    {
        BakedShipMesh mesh = ShipClientCache.getMesh(entity.getUUID());
        if (mesh == null || !mesh.isReady()) return;

        int lod = ShipLodManager.getLODLevel(entity, this.entityRenderDispatcher.camera);
        VertexBuffer vbo = mesh.getBuffer(lod);
        if (vbo == null) return;
        if (bufferSource instanceof MultiBufferSource.BufferSource bs) bs.endBatch();

        poseStack.pushPose();
        poseStack.mulPose(entity.getShipRotation());

        Matrix4f modelView = new Matrix4f(RenderSystem.getModelViewMatrix()).mul(poseStack.last().pose());
        Matrix4f projection = RenderSystem.getProjectionMatrix();

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        if (lod <= 1)
        {
            RenderSystem.setShader(GameRenderer::getRendertypeSolidShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            vbo.bind();
            vbo.drawWithShader(modelView, projection, RenderSystem.getShader());
            VertexBuffer.unbind();
        }
        else
        {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            vbo.bind();
            vbo.drawWithShader(modelView, projection, RenderSystem.getShader());
            VertexBuffer.unbind();
            RenderSystem.disableBlend();
        }

        poseStack.popPose();
    }
}