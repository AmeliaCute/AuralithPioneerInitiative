package cute.ame.auralithpioneerinitiative.SpaceSuit.HUD;


import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Registrie.ModAttachments;
import cute.ame.auralithpioneerinitiative.SpaceSuit.HUD.Components.*;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Item.SuitArmorItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public final class SuitHudOverlay
{
  private SuitHudOverlay() {}

  public static final ResourceLocation LAYER_ID = ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "suit_hud");

  public static void onRegisterGuiLayers(RegisterGuiLayersEvent event)
  {
    event.registerAboveAll(LAYER_ID, SuitHudOverlay::render);
  }

  private static void render(GuiGraphics gui, DeltaTracker dt)
  {
    if (!isActive()) return;
    Minecraft mc = Minecraft.getInstance();
    if (mc.player == null || mc.screen != null || mc.getOverlay() != null) return;

    int w = mc.getWindow().getGuiScaledWidth();
    int h = mc.getWindow().getGuiScaledHeight();

    CompassRenderer.render(gui, w);
    SuitGaugesRenderer.render(gui, h);
  }

  public static boolean isActive()
  {
    Minecraft mc = Minecraft.getInstance();
    if (mc.player == null || mc.player.isSpectator()) return false;
    return mc.player.getInventory().getArmor(3).getItem() instanceof SuitArmorItem;
  }
}