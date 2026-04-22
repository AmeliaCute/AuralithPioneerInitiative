package cute.ame.auralithpioneerinitiative.vehicle.Client;

import cute.ame.auralithpioneerinitiative.vehicle.Seat.CameraConfig;
import cute.ame.auralithpioneerinitiative.vehicle.Entity.AbstractVehicleEntity;
import cute.ame.auralithpioneerinitiative.vehicle.Entity.VehicleSeat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Vector3f;

import java.lang.reflect.Field;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT)
public final class VehicleCameraHandler
{
  private VehicleCameraHandler() {}

  private static Field cameraPositionField = null;
  private static boolean reflectionFailed = false;
  private static java.util.UUID lastMountedVehicleId = null;

  @SubscribeEvent
  public static void onClientTick(ClientTickEvent.Post event)
  {
    LocalPlayer player = Minecraft.getInstance().player;
    if (player == null) return;

    if (!(player.getVehicle() instanceof AbstractVehicleEntity vehicle))
    {
      lastMountedVehicleId = null;
      return;
    }

    if (!vehicle.getUUID().equals(lastMountedVehicleId))
    {
      lastMountedVehicleId = vehicle.getUUID();
      player.setYRot(vehicleToWorldYaw(vehicle));
      player.setXRot(vehicleToWorldPitch(vehicle));
      player.yRotO = player.getYRot();
      player.xRotO = player.getXRot();
    }
  }

  @SubscribeEvent
  public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event)
  {
    LocalPlayer player = Minecraft.getInstance().player;
    if (player == null) return;
    if (!(player.getVehicle() instanceof AbstractVehicleEntity vehicle)) return;

    var rawSeat = vehicle.getSeatOf(player);
    if (!(rawSeat instanceof VehicleSeat seat)) return;

    CameraConfig cfg = seat.getDefinition().camera();
    float pt = (float) event.getPartialTick();

    Vec3 eyePos = seat.getInterpolatedEyePosition(vehicle, pt);
    setCameraPosition(event.getCamera(), eyePos);

    float clampedPitch = (float) Math.max(cfg.minPitch(), Math.min(cfg.maxPitch(), event.getPitch()));
    event.setPitch(clampedPitch);

//    FUNC DOES NOT EXIST, MAY BE UNNECESSARY ANYWAY
//    if (cfg.defaultFov() > 0) {
//      event.setFOV(cfg.defaultFov());
//    }
  }

//  @SubscribeEvent
//  public static void onComputeFov(ViewportEvent.ComputeFov event)
//  {
//  }

  public static float vehicleToWorldYaw(AbstractVehicleEntity vehicle)
  {
    Vector3f euler = new Vector3f();
    vehicle.getVehicleRotation().getEulerAnglesYXZ(euler);
    return (float) Math.toDegrees(-euler.y) + 180f;
  }

  public static float vehicleToWorldPitch(AbstractVehicleEntity vehicle)
  {
    Vector3f euler = new Vector3f();
    vehicle.getVehicleRotation().getEulerAnglesYXZ(euler);
    return (float) Math.toDegrees(-euler.x);
  }

  private static void setCameraPosition(Camera camera, Vec3 eyePos)
  {
    if (reflectionFailed) return;

    try
    {
      if (cameraPositionField == null)
      {
        cameraPositionField = findPositionField(camera.getClass());
        if (cameraPositionField == null)
        {
          reflectionFailed = true;
          return;
        }
        cameraPositionField.setAccessible(true);
      }
      cameraPositionField.set(camera, eyePos);
    }
    catch (Exception e)
    {
      reflectionFailed = true;
    }
  }

  private static Field findPositionField(Class<?> clazz)
  {
    for (String name : new String[]{"position", "f_90594_", "eyePosition"})
    {
      try
      {
        Field f = Camera.class.getDeclaredField(name);
        if (f.getType() == Vec3.class) return f;
      }
      catch (NoSuchFieldException ignored) {}
    }
    for (Field f : Camera.class.getDeclaredFields())
    {
      if (f.getType() == Vec3.class) return f;
    }
    return null;
  }
}

