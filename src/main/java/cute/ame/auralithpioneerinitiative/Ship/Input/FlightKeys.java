package cute.ame.auralithpioneerinitiative.Ship.Input;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;

@OnlyIn(Dist.CLIENT)
public final class FlightKeys
{
  private FlightKeys() {}

  public static final String CATEGORY = "key.categories.auralith.flight";

  public static final KeyMapping KEY_THRUST_FWD = key("flight.thrust_forward", GLFW.GLFW_KEY_W);
  public static final KeyMapping KEY_THRUST_BACK = key("flight.thrust_back", GLFW.GLFW_KEY_S);
  public static final KeyMapping KEY_THRUST_LEFT = key("flight.thrust_left", GLFW.GLFW_KEY_A);
  public static final KeyMapping KEY_THRUST_RIGHT = key("flight.thrust_right", GLFW.GLFW_KEY_D);
  public static final KeyMapping KEY_THRUST_UP = key("flight.thrust_up", GLFW.GLFW_KEY_SPACE);
  public static final KeyMapping KEY_THRUST_DOWN  = key("flight.thrust_down", GLFW.GLFW_KEY_LEFT_CONTROL);

  public static final KeyMapping KEY_PITCH_UP = key("flight.pitch_up", GLFW.GLFW_KEY_UP);
  public static final KeyMapping KEY_PITCH_DOWN = key("flight.pitch_down", GLFW.GLFW_KEY_DOWN);
  public static final KeyMapping KEY_YAW_LEFT = key("flight.yaw_left", GLFW.GLFW_KEY_LEFT);
  public static final KeyMapping KEY_YAW_RIGHT = key("flight.yaw_right", GLFW.GLFW_KEY_RIGHT);
  public static final KeyMapping KEY_ROLL_LEFT = key("flight.roll_left", GLFW.GLFW_KEY_Q);
  public static final KeyMapping KEY_ROLL_RIGHT = key("flight.roll_right", GLFW.GLFW_KEY_E);

  public static final KeyMapping KEY_BOOST = key("flight.boost", GLFW.GLFW_KEY_LEFT_SHIFT);
  public static final KeyMapping KEY_DISMOUNT = key("flight.dismount", GLFW.GLFW_KEY_R);

  public static final KeyMapping KEY_FREE_LOOK = new KeyMapping("key.auralith.flight.free_look", InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_MIDDLE, CATEGORY);

  public static final KeyMapping[] ALL =
  {
    KEY_THRUST_FWD, KEY_THRUST_BACK,  KEY_THRUST_LEFT, KEY_THRUST_RIGHT,  KEY_THRUST_UP, KEY_THRUST_DOWN,  KEY_PITCH_UP, KEY_PITCH_DOWN,  KEY_YAW_LEFT, KEY_YAW_RIGHT,  KEY_ROLL_LEFT, KEY_ROLL_RIGHT,  KEY_BOOST, KEY_FREE_LOOK, KEY_DISMOUNT
  };

  private static KeyMapping key(String name, int glfwKey)
  {
    return new KeyMapping("key.auralith." + name, glfwKey, CATEGORY);
  }
}