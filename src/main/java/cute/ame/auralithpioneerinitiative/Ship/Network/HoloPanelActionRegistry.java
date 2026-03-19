package cute.ame.auralithpioneerinitiative.Ship.Network;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public final class HoloPanelActionRegistry
{
  private HoloPanelActionRegistry() {}

  private record Key(ResourceLocation panel, String action) {}
  private static final Map<Key, BiConsumer<String, ServerPlayer>> HANDLERS = new HashMap<>();

  public static void register(ResourceLocation panelType, String action, BiConsumer<String, ServerPlayer> handler) { HANDLERS.put(new Key(panelType, action), handler); }
  public static void dispatch(ResourceLocation panelType, String action, ServerPlayer player)
  {
    var key = new Key(panelType, action);
    BiConsumer<String, ServerPlayer> handler = HANDLERS.get(key);

    if (handler == null)
    {
      String prefix = action.contains(":") ? action.substring(0, action.lastIndexOf(':') + 1) + "*" : "*";
      handler = HANDLERS.get(new Key(panelType, prefix));
    }

    if (handler != null) handler.accept(action, player);
    else Auralithpioneerinitiative.LOGGER.warn("[Auralith] No handler for panel action '{}' on panel '{}'", action, panelType);
  }


  static
  {
    ResourceLocation systemsPanel = ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "systems_panel");

    register(systemsPanel, "subsystem:toggle:*", (action, player) ->
    {
      Entity vehicle = player.getVehicle();
      if (!(vehicle instanceof ShipEntity ship)) return;

      String sub = action.substring(action.lastIndexOf(':') + 1);
      int bit = switch (sub)
      {
        case "SYS" -> ShipEntity.SUBSYSTEM_SHIELDS;
        case "ENG" -> ShipEntity.SUBSYSTEM_ENGINE;
        case "WEP" -> ShipEntity.SUBSYSTEM_WEAPONS;
        default -> 0;
      };
      if (bit != 0) ship.setSubsystemState(ship.getSubsystemState() ^ bit);
    });
  }
}