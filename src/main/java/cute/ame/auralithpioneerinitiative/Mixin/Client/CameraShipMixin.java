package cute.ame.auralithpioneerinitiative.Mixin.Client;

import cute.ame.auralithpioneerinitiative.Ship.Client.ShipClientPhysics;
import cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity;
import cute.ame.auralithpioneerinitiative.Ship.Input.FlightCameraState;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraShipMixin
{
  @Shadow private Quaternionf rotation;

  @Shadow
  private float yRot;

  @Shadow
  private float xRot;

  @Inject(method = "setup", at = @At("TAIL"))
  private void auralith$handleShipCamera(BlockGetter level, Entity entity, boolean detached, boolean thirdPerson, float partialTick, CallbackInfo ci) {
    if (!(entity.getVehicle() instanceof ShipEntity ship)) return;

    ShipClientPhysics.State cs = ShipClientPhysics.get(ship.getUUID());
    Quaternionf shipQuat = (cs != null) ? new Quaternionf(cs.rot()) : new Quaternionf(ship.getShipRotation());

    if (FlightCameraState.isFreeLook())
    {
      double dx = FlightCameraState.consumeDX();
      double dy = FlightCameraState.consumeDY();
      float sensitivity = 0.15f;

      entity.setYRot(entity.getYRot() - (float) (dx * sensitivity));
      entity.setXRot(entity.getXRot() - (float) (dy * sensitivity));
      entity.setXRot(Math.max(-90f, Math.min(90f, entity.getXRot())));

      this.rotation.set(shipQuat).rotateLocalY((float) Math.toRadians(-entity.getYRot())).rotateLocalX((float) Math.toRadians(entity.getXRot()));

    } else {
      entity.setYRot(0f);
      entity.setXRot(0f);

      this.rotation.set(shipQuat);
    }

    Vector3f fwd = this.rotation.transformUnit(new Vector3f(0, 0, -1));
    this.yRot = (float) Math.toDegrees(Math.atan2(-fwd.x, fwd.z));
    this.xRot = (float) Math.toDegrees(Math.atan2(-fwd.y, Math.sqrt(fwd.x * fwd.x + fwd.z * fwd.z)));

    Vector3f up = this.rotation.transformUnit(new Vector3f(0, 1, 0));
    FlightCameraState.setRoll((float) Math.atan2(up.x, up.y));
  }
}