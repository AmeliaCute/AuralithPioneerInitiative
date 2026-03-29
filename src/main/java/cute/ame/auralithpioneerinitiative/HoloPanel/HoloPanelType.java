package cute.ame.auralithpioneerinitiative.HoloPanel;

import cute.ame.auralithpioneerinitiative.HoloPanel.Anchor.PanelAnchor;
import cute.ame.auralithpioneerinitiative.HoloPanel.Widget.HoloPanelContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public interface HoloPanelType
{
  ResourceLocation getId();
  int getWidth();
  int getHeight();
  PanelAnchor getAnchor();

  default float getFovGate() { return 0.5f; }
  default boolean isInteractable() { return true; }

  default float worldWidth() { return 1.5f; }
  default float worldHeight() { return 1.0f; }

  void renderContent(GuiGraphics g, HoloPanelContext ctx, float partialTick);
  default void onAction(String action, HoloPanelContext ctx) {}
}