package cute.ame.auralithpioneerinitiative.Mixin.Client;

import cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity;
import cute.ame.auralithpioneerinitiative.Ship.Input.FlightCameraState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseScrollMixin
{
  @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
  private void auralith$captureScrollForPanel(long window, double xOffset, double yOffset, CallbackInfo ci)
  {
    Minecraft mc = Minecraft.getInstance();
    if (mc.player == null) return;
    if (!(mc.player.getVehicle() instanceof ShipEntity)) return;
    if (!FlightCameraState.isPanelFocused()) return;

    FlightCameraState.addScrollDelta(yOffset);
    ci.cancel();
  }
}