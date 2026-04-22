package cute.ame.auralithpioneerinitiative.vehicle.Client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import cute.ame.auralithpioneerinitiative.SkyPlanet.RenderingHelper.ShaderHelper;
import cute.ame.auralithpioneerinitiative.vehicle.Baking.BakedVehicleMesh;
import cute.ame.auralithpioneerinitiative.vehicle.Entity.BlockVehicleEntity;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public class VehicleRenderer<T extends BlockVehicleEntity> extends EntityRenderer<T>
{
  public VehicleRenderer(EntityRendererProvider.Context context) {
    super(context);
  }

  @Override
  public ResourceLocation getTextureLocation(T entity) {
    return TextureAtlas.LOCATION_BLOCKS;
  }

  @Override
  public boolean shouldRender(T entity, Frustum frustum, double camX, double camY, double camZ)
  {
    BakedVehicleMesh mesh = VehicleClientCache.getMesh(entity.getUUID());
    if (mesh == null || !mesh.isReady()) return false;

    return frustum.isVisible(mesh.getLocalBounds().move(entity.position()));
  }

  @Override
  public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight)
  {
    VehicleClientCache.registerEntity(entity);

    BakedVehicleMesh mesh = VehicleClientCache.getMesh(entity.getUUID());
    if (mesh == null || !mesh.isReady()) return;

    int lod = VehicleLodManager.getLODLevel(entity, this.entityRenderDispatcher.camera);
    if (lod >= 3) return;

    boolean shadersActive = ShaderHelper.shadersActive();
    VertexBuffer vbo = mesh.getBuffer(lod, shadersActive);
    if (vbo == null) return;

    if (bufferSource instanceof MultiBufferSource.BufferSource bs) bs.endBatch();
    Quaternionf renderRot = computeInterpolatedRotation(entity, partialTick);

    poseStack.pushPose();
    poseStack.mulPose(renderRot);

    Matrix4f modelView = new Matrix4f(poseStack.last().pose());
    Matrix4f projection = RenderSystem.getProjectionMatrix();

    RenderSystem.enableDepthTest();
    RenderSystem.depthMask(true);
    RenderSystem.enableCull();
    RenderSystem.disableBlend();

    if (lod <= BakedVehicleMesh.LOD_REDUCED)
    {
      if (shadersActive) RenderSystem.setShader(GameRenderer::getRendertypeSolidShader);
      else RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
      RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
      RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

      vbo.bind();
      vbo.drawWithShader(modelView, projection, RenderSystem.getShader());
      VertexBuffer.unbind();

    } else
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

  private Quaternionf computeInterpolatedRotation(T entity, float partialTick)
  {
    if (entity.renderPrevRot == null || entity.renderTargetRot == null)
      return new Quaternionf(entity.getVehicleRotation());

    if (entity.lerpSteps <= 0)
      return new Quaternionf(entity.renderTargetRot);

    float t = partialTick / Math.max(1, entity.lerpSteps);
    return new Quaternionf(entity.renderPrevRot).slerp(entity.renderTargetRot, t);
  }

}

