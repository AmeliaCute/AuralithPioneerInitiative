package cute.ame.auralithpioneerinitiative.Space.Rendering.Custom.PBR;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import cute.ame.auralithpioneerinitiative.Space.Rendering.Custom.PostProcess.LightingPostProcessor;
import cute.ame.auralithpioneerinitiative.Space.Rendering.Geometry.AuralithMesh;
import net.minecraft.client.Camera;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PBRRenderer
{

    private static PBRRenderer instance;
    private PBRShaderProgram shader;
    private boolean initialized = false;

    private final List<Vector3f> tempPositions = new ArrayList<>();
    private final List<Vector3f> tempColors = new ArrayList<>();
    private final List<Float> tempFloats = new ArrayList<>();
    private final List<Vector3f> tempDirections = new ArrayList<>();

    private PBRRenderer() {}

    public static PBRRenderer getInstance()
    {
        if (instance == null) instance = new PBRRenderer();
        return instance;
    }

    public void init()
    {
        if (initialized) return;

        try
        {
            shader = new PBRShaderProgram();
            shader.createVertexShader(PBRShaders.VERTEX_SHADER);
            shader.createFragmentShader(PBRShaders.FRAGMENT_SHADER);
            shader.link();
            initialized = true;
            System.out.println("[PBRRenderer] Shader initialized successfully");
        } catch (IOException e)
        {
            System.err.println("[PBRRenderer] Failed to initialize shader:");
            e.printStackTrace();
        }
    }

    public void renderMesh(AuralithMesh mesh, PoseStack poseStack, Camera camera, Vector3f lightPos, Vector3f lightColor, Vector3f albedo, float metallic, float roughness, float ao, Vector3f planetCenter, List<Vector3f> shadowCasterPositions, List<Float> shadowCasterRadii)
    {
        if (!initialized) init();

        if (!initialized || shader == null)
        {
            System.err.println("[PBRRenderer] Shader not available for rendering");
            return;
        }

        if (mesh == null || !mesh.isReadyToRender())
        {
            System.err.println("[PBRRenderer] Mesh not ready for rendering");
            return;
        }

        Matrix4f modelMatrix = new Matrix4f(poseStack.last().pose());
        Matrix4f viewMatrix = createViewMatrix(camera);
        Matrix4f projectionMatrix = new Matrix4f(RenderSystem.getProjectionMatrix());
        Matrix4f normalMatrix = new Matrix4f(modelMatrix).invert().transpose();
        Vector3f cameraPos = getCameraPosition(camera);

        shader.bind();

        shader.setModelMatrix(modelMatrix);
        shader.setViewMatrix(viewMatrix);
        shader.setProjectionMatrix(projectionMatrix);
        shader.setNormalMatrix(normalMatrix);
        shader.setCameraPos(cameraPos);

        shader.setLightPos(lightPos);
        shader.setLightColor(lightColor);

        shader.setAlbedo(albedo);
        shader.setMetallic(metallic);
        shader.setRoughness(roughness);
        shader.setAO(ao);
        shader.setPlanetCenter(planetCenter);

        shader.setShadowCasters(shadowCasterPositions != null ? shadowCasterPositions : Collections.emptyList(), shadowCasterRadii != null ? shadowCasterRadii : Collections.emptyList());

        setDynamicLights();
        mesh.render();

        shader.unbind();
    }

    private void setDynamicLights()
    {
        LightingPostProcessor lightProcessor = LightingPostProcessor.getInstance();

        List<LightingPostProcessor.PointLight> pointLights = lightProcessor.getPointLights();
        if (!pointLights.isEmpty())
        {
            clearTempLists();

            for (var light : pointLights)
            {
                tempPositions.add(light.position);
                tempColors.add(light.color);
                tempFloats.add(light.brightness);
            }

            List<Float> range = new ArrayList<>(pointLights.size());
            List<Float> linear = new ArrayList<>(pointLights.size());
            List<Float> quadratic = new ArrayList<>(pointLights.size());

            for (var light : pointLights)
            {
                range.add(light.range);
                linear.add(light.linear);
                quadratic.add(light.quadratic);
            }

            shader.setDynamicPointLights(
                    new ArrayList<>(tempPositions),
                    new ArrayList<>(tempColors),
                    new ArrayList<>(tempFloats),
                    range, linear, quadratic
            );
        } else {
            shader.setDynamicPointLights(
                    Collections.emptyList(), Collections.emptyList(),
                    Collections.emptyList(), Collections.emptyList(),
                    Collections.emptyList(), Collections.emptyList()
            );
        }

        List<LightingPostProcessor.Spotlight> spotlights = lightProcessor.getSpotlights();
        if (!spotlights.isEmpty())
        {
            clearTempLists();

            for (var light : spotlights)
            {
                tempPositions.add(light.position);
                tempDirections.add(light.direction);
                tempColors.add(light.color);
                tempFloats.add(light.brightness);
            }

            List<Float> range = new ArrayList<>(spotlights.size());
            List<Float> innerCone = new ArrayList<>(spotlights.size());
            List<Float> outerCone = new ArrayList<>(spotlights.size());

            for (var light : spotlights)
            {
                range.add(light.range);
                innerCone.add(light.innerCone);
                outerCone.add(light.outerCone);
            }

            shader.setDynamicSpotlights(new ArrayList<>(tempPositions), new ArrayList<>(tempDirections), new ArrayList<>(tempColors), new ArrayList<>(tempFloats), range, innerCone, outerCone);
        } else
            shader.setDynamicSpotlights(Collections.emptyList(), Collections.emptyList(),Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),Collections.emptyList());
    }

    private void clearTempLists()
    {
        tempPositions.clear();
        tempColors.clear();
        tempFloats.clear();
        tempDirections.clear();
    }

    private Matrix4f createViewMatrix(Camera camera)
    {
        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.rotateX((float) Math.toRadians(camera.getXRot()));
        viewMatrix.rotateY((float) Math.toRadians(camera.getYRot() + 180.0f));
        return viewMatrix;
    }

    private Vector3f getCameraPosition(Camera camera)
    {
        return new Vector3f((float) camera.getPosition().x,(float) camera.getPosition().y,(float) camera.getPosition().z);
    }

    public void cleanup()
    {
        if (shader != null) {
            shader.cleanup();
            shader = null;
        }
        initialized = false;
        System.out.println("[PBRRenderer] Cleaned up resources");
    }

    public boolean isInitialized()
    {
        return initialized;
    }
}