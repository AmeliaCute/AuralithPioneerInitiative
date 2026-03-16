package cute.ame.auralithpioneerinitiative.Ship.Physics;

import net.minecraft.world.phys.Vec3;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ShipTransformRegistry
{
  private ShipTransformRegistry() {}

  private static final Map<UUID, ShipTransformData> CURRENT  = new ConcurrentHashMap<>();
  private static final Map<UUID, ShipTransformData> PREVIOUS = new ConcurrentHashMap<>();

  public static void update(UUID id, ShipTransformData data)
  {
    ShipTransformData prev = CURRENT.get(id);
    if (prev != null) PREVIOUS.put(id, prev);
    CURRENT.put(id, data);
  }

  public static Optional<ShipTransformData> get(UUID id)
  {
    return Optional.ofNullable(CURRENT.get(id));
  }

  public static Optional<ShipTransformData> getPrevious(UUID id)
  {
    return Optional.ofNullable(PREVIOUS.get(id));
  }

  public static void remove(UUID id)
  {
    CURRENT.remove(id);
    PREVIOUS.remove(id);
  }

  public static Collection<ShipTransformData> all()
  {
    return CURRENT.values();
  }

  public static Optional<ShipTransformData> findContaining(Vec3 worldPos)
  {
    for (ShipTransformData t : CURRENT.values())
    {
      if (t.localBounds() == null) continue;
      Vec3 local = t.toShip(worldPos);
      if (t.localBounds().inflate(1.0).contains(local)) return Optional.of(t);
    }
    return Optional.empty();
  }
}