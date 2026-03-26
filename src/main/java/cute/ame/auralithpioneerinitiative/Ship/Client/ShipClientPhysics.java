package cute.ame.auralithpioneerinitiative.Ship.Client;

import cute.ame.auralithpioneerinitiative.Ship.Physics.FlightInput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public final class ShipClientPhysics
{
  private ShipClientPhysics() {}

  private static final Map<UUID, State> STATES = new ConcurrentHashMap<>();

  public record State(Vec3 pos, Quaternionf rot, Vec3 linearVel, Vec3 angularVel) {}

  public static void predict(UUID shipId, FlightInput input,
                             float thrustAccel, float maxSpeed, float angularAccel, float gravity)
  {
    State s = STATES.get(shipId);
    if (s == null) return;

    float boost = input.boosting() ? 3f : 1f;
    Vector3f localT = new Vector3f(input.thrustX() * thrustAccel * boost, input.thrustY() * thrustAccel * boost, input.thrustZ() * thrustAccel * boost);
    new Quaternionf(s.rot()).transform(localT);

    Vec3 worldThrust = new Vec3(localT.x, localT.y - gravity * 0.002, localT.z);
    Vec3 lv = s.linearVel().add(worldThrust);
    lv = lv.scale(1.0 - (gravity > 0f ? 0.025 : 0.002));
    double cap = input.boosting() ? maxSpeed * 3.0 : maxSpeed;
    if (lv.lengthSqr() > cap * cap) lv = lv.normalize().scale(cap);

    Vec3 av = s.angularVel().add(input.rotPitch() * angularAccel, input.rotYaw()   * angularAccel, input.rotRoll()  * angularAccel).scale(1.0 - 0.10);

    Quaternionf dq = avToQuat(av);
    Quaternionf newRot = new Quaternionf(s.rot()).mul(dq).normalize();
    Vec3 newPos = s.pos().add(lv);

    STATES.put(shipId, new State(newPos, newRot, lv, av));
  }

  public static void reconcile(UUID shipId, Vec3 serverPos, Quaternionf serverRot, Vec3 serverVel)
  {
    State s = STATES.get(shipId);
    if (s == null)
    {
      STATES.put(shipId, new State(serverPos, new Quaternionf(serverRot), serverVel, Vec3.ZERO));
      return;
    }

    double posDrift = s.pos().distanceToSqr(serverPos);

    if (posDrift > 256.0)
    {
      STATES.put(shipId, new State(serverPos, new Quaternionf(serverRot),
          serverVel, s.angularVel()));
      return;
    }

    Vec3 lerpPos = s.pos().lerp(serverPos, 0.15);
    Quaternionf lerpRot = new Quaternionf(s.rot()).slerp(serverRot, 0.15f);
    Vec3 lerpVel = s.linearVel().lerp(serverVel, 0.15);

    STATES.put(shipId, new State(lerpPos, lerpRot, lerpVel, s.angularVel()));
  }

  public static State get(UUID id) { return STATES.get(id); }
  public static void remove(UUID id) { STATES.remove(id); }
  public static void clear() { STATES.clear(); }

  private static Quaternionf avToQuat(Vec3 av)
  {
    double len = av.length();
    if (len < 1e-9) return new Quaternionf();
    double half = len * 0.5;
    float s = (float)(Math.sin(half) / len);
    return new Quaternionf((float)(av.x * s), (float)(av.y * s), (float)(av.z * s), (float) Math.cos(half));
  }
}