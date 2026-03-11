package cute.ame.auralithpioneerinitiative.Mixin.Rendering;

import cute.ame.auralithpioneerinitiative.Client.Sky.SolarSystemRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class SkyMixin
{
    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void auralith$renderSky(Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, Camera camera, boolean isFoggy, Runnable skyFogSetup, CallbackInfo ci)
    {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;

        SolarSystemRenderer renderer = SolarSystemRenderer.getInstance();
        renderer.renderSky(frustumMatrix, projectionMatrix, partialTick, camera, isFoggy, skyFogSetup, level);
        ci.cancel();
    }
}
