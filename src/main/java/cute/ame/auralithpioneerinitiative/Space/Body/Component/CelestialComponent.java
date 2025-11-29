package cute.ame.auralithpioneerinitiative.Space.Body.Component;

import com.mojang.blaze3d.vertex.PoseStack;
import cute.ame.auralithpioneerinitiative.Space.Body.CelestialBodyBase;
import net.minecraft.client.Camera;

public interface CelestialComponent
{
    void render(CelestialBodyBase body, PoseStack poseStack, Camera camera, double relX, double relY, double relZ);
    default void tick(CelestialBodyBase body) {}

    default int getRenderPriority() {
        return 0;
    }

    default void onAttach(CelestialBodyBase body) {}
    default void onDetach(CelestialBodyBase body) {}
}
