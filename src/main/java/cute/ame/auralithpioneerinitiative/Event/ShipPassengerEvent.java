package cute.ame.auralithpioneerinitiative.Event;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity;
import cute.ame.auralithpioneerinitiative.Ship.Physics.ShipTransformData;
import cute.ame.auralithpioneerinitiative.Ship.Physics.ShipTransformRegistry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = Auralithpioneerinitiative.MODID)
public class ShipPassengerEvent
{
  private ShipPassengerEvent() {}

  @SubscribeEvent
  public static void onPlayerTick(PlayerTickEvent.Pre event)
  {
    Player player = event.getEntity();
    if (player.level().isClientSide()) return;
    if (player.isPassenger()) return;

    Vec3 playerPos = player.position();
    Optional<ShipTransformData> currentOpt = ShipTransformRegistry.findContaining(playerPos);
    if (currentOpt.isEmpty()) return;

    UUID shipId = currentOpt.get().shipId();

    ShipTransformData current  = currentOpt.get();
    ShipTransformData previous = ShipTransformRegistry.getPrevious(shipId).orElse(null);
    if (previous == null) return;

    Vec3 localPos = previous.toShip(playerPos);
    Vec3 newWorldPos = current.toWorld(localPos);
    Vec3 delta = newWorldPos.subtract(playerPos);
    if (delta.lengthSqr() < 1e-10) return;

    player.setPos(newWorldPos.x, newWorldPos.y, newWorldPos.z);
    player.hurtMarked = true;
  }
}