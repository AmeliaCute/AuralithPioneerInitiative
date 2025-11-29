package cute.ame.auralithpioneerinitiative.Space.Body;

import com.mojang.blaze3d.vertex.*;
import cute.ame.auralithpioneerinitiative.Space.Body.Component.CelestialComponent;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class CelestialBodyBase
{
    protected final Vec3 pos;
    protected final float size;

    protected Vec3 lightSourcePos = null;

    protected final List<CelestialComponent> components = new ArrayList<>();
    protected boolean componentsDirty = false;

    public CelestialBodyBase(Vec3 pos, float size)
    {
        this.pos = pos;
        this.size = size;
    }

    protected abstract void renderBody(PoseStack poseStack, Camera camera, double relX, double relY, double relZ);

    public final void render(PoseStack poseStack, Camera camera)
    {
        Vec3 cameraPos = camera.getPosition();
        double relX = this.pos.x - cameraPos.x;
        double relY = this.pos.y - cameraPos.y;
        double relZ = this.pos.z - cameraPos.z;
        poseStack.pushPose();
        poseStack.translate(relX, relY, relZ);

        if (componentsDirty)
        {
            components.sort(Comparator.comparingInt(CelestialComponent::getRenderPriority));
            componentsDirty = false;
        }

        for (CelestialComponent component : components)
        {
            component.render(this, poseStack, camera, relX, relY, relZ);
        }

        renderBody(poseStack, camera, relX, relY, relZ);

        poseStack.popPose();
    }

    public void tick()
    {
        for (CelestialComponent component : components) component.tick(this);
    }

    public void addComponent(CelestialComponent component)
    {
        if (!components.contains(component))
        {
            components.add(component);
            component.onAttach(this);
            componentsDirty = true;
        }
    }

    public void removeComponent(CelestialComponent component)
    {
        if (components.remove(component)) component.onDetach(this);
    }

    public <T extends CelestialComponent> T getComponent(Class<T> componentClass)
    {
        for (CelestialComponent component : components)
            if (componentClass.isInstance(component))
                return componentClass.cast(component);

        return null;
    }

    public <T extends CelestialComponent> List<T> getComponents(Class<T> componentClass)
    {
        List<T> result = new ArrayList<>();
        for (CelestialComponent component : components)
            if (componentClass.isInstance(component))
                result.add(componentClass.cast(component));

        return result;
    }

    public boolean hasComponent(Class<? extends CelestialComponent> componentClass) { return getComponent(componentClass) != null; }

    public void setLightSourcePos(Vec3 lightSourcePos) { this.lightSourcePos = lightSourcePos; }

    public boolean emitLight() { return false; }

    public Vec3 getPos() { return pos; }

    public float getSize() { return size; }

    public Vec3 getLightSourcePos() { return lightSourcePos; }
}