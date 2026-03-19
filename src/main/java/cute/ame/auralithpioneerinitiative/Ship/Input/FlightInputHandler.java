package cute.ame.auralithpioneerinitiative.Ship.Input;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Client.HUD.HoloPanelRenderer;
import cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity;
import cute.ame.auralithpioneerinitiative.Ship.Network.FlightInputPacket;
import net.minecraft.client.Minecraft;
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
  private static final float MOUSE_MAX         = 1.0f;

  @SubscribeEvent
  public static void onClientTick(ClientTickEvent.Post event)
  {
    Minecraft mc = Minecraft.getInstance();
    if (mc.level == null || mc.player == null) return;

    if (!(mc.player.getVehicle() instanceof ShipEntity))
    {
      FlightCameraState.reset();
      return;
    }

    double scrollDelta = FlightCameraState.consumeScrollDelta();
    if (scrollDelta != 0.0)
    {
      HoloPanelRenderer.getInstance().forwardScroll(scrollDelta);
    }

    boolean freeLook = FlightKeys.KEY_FREE_LOOK.isDown();
    FlightCameraState.setFreeLook(freeLook);

    if (FlightCameraState.isPanelFocused())
    {
      sendThrustPacket();
      return;
    }

    float mousePitch = 0f;
    float mouseYaw   = 0f;

    if (!freeLook)
    {
      double rawDX = FlightCameraState.consumeDX();
      double rawDY = FlightCameraState.consumeDY();

      float sens = (float)(mc.options.sensitivity().get() * 0.6 + 0.2);
      sens = sens * sens * sens;

      mouseYaw   = clamp((float)(-rawDX * MOUSE_SENSITIVITY * sens), -MOUSE_MAX, MOUSE_MAX);
      mousePitch = clamp((float)(-rawDY * MOUSE_SENSITIVITY * sens), -MOUSE_MAX, MOUSE_MAX);
    }

    float thrustZ = axis(FlightKeys.KEY_THRUST_BACK) - axis(FlightKeys.KEY_THRUST_FWD);
    float thrustX = axis(FlightKeys.KEY_THRUST_RIGHT) - axis(FlightKeys.KEY_THRUST_LEFT);
    float thrustY = axis(FlightKeys.KEY_THRUST_UP) - axis(FlightKeys.KEY_THRUST_DOWN);

    float kbPitch = axis(FlightKeys.KEY_PITCH_UP) - axis(FlightKeys.KEY_PITCH_DOWN);
    float kbYaw = axis(FlightKeys.KEY_YAW_LEFT) - axis(FlightKeys.KEY_YAW_RIGHT);
    float rollAx = axis(FlightKeys.KEY_ROLL_LEFT) - axis(FlightKeys.KEY_ROLL_RIGHT);

    float rotPitch = clamp(mousePitch + kbPitch, -1f, 1f);
    float rotYaw = clamp(mouseYaw + kbYaw, -1f, 1f);
    boolean boost = FlightKeys.KEY_BOOST.isDown();
    boolean dismount = FlightKeys.KEY_DISMOUNT.consumeClick();

    boolean anyInput = thrustX != 0 || thrustY != 0 || thrustZ != 0 || rotPitch != 0 || rotYaw != 0 || rollAx != 0 || boost || dismount;

    if (!anyInput) return;

    PacketDistributor.sendToServer(new FlightInputPacket(
        thrustX, thrustY, thrustZ,
        rotPitch, rotYaw, rollAx,
        boost, dismount
    ));
  }

  private static void sendThrustPacket() {
    float thrustZ = axis(FlightKeys.KEY_THRUST_BACK) - axis(FlightKeys.KEY_THRUST_FWD);
    float thrustX = axis(FlightKeys.KEY_THRUST_RIGHT) - axis(FlightKeys.KEY_THRUST_LEFT);
    float thrustY = axis(FlightKeys.KEY_THRUST_UP) - axis(FlightKeys.KEY_THRUST_DOWN);
    boolean dismount = FlightKeys.KEY_DISMOUNT.consumeClick();
    if (thrustX == 0 && thrustY == 0 && thrustZ == 0 && !dismount) return;
    PacketDistributor.sendToServer(new FlightInputPacket(thrustX, thrustY, thrustZ, 0, 0, 0, false, dismount));
  }

  private static float axis(net.minecraft.client.KeyMapping key) { return key.isDown() ? 1f : 0f; }

  private static float clamp(float v, float min, float max) { return Math.max(min, Math.min(max, v)); }
}