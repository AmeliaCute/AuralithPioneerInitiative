package cute.ame.auralithpioneerinitiative.vehicle.Client;

import com.mojang.blaze3d.vertex.PoseStack;
import cute.ame.auralithpioneerinitiative.vehicle.Entity.BlockVehicleEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NoopVehicleRenderer<T extends BlockVehicleEntity> extends EntityRenderer<T>
{
  public NoopVehicleRenderer(EntityRendererProvider.Context context)
  {
    super(context);
  }

  @Override
  public boolean shouldRender(T entity, Frustum frustum, double camX, double camY, double camZ)
  {
    return false;
  }

  @Override
  public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight)
  {
  }

  @Override
  public ResourceLocation getTextureLocation(T entity)
  {
    return ResourceLocation.withDefaultNamespace("textures/misc/67.png");
  }
}