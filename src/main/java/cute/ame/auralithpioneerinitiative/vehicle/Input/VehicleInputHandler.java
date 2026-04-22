package cute.ame.auralithpioneerinitiative.vehicle.Input;

import cute.ame.auralithpioneerinitiative.Ship.Input.FlightCameraState;
import cute.ame.auralithpioneerinitiative.Ship.Input.FlightKeys;

import cute.ame.auralithpioneerinitiative.vehicle.Network.VehicleInputPacket;
import cute.ame.auralithpioneerinitiative.vehicle.Types.GroundVehicleEntity;
import cute.ame.auralithpioneerinitiative.vehicle.Types.RocketEntity;
import cute.ame.auralithpioneerinitiative.vehicle.Types.SpaceshipEntity;
import cute.ame.auralithpioneerinitiative.vehicle.Types.SubmarineEntity;
import cute.ame.auralithpioneerinitiative.vehicle.Entity.AbstractVehicleEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT)
public final class VehicleInputHandler
{

  private VehicleInputHandler() {}

  private static final long RATE_MS = 50L;
  private static long lastSentMs = 0L;

  private static VehicleInputPacket lastSentPacket = null;

  private static final float MOUSE_SENSITIVITY = 0.0025f;
  private static final float MOUSE_MAX = 1.0f;

  @SubscribeEvent
  public static void onClientTick(ClientTickEvent.Post event)
  {
    Minecraft mc = Minecraft.getInstance();
    if (mc.level == null || mc.player == null) return;

    LocalPlayer player = mc.player;

    if (!(player.getVehicle() instanceof AbstractVehicleEntity vehicle))
    {
      FlightCameraState.reset();
      lastSentPacket = null;
      return;
    }

    boolean isPilotOfVehicle = switch (vehicle)
    {
      case SpaceshipEntity s -> s.getPilotUUID().map(id -> id.equals(player.getUUID())).orElse(false);
      case RocketEntity r -> r.getPilotUUID().map(id -> id.equals(player.getUUID())).orElse(false);
      case SubmarineEntity s -> s.getPilotUUID().map(id -> id.equals(player.getUUID())).orElse(false);
      case GroundVehicleEntity g -> g.getPilotUUID().map(id -> id.equals(player.getUUID())).orElse(false);
      default -> false;
    };
    if (!isPilotOfVehicle) return;

    boolean freeLook = FlightKeys.KEY_FREE_LOOK.isDown();
    FlightCameraState.setFreeLook(freeLook);

    float mouseRoll = 0f;
    if (!freeLook)
    {
      double rawDX = FlightCameraState.consumeDX();
      float sens = (float)(mc.options.sensitivity().get() * 0.6 + 0.2);
      sens = sens * sens * sens;
      mouseRoll = clamp((float)(-rawDX * MOUSE_SENSITIVITY * sens), -MOUSE_MAX, MOUSE_MAX);
    }
    else
    {
      FlightCameraState.consumeDX();
      FlightCameraState.consumeDY();
    }

    float thrustZ = axis(FlightKeys.KEY_THRUST_BACK) - axis(FlightKeys.KEY_THRUST_FWD);
    float thrustX = axis(FlightKeys.KEY_THRUST_RIGHT) - axis(FlightKeys.KEY_THRUST_LEFT);
    float thrustY = axis(FlightKeys.KEY_THRUST_UP) - axis(FlightKeys.KEY_THRUST_DOWN);

    float kbRoll = axis(FlightKeys.KEY_ROLL_LEFT) - axis(FlightKeys.KEY_ROLL_RIGHT);
    float rollAx = clamp(mouseRoll + kbRoll, -1f, 1f);

    boolean boost = FlightKeys.KEY_BOOST.isDown();
    boolean dismount = FlightKeys.KEY_DISMOUNT.consumeClick();
    boolean anyInput = thrustX != 0 || thrustY != 0 || thrustZ != 0 || rollAx != 0 || boost || dismount;

    if (!anyInput)
    {
      if (lastSentPacket != null && hasAnyInput(lastSentPacket))
      {
        VehicleInputPacket idle = VehicleInputPacket.build(vehicle.getUUID(), 0, 0, 0, 0, false, false);
        PacketDistributor.sendToServer(idle);
        lastSentPacket = idle;
        lastSentMs = System.currentTimeMillis();
      }
      return;
    }

    VehicleInputPacket packet = VehicleInputPacket.build(vehicle.getUUID(), thrustX, thrustY, thrustZ, rollAx, boost, dismount);

    long now = System.currentTimeMillis();
    if (now - lastSentMs < RATE_MS) return;
    if (packet.equals(lastSentPacket)) return;

    PacketDistributor.sendToServer(packet);
    lastSentPacket = packet;
    lastSentMs = now;
  }

  private static float axis(net.minecraft.client.KeyMapping key) {
    return key.isDown() ? 1f : 0f;
  }

  private static float clamp(float v, float min, float max) {
    return Math.max(min, Math.min(max, v));
  }

  private static float clamp(float v) {
    return clamp(v, -1f, 1f);
  }

  private static boolean hasAnyInput(VehicleInputPacket p)
  {
    return p.thrustX() != 0 || p.thrustY() != 0 || p.thrustZ() != 0 || p.rollInput() != 0 || p.boosting() || p.dismounting();
  }
}

