package cute.ame.auralithpioneerinitiative.Mixin.Rendering;

import cute.ame.auralithpioneerinitiative.Client.HUD.HoloPanelRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class HoloPanelRenderMixin
{

  @Inject(method = "renderLevel", at = @At("TAIL"))
  private void auralith$renderHoloPanels(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci)
  {
    net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
    if (mc.level == null) return;
    HoloPanelRenderer.getInstance().renderPanels(frustumMatrix, projectionMatrix, deltaTracker.getGameTimeDeltaPartialTick(true), camera, mc.level);
  }
}