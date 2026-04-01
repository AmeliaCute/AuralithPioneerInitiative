package cute.ame.auralithpioneerinitiative.SpaceSuit.HUD;


import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Registrie.ModAttachments;
import cute.ame.auralithpioneerinitiative.SpaceSuit.HUD.Components.*;
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

  private static final ResourceLocation[] SUPPRESSED =
  {
    VanillaGuiLayers.PLAYER_HEALTH,
    VanillaGuiLayers.FOOD_LEVEL,
    VanillaGuiLayers.ARMOR_LEVEL,
    VanillaGuiLayers.AIR_LEVEL,
    VanillaGuiLayers.HOTBAR
  };

  public static void onRegisterGuiLayers(RegisterGuiLayersEvent event)
  {
    event.registerAboveAll(LAYER_ID, SuitHudOverlay::render);
  }

  public static void onPreRenderLayer(RenderGuiLayerEvent.Pre event)
  {
    if (!isActive()) return;
    ResourceLocation layer = event.getName();
    for (ResourceLocation sup : SUPPRESSED)
      if (sup.equals(layer)) { event.setCanceled(true); return; }
  }

  private static void render(GuiGraphics gui, DeltaTracker dt)
  {
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
    return mc.player.getData(ModAttachments.SUIT_DATA).isHelmetOn();
  }
}