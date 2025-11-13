package cute.ame.auralithpioneerinitiative.Space.CelestialBody;

import net.minecraft.world.phys.Vec3;

public class CelestialSun extends CelestialBodyBase
{
    public CelestialSun(Vec3 pos, float size)
    {
        super(pos, size);
    }

    @Override
    public boolean emitLight() {
        return true;
    }
}
