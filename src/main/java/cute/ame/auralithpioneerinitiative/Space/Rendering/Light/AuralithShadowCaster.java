package cute.ame.auralithpioneerinitiative.Space.Rendering.Light;

import net.minecraft.world.phys.Vec3;

public interface AuralithShadowCaster
{
    boolean intersectsRay(Vec3 origin, Vec3 direction, float maxDistance);
}
