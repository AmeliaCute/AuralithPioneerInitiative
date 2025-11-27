package cute.ame.auralithpioneerinitiative.Space.Rendering.Light;

import net.minecraft.world.phys.Vec3;

public record AuralithLight
(
    Vec3 position,
    Vec3 color,
    float intensity,
    float radius
)
{
}
