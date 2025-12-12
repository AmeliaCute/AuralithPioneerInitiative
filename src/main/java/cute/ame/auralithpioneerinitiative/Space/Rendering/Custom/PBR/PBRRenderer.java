package cute.ame.auralithpioneerinitiative.Space.Rendering.Custom.PBR;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import cute.ame.auralithpioneerinitiative.Space.Rendering.Custom.PostProcess.LightingPostProcessor;
import cute.ame.auralithpioneerinitiative.Space.Rendering.Geometry.AuralithMesh;
import cute.ame.auralithpioneerinitiative.Space.Rendering.Geometry.AuralithTriangle;
import net.minecraft.client.Camera;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class PBRRenderer
{
    private static PBRRenderer instance;
    private PBRShaderProgram shader;
    private boolean initialized = false;

    private PBRRenderer() {}

    public static PBRRenderer getInstance()
    {
        if (instance == null)
            instance = new PBRRenderer();
        return instance;
    }

    public void init()
    {
        if (initialized) return;

        try {
            shader = new PBRShaderProgram();
            shader.createVertexShader(PBRShaders.VERTEX_SHADER);
            shader.createFragmentShader(PBRShaders.FRAGMENT_SHADER);
            shader.link();
            initialized = true;
            System.out.println("PBR Shader with Dynamic Lighting initialized successfully!");
        } catch (IOException e) {
            System.err.println("Failed to initialize PBR shader:");
            e.printStackTrace();
        }
    }

    public void renderMesh(AuralithMesh mesh, PoseStack poseStack, Camera camera, Vector3f lightPos, Vector3f lightColor, Vector3f albedo, float metallic, float roughness, float ao, Vector3f planetCenter, List<Vector3f> shadowCasterPositions, List<Float> shadowCasterRadii)
    {
        if (!initialized)
            init();

        if (!initialized || shader == null)
            return;

        int vao = GL33.glGenVertexArrays();
        GL33.glBindVertexArray(vao);

        List<Float> vertices = new ArrayList<>();
        for (AuralithTriangle tri : mesh.triangles)
        {
            // v1
            vertices.add(tri.v1().position().x);
            vertices.add(tri.v1().position().y);
            vertices.add(tri.v1().position().z);
            vertices.add(tri.v1().normal().x);
            vertices.add(tri.v1().normal().y);
            vertices.add(tri.v1().normal().z);

            // v2
            vertices.add(tri.v2().position().x);
            vertices.add(tri.v2().position().y);
            vertices.add(tri.v2().position().z);
            vertices.add(tri.v2().normal().x);
            vertices.add(tri.v2().normal().y);
            vertices.add(tri.v2().normal().z);

            // v3
            vertices.add(tri.v3().position().x);
            vertices.add(tri.v3().position().y);
            vertices.add(tri.v3().position().z);
            vertices.add(tri.v3().normal().x);
            vertices.add(tri.v3().normal().y);
            vertices.add(tri.v3().normal().z);
        }

        float[] vertexArray = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); ++i)
            vertexArray[i] = vertices.get(i);

        FloatBuffer vertexBuffer = org.lwjgl.BufferUtils.createFloatBuffer(vertexArray.length);
        vertexBuffer.put(vertexArray);
        vertexBuffer.flip();

        int vbo = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, vbo);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, vertexBuffer, GL33.GL_STATIC_DRAW);

        GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 6 * Float.BYTES, 0);
        GL33.glEnableVertexAttribArray(0);

        GL33.glVertexAttribPointer(1, 3, GL33.GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        GL33.glEnableVertexAttribArray(1);

        Matrix4f modelMatrix = new Matrix4f(poseStack.last().pose());
        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.rotateX((float) Math.toRadians(camera.getXRot()));
        viewMatrix.rotateY((float) Math.toRadians(camera.getYRot() + 180.0f));
        Matrix4f projectionMatrix = new Matrix4f(RenderSystem.getProjectionMatrix());
        Matrix4f normalMatrix = new Matrix4f(modelMatrix).invert().transpose();
        Vector3f cameraPos = new Vector3f(
                (float)camera.getPosition().x,
                (float)camera.getPosition().y,
                (float)camera.getPosition().z
        );

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

        shader.setShadowCasters(shadowCasterPositions, shadowCasterRadii);

        LightingPostProcessor lightProcessor = LightingPostProcessor.getInstance();

        List<LightingPostProcessor.PointLight> pointLights = lightProcessor.getPointLights();
        if (!pointLights.isEmpty())
        {
            List<Vector3f> positions = new ArrayList<>();
            List<Vector3f> colors = new ArrayList<>();
            List<Float> brightness = new ArrayList<>();
            List<Float> range = new ArrayList<>();
            List<Float> linear = new ArrayList<>();
            List<Float> quadratic = new ArrayList<>();

            for (var light : pointLights)
            {
                positions.add(light.position);
                colors.add(light.color);
                brightness.add(light.brightness);
                range.add(light.range);
                linear.add(light.linear);
                quadratic.add(light.quadratic);
            }

            shader.setDynamicPointLights(positions, colors, brightness, range, linear, quadratic);
        } else
            shader.setDynamicPointLights(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        List<LightingPostProcessor.Spotlight> spotlights = lightProcessor.getSpotlights();
        if (!spotlights.isEmpty())
        {
            List<Vector3f> positions = new ArrayList<>();
            List<Vector3f> directions = new ArrayList<>();
            List<Vector3f> colors = new ArrayList<>();
            List<Float> brightness = new ArrayList<>();
            List<Float> range = new ArrayList<>();
            List<Float> innerCone = new ArrayList<>();
            List<Float> outerCone = new ArrayList<>();

            for (var light : spotlights)
            {
                positions.add(light.position);
                directions.add(light.direction);
                colors.add(light.color);
                brightness.add(light.brightness);
                range.add(light.range);
                innerCone.add(light.innerCone);
                outerCone.add(light.outerCone);
            }

            shader.setDynamicSpotlights(positions, directions, colors, brightness,
                    range, innerCone, outerCone);
        } else
            shader.setDynamicSpotlights(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, mesh.triangles.size() * 3);

        shader.unbind();
        GL33.glBindVertexArray(0);
        GL33.glDeleteBuffers(vbo);
        GL33.glDeleteVertexArrays(vao);
    }

    public void cleanup()
    {
        if (shader != null)
            shader.cleanup();
    }
}