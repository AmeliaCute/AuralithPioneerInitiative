package cute.ame.auralithpioneerinitiative.SpaceSuit.Input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class SuitKeybinds
{
  private SuitKeybinds() {}

  public static final KeyMapping SUIT_MENU = new KeyMapping(
      "key.auralith.suit.menu",
      InputConstants.Type.KEYSYM,
      GLFW.GLFW_KEY_G,
      "key.categories.auralith.flight"
  );

  public static final KeyMapping FLASHLIGHT = new KeyMapping(
      "key.auralith.suit.flashlight",
      InputConstants.Type.KEYSYM,
      GLFW.GLFW_KEY_F,
      "key.categories.auralith.flight"
  );

  public static final KeyMapping[] ALL = { SUIT_MENU, FLASHLIGHT };
}