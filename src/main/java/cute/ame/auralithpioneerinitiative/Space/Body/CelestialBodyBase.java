package cute.ame.auralithpioneerinitiative.Space.Body;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;

public abstract class CelestialBodyBase
{
    protected static final double DISTANCE_THRESHOLD = 350.0;

    protected final Vec3 pos;
    protected final float size;

    protected Vec3 lightSourcePos = null;

    public CelestialBodyBase(Vec3 pos, float size)
    {
        this.pos = pos;
        this.size = size;
    }

    public abstract void render(PoseStack poseStack, Camera camera);

    public void setLightSourcePos(Vec3 lightSourcePos)
    {
        this.lightSourcePos = lightSourcePos;
    }

    public boolean emitLight()
    {
        return false;
    }

    public Vec3 getPos()
    {
        return pos;
    }
}