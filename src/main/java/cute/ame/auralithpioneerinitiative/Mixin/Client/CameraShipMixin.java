package cute.ame.auralithpioneerinitiative.Mixin.Client;

import cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity;
import cute.ame.auralithpioneerinitiative.Ship.Input.FlightCameraState;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraShipMixin
{
  @Shadow private float xRot;
  @Shadow private float yRot;
  @Shadow protected abstract void setRotation(float yRot, float xRot);

  @Inject(method = "setup", at = @At("TAIL"))
  private void auralith$overrideShipCamera(BlockGetter level, Entity entity, boolean detached, boolean thirdPerson, float partialTick, CallbackInfo ci)
  {
    if (FlightCameraState.isFreeLook()) return;
    if (!(entity.getVehicle() instanceof ShipEntity ship)) return;

    Quaternionf q = ship.getShipRotation();

    double sinPitch = 2.0 * (q.w * q.x - q.z * q.y);
    sinPitch = Math.max(-1.0, Math.min(1.0, sinPitch));
    float shipPitch = (float) Math.toDegrees(Math.asin(sinPitch));

    double yawY = 2.0 * (q.w * q.y + q.x * q.z);
    double yawX = 1.0 - 2.0 * (q.y * q.y + q.x * q.x);
    float shipYaw = (float) Math.toDegrees(Math.atan2(yawY, yawX));

    setRotation(180f - shipYaw, -shipPitch);
  }
}