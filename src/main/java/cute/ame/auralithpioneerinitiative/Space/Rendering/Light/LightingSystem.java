package cute.ame.auralithpioneerinitiative.Space.Rendering.Light;

import cute.ame.auralithpioneerinitiative.Space.Body.CelestialBodyBase;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class LightingSystem
{
    private final List<AuralithLight> lights = new ArrayList<>();
    private final List<CelestialBodyBase> shadowCasters = new ArrayList<>();

    public void addLight(AuralithLight light) {
        lights.add(light);
    }

    public void clearLights() {
        lights.clear();
    }

    public List<AuralithLight> getLights() {
        return lights;
    }

    public void addShadowCaster(CelestialBodyBase body) {
        shadowCasters.add(body);
    }

    public void clearShadowCasters() {
        shadowCasters.clear();
    }

    public Vec3 calculateLighting(Vec3 worldPos, Vector3f normal, Vec3 albedo, float roughness, float metallic)
    {
        Vec3 finalColor = Vec3.ZERO;

        for(AuralithLight light : lights)
        {
            Vec3 toLight = light.position().subtract(worldPos);
            double distance = toLight.length();

            if(distance > light.radius()) continue;

            Vec3 lightDir = toLight.normalize();

            if (isInShadow(worldPos, light, lightDir, distance)) {
                continue;
            }

            float NdotL = Math.max(
                    (float)(lightDir.x * normal.x + lightDir.y * normal.y + lightDir.z * normal.z),
                    0.0f
            );

            float attenuation = calculateAttenuation((float)distance, light.radius());
            Vec3 diffuse = light.color().scale(light.intensity() * NdotL * attenuation);

            float specular = calculateSpecular(worldPos, lightDir, normal, roughness);
            Vec3 specularColor = light.color().scale(specular * attenuation * (1.0f - roughness));

            Vec3 litColor = new Vec3(
                    albedo.x * diffuse.x + specularColor.x * metallic,
                    albedo.y * diffuse.y + specularColor.y * metallic,
                    albedo.z * diffuse.z + specularColor.z * metallic
            );

            finalColor = finalColor.add(litColor);
        }

        Vec3 ambient = albedo.scale(0.2);
        finalColor = finalColor.add(ambient);

        return finalColor;
    }

    public boolean isInShadow(Vec3 worldPos, AuralithLight light, Vec3 lightDir, double distanceToLight) {
        for (CelestialBodyBase caster : shadowCasters) {
            if (caster.emitLight()) {
                continue;
            }

            Vec3 casterPos = caster.getPos();
            Vec3 toSphere = casterPos.subtract(worldPos);
            double tca = toSphere.dot(lightDir);
            if (tca < 0) continue;

            double d2 = toSphere.lengthSqr() - tca * tca;
            double shadowRadius = getShadowRadius(caster);
            double radius2 = shadowRadius * shadowRadius;

            if (d2 > radius2) continue;
            double thc = Math.sqrt(radius2 - d2);
            double t0 = tca - thc;
            if (t0 > 0.01 && t0 < distanceToLight)
                return true;
        }

        return false;
    }

    private double getShadowRadius(CelestialBodyBase body) {
        return 100.0;
    }

    private float calculateAttenuation(float distance, float radius)
    {
        float ratio = distance / radius;
        return Math.max(1.0f / (1.0f + ratio + ratio * ratio), 0.0f);
    }

    private float calculateSpecular(Vec3 worldPos, Vec3 lightDir, Vector3f normal, float roughness)
    {
        Vec3 viewDir = worldPos.normalize().reverse();
        Vec3 halfDir = lightDir.add(viewDir).normalize();
        float NdotH = Math.max(
                (float)(halfDir.x * normal.x + halfDir.y * normal.y + halfDir.z * normal.z),
                0.0f
        );

        float shininess = (1.0f - roughness) * 128.0f + 1.0f;
        return (float) Math.pow(NdotH, shininess);
    }

    public static boolean raySphereIntersect(Vec3 rayOrigin, Vec3 rayDir, Vec3 sphereCenter, double sphereRadius, double maxDistance) {
        Vec3 oc = rayOrigin.subtract(sphereCenter);

        double a = rayDir.dot(rayDir);
        double b = 2.0 * oc.dot(rayDir);
        double c = oc.dot(oc) - sphereRadius * sphereRadius;

        double discriminant = b * b - 4 * a * c;

        if (discriminant < 0) {
            return false;
        }

        double t = (-b - Math.sqrt(discriminant)) / (2.0 * a);

        return t > 0.01 && t < maxDistance;
    }
}
