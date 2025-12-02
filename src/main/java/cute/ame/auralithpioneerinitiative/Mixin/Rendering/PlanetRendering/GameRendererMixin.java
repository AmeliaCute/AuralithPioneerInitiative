package cute.ame.auralithpioneerinitiative.Mixin.Rendering.PlanetRendering;


import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "getProjectionMatrix",at = @At("RETURN"),cancellable = true)
    private void onGetProjectionMatrix(double fov, CallbackInfoReturnable<Matrix4f> cir)
    {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.level.dimension().location().getPath().equals("space"))
        {
            float aspectRatio = (float) minecraft.getWindow().getWidth() / (float) minecraft.getWindow().getHeight();
            float nearPlane = 0.05f;
            float farPlane = 1000000.0f;

            Matrix4f newMatrix = new Matrix4f().setPerspective((float) Math.toRadians(fov), aspectRatio, nearPlane, farPlane);
            cir.setReturnValue(newMatrix);
        }
    }
}