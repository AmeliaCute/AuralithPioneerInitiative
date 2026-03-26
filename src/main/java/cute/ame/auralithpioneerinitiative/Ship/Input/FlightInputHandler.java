package cute.ame.auralithpioneerinitiative.Ship.Input;

import cute.ame.auralithpioneerinitiative.API.AuralithAPI;
import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Ship.Client.ShipClientPhysics;
import cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity;
import cute.ame.auralithpioneerinitiative.Ship.Network.FlightInputPacket;
import cute.ame.auralithpioneerinitiative.Ship.Physics.FlightInput;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Auralithpioneerinitiative.MODID, value = Dist.CLIENT)
public final class FlightInputHandler
{
  private FlightInputHandler() {}

  private static final float MOUSE_SENSITIVITY = 0.0025f;
  private static final float MOUSE_MAX = 1.0f;

  private static final float DEFAULT_THRUST_ACCEL  = 0.06f;
  private static final float DEFAULT_MAX_SPEED = 64.0f;
  private static final float DEFAULT_ANGULAR_ACCEL  = 0.006f;

  @SubscribeEvent
  public static void onClientTick(ClientTickEvent.Post event)
  {
    Minecraft mc = Minecraft.getInstance();
    if (mc.level == null || mc.player == null) return;

    if (!(mc.player.getVehicle() instanceof ShipEntity ship))
    {
      FlightCameraState.reset();
      ShipClientPhysics.clear();
      return;
    }

    boolean freeLook = FlightKeys.KEY_FREE_LOOK.isDown();
    FlightCameraState.setFreeLook(freeLook);

    float mousePitch = 0f, mouseYaw = 0f;
    if (!freeLook)
    {
      double rawDX = FlightCameraState.consumeDX();
      double rawDY = FlightCameraState.consumeDY();
      float  sens  = (float)(mc.options.sensitivity().get() * 0.6 + 0.2);
      sens = sens * sens * sens;
      mouseYaw = clamp((float)(-rawDX * MOUSE_SENSITIVITY * sens), -MOUSE_MAX, MOUSE_MAX);
      mousePitch = clamp((float)(-rawDY * MOUSE_SENSITIVITY * sens), -MOUSE_MAX, MOUSE_MAX);
    }

    float thrustZ = axis(FlightKeys.KEY_THRUST_BACK) - axis(FlightKeys.KEY_THRUST_FWD);
    float thrustX = axis(FlightKeys.KEY_THRUST_RIGHT) - axis(FlightKeys.KEY_THRUST_LEFT);
    float thrustY = axis(FlightKeys.KEY_THRUST_UP) - axis(FlightKeys.KEY_THRUST_DOWN);

    float kbPitch = axis(FlightKeys.KEY_PITCH_UP) - axis(FlightKeys.KEY_PITCH_DOWN);
    float kbYaw = axis(FlightKeys.KEY_YAW_LEFT) - axis(FlightKeys.KEY_YAW_RIGHT);
    float rollAx = axis(FlightKeys.KEY_ROLL_LEFT)  - axis(FlightKeys.KEY_ROLL_RIGHT);

    float rotPitch = clamp(mousePitch + kbPitch, -1f, 1f);
    float rotYaw = clamp(mouseYaw + kbYaw, -1f, 1f);
    boolean boost = FlightKeys.KEY_BOOST.isDown();
    boolean dismount = FlightKeys.KEY_DISMOUNT.consumeClick();

    boolean anyInput = thrustX != 0 || thrustY != 0 || thrustZ != 0 || rotPitch != 0 || rotYaw != 0 || rollAx != 0 || boost || dismount;
    FlightInput fi = anyInput ? new FlightInput(thrustX, thrustY, thrustZ, rotPitch, rotYaw, rollAx, boost, dismount) : FlightInput.IDLE;

    if (anyInput) PacketDistributor.sendToServer(new FlightInputPacket(thrustX, thrustY, thrustZ, rotPitch, rotYaw, rollAx, boost, dismount));
    if (ShipClientPhysics.get(ship.getUUID()) == null)  ShipClientPhysics.reconcile(ship.getUUID(), ship.position(), ship.getShipRotation(), Vec3.ZERO);

    float thrustAccel = DEFAULT_THRUST_ACCEL;
    float maxSpeed = DEFAULT_MAX_SPEED;
    float angularAccel = DEFAULT_ANGULAR_ACCEL;
    var defOpt = ship.getDefinition();
    if (defOpt.isPresent())
    {
      var def  = defOpt.get();
      float mass = Math.max(1f, def.mass());
      thrustAccel  = Math.max(0.005f, Math.min(0.25f, 60_000f / (mass * 1_000f)));
      maxSpeed = maxSpeedForClass(def.shipClass()) * 16.0f;
      int[] sz = def.computeSize();
      float dia = (float) Math.sqrt(sz[0]*sz[0] + sz[1]*sz[1] + sz[2]*sz[2]);
      float moi = Math.max(1f, mass * (dia * dia) / 20f);
      angularAccel = Math.max(0.0005f, Math.min(0.02f, 1_000f / moi));
    }

    float gravity = AuralithAPI.getGravityFor(mc.level.dimension());
    ShipClientPhysics.predict(ship.getUUID(), fi, thrustAccel, maxSpeed, angularAccel, gravity);
  }

  private static float axis(net.minecraft.client.KeyMapping key)
  {
    return key.isDown() ? 1f : 0f;
  }

  private static float clamp(float v, float min, float max)
  {
    return Math.max(min, Math.min(max, v));
  }

  private static float maxSpeedForClass(String cls)
  {
    return switch (cls == null ? "" : cls.toLowerCase(java.util.Locale.ROOT))
    {
      case "heavy_fighter" -> 2.8f;
      case "corvette" -> 3.2f;
      case "frigate" -> 2.0f;
      case "cargo" -> 1.2f;
      case "capital" -> 0.6f;
      default -> 4.0f;
    };
  }
}