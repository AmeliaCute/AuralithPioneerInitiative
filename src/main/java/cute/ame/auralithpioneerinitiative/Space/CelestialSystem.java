package cute.ame.auralithpioneerinitiative.Space;

import com.mojang.blaze3d.vertex.PoseStack;
import cute.ame.auralithpioneerinitiative.Space.CelestialBody.CelestialBodyBase;
import cute.ame.auralithpioneerinitiative.Space.CelestialBody.CelestialPlanet;
import cute.ame.auralithpioneerinitiative.Space.CelestialBody.CelestialSun;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class CelestialSystem
{
    private final List<CelestialPlanet> objectList = new ArrayList<>();
    private CelestialSun primarySun = null;

    public CelestialSystem()
    {
        primarySun = new CelestialSun(Vec3.ZERO, 80);

        objectList.add(
            new CelestialPlanet(new Vec3(1000, 0, 1000), 20)
        );

        updateLightSources();
    }

    public void updateLightSources()
    {
        if(primarySun == null) return;

        Vec3 sunPos = primarySun.getPos();
        for(CelestialPlanet body : objectList)
            if(!body.emitLight()) body.setLightSourcePos(sunPos);
    }

    public void render(PoseStack poseStack, Camera camera){
        primarySun.render(poseStack,camera);

        for(CelestialBodyBase body : objectList)
        {
            body.render(poseStack, camera);
        }
    }
}
