package cute.ame.auralithpioneerinitiative.Space.Body.Component.Impl;

import cute.ame.auralithpioneerinitiative.Space.Body.CelestialBodyBase;
import cute.ame.auralithpioneerinitiative.Space.Body.Component.CelestialComponent;
import cute.ame.auralithpioneerinitiative.Space.Body.Component.OrbitComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

/**
 * Component that synchronizes orbital position with Minecraft world time
 * Allows planets to match day/night cycles
 */
public class TimeSyncComponent implements CelestialComponent
{
    private OrbitComponent orbital;
    private long lastSyncTime = -1;

    /**
     * @param orbital The orbital component to synchronize
     */
    public TimeSyncComponent(OrbitComponent orbital)
    {
        this.orbital = orbital;
    }

    @Override
    public void tick(CelestialBodyBase body)
    {
        if (orbital == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        long currentTime = mc.level.getDayTime();
        if (lastSyncTime < 0)
        {
            lastSyncTime = currentTime;
            return;
        }

        long timeDelta = currentTime - lastSyncTime;
        if (Math.abs(timeDelta) > 100) syncToWorldTime(mc.level, body);

        lastSyncTime = currentTime;
    }

    /**
     * Synchronize orbital angle to match world time
     */
    private void syncToWorldTime(Level level, CelestialBodyBase body)
    {
        long dayTime = level.getDayTime() % 24000; // 0-24000 for one day
        double angleFromTime = (dayTime / 24000.0) * (2.0 * Math.PI);

        try
        {
            java.lang.reflect.Field angleField = OrbitComponent.class.getDeclaredField("currentAngle");
            angleField.setAccessible(true);
            angleField.set(orbital, angleFromTime);
        }
        catch (Exception e)
        {
            System.err.println("Failed to sync orbital time: " + e.getMessage());
        }
    }

    @Override
    public void render(CelestialBodyBase body, com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.Camera camera, double relX, double relY, double relZ)
    {
    }

    @Override
    public int getRenderPriority()
    {
        return -999;
    }

    @Override
    public void onAttach(CelestialBodyBase body)
    {
        lastSyncTime = -1;
    }
}