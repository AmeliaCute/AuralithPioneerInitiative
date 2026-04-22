package cute.ame.auralithpioneerinitiative.vehicle.Client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.vehicle.Baking.BakedVehicleMesh;
import cute.ame.auralithpioneerinitiative.vehicle.Entity.AbstractVehicleEntity;
import cute.ame.auralithpioneerinitiative.vehicle.Entity.BlockVehicleEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Auralithpioneerinitiative.MODID, value = Dist.CLIENT)
public final class VehicleRenderDispatcher
{

  private VehicleRenderDispatcher() {}

  @SubscribeEvent
  public static void onRenderVehicles(RenderLevelStageEvent event)
  {
    if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) return;

    Minecraft mc = Minecraft.getInstance();
    ClientLevel level = mc.level;
    if (level == null) return;

    Camera camera = event.getCamera();
    Frustum frustum = event.getFrustum();
    float pt = event.getPartialTick().getGameTimeDeltaPartialTick(true);
    Matrix4f proj = RenderSystem.getProjectionMatrix();

    for (Entity entity : level.entitiesForRendering())
    {
      if (!(entity instanceof BlockVehicleEntity vehicle)) continue;

      VehicleClientCache.registerEntity((AbstractVehicleEntity) vehicle);
      BakedVehicleMesh mesh = VehicleClientCache.getMesh(vehicle.getUUID());
      if (mesh == null || !mesh.isReady()) continue;
      if (!frustum.isVisible(mesh.getLocalBounds().move(vehicle.position()))) continue;

      int lod = VehicleLodManager.getLODLevel((AbstractVehicleEntity) vehicle, camera);
      if (lod >= 3) continue;

      boolean shadersActive = false; // TODO: brancher ShaderHelper.shadersActive()
      VertexBuffer vbo = mesh.getBuffer(lod, shadersActive);
      if (vbo == null) continue;

      renderVehicle(vehicle, vbo, lod, shadersActive, camera, pt, proj);
    }
  }

  private static void renderVehicle(BlockVehicleEntity vehicle, VertexBuffer vbo, int lod, boolean shadersActive, Camera camera, float pt, Matrix4f proj)
  {
    AbstractVehicleEntity av = (AbstractVehicleEntity) vehicle;
    Quaternionf renderRot = interpolateRotation(av, pt);

    double dx = vehicle.getX() - camera.getPosition().x;
    double dy = vehicle.getY() - camera.getPosition().y;
    double dz = vehicle.getZ() - camera.getPosition().z;

    Matrix4f modelView = new Matrix4f().translate((float)dx, (float)dy, (float)dz).rotate(renderRot);
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
    } else
    {
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
    }

    vbo.bind();
    vbo.drawWithShader(modelView, proj, RenderSystem.getShader());
    VertexBuffer.unbind();

    if (lod > BakedVehicleMesh.LOD_REDUCED) RenderSystem.disableBlend();
  }

  private static Quaternionf interpolateRotation(AbstractVehicleEntity entity, float pt)
  {
    if (entity.renderPrevRot == null || entity.renderTargetRot == null)
      return new Quaternionf(entity.getVehicleRotation());

    if (entity.lerpSteps <= 0)
      return new Quaternionf(entity.renderTargetRot);

    float t = pt / Math.max(1, entity.lerpSteps);
    return new Quaternionf(entity.renderPrevRot).slerp(entity.renderTargetRot, t);
  }
}