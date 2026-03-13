package cute.ame.auralithpioneerinitiative.Mixin.Client;

import cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity;
import cute.ame.auralithpioneerinitiative.Ship.Input.FlightCameraState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.SmoothDouble;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseFlightMixin
{
  @Shadow private double accumulatedDX;
  @Shadow private double accumulatedDY;

  @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
  private void auralith$interceptMouseForFlight(double movementTime, CallbackInfo ci)
  {
    Minecraft mc = Minecraft.getInstance();
    if (mc.player == null) return;
    if (!(mc.player.getVehicle() instanceof ShipEntity)) return;
    if (FlightCameraState.isFreeLook()) return; // let vanilla handle camera

    FlightCameraState.addMouseDelta(accumulatedDX * 2.5f, accumulatedDY * 1.5f);

    accumulatedDX = 0.0;
    accumulatedDY = 0.0;
    ci.cancel();
  }
}