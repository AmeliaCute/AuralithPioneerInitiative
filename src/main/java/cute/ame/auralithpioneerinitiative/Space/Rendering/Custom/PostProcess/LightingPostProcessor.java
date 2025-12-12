package cute.ame.auralithpioneerinitiative.Space.Rendering.Custom.PostProcess;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

import java.util.ArrayList;
import java.util.List;

public class LightingPostProcessor
{
    private static LightingPostProcessor instance;

    public static class PointLight
    {
        public Vector3f position;
        public Vector3f color;
        public float brightness;
        public float range;
        public float linear;
        public float quadratic;

        public PointLight(Vector3f pos, Vector3f col, float bright, float rng, float lin, float quad)
        {
            this.position = new Vector3f(pos);
            this.color = new Vector3f(col);
            this.brightness = bright;
            this.range = rng;
            this.linear = lin;
            this.quadratic = quad;
        }
    }

    public static class Spotlight
    {
        public Vector3f position;
        public Vector3f direction;
        public Vector3f color;
        public float brightness;
        public float range;
        public float innerCone;
        public float outerCone;

        public Spotlight(Vector3f pos, Vector3f dir, Vector3f col, float bright, float rng, float inner, float outer)
        {
            this.position = new Vector3f(pos);
            this.direction = new Vector3f(dir).normalize();
            this.color = new Vector3f(col);
            this.brightness = bright;
            this.range = rng;
            this.innerCone = inner;
            this.outerCone = outer;
        }
    }

    public static class AerialLight
    {
        public Vector3f position;
        public Vector3f direction;
        public Vector3f color;
        public float brightness;
        public float range;
        public float innerCone;
        public float outerCone;
        public float volumetricIntensity;

        public AerialLight(Vector3f pos, Vector3f dir, Vector3f col, float bright, float rng, float inner, float outer, float volumetric)
        {
            this.position = new Vector3f(pos);
            this.direction = new Vector3f(dir).normalize();
            this.color = new Vector3f(col);
            this.brightness = bright;
            this.range = rng;
            this.innerCone = inner;
            this.outerCone = outer;
            this.volumetricIntensity = volumetric;
        }
    }

    private final List<PointLight> pointLights = new ArrayList<>();
    private final List<Spotlight> spotlights = new ArrayList<>();
    private final List<AerialLight> aerialLights = new ArrayList<>();

    private Matrix4f invProjectionMatrix;
    private Matrix4f invViewMatrix;

    private LightingPostProcessor() {}

    public static LightingPostProcessor getInstance()
    {
        if (instance == null) instance = new LightingPostProcessor();
        return instance;
    }

    public void clearLights()
    {
        pointLights.clear();
        spotlights.clear();
        aerialLights.clear();
    }

    public void addPointLight(Vector3f position, Vector3f color, float brightness, float range, float linear, float quadratic)
    {
        pointLights.add(new PointLight(position, color, brightness, range, linear, quadratic));
    }

    public void addSpotlight(Vector3f position, Vector3f direction, Vector3f color, float brightness, float range, float innerCone, float outerCone)
    {
        spotlights.add(new Spotlight(position, direction, color, brightness, range, innerCone, outerCone));
    }

    public void addAerialLight(Vector3f position, Vector3f direction, Vector3f color, float brightness, float range, float innerCone, float outerCone, float volumetric)
    {
        aerialLights.add(new AerialLight(position, direction, color, brightness, range, innerCone, outerCone, volumetric));
    }

    public void setMatrices(Matrix4f invProj, Matrix4f invView)
    {
        this.invProjectionMatrix = new Matrix4f(invProj);
        this.invViewMatrix = new Matrix4f(invView);
    }

    public void applyLighting(int shaderProgram)
    {
        int numPointLights = Math.min(pointLights.size(), 16);
        GL33.glUniform1i(GL33.glGetUniformLocation(shaderProgram, "uNumPointLights"), numPointLights);

        float[] positions = new float[numPointLights * 3];
        float[] colors = new float[numPointLights * 3];
        float[] params = new float[numPointLights * 4];

        for (int i = 0; i < numPointLights; i++)
        {
            PointLight light = pointLights.get(i);
            positions[i * 3] = light.position.x;
            positions[i * 3 + 1] = light.position.y;
            positions[i * 3 + 2] = light.position.z;

            colors[i * 3] = light.color.x;
            colors[i * 3 + 1] = light.color.y;
            colors[i * 3 + 2] = light.color.z;

            params[i * 4] = light.brightness;
            params[i * 4 + 1] = light.range;
            params[i * 4 + 2] = light.linear;
            params[i * 4 + 3] = light.quadratic;
        }

        GL33.glUniform3fv(GL33.glGetUniformLocation(shaderProgram, "uPointLightPositions"), positions);
        GL33.glUniform3fv(GL33.glGetUniformLocation(shaderProgram, "uPointLightColors"), colors);
        GL33.glUniform4fv(GL33.glGetUniformLocation(shaderProgram, "uPointLightParams"), params);

        int numSpotlights = Math.min(spotlights.size(), 16);
        GL33.glUniform1i(GL33.glGetUniformLocation(shaderProgram, "uNumSpotlights"), numSpotlights);

        float[] spotPositions = new float[numSpotlights * 3];
        float[] spotDirections = new float[numSpotlights * 3];
        float[] spotColors = new float[numSpotlights * 3];
        float[] spotParams = new float[numSpotlights * 4];

        for (int i = 0; i < numSpotlights; i++)
        {
            Spotlight light = spotlights.get(i);
            spotPositions[i * 3] = light.position.x;
            spotPositions[i * 3 + 1] = light.position.y;
            spotPositions[i * 3 + 2] = light.position.z;

            spotDirections[i * 3] = light.direction.x;
            spotDirections[i * 3 + 1] = light.direction.y;
            spotDirections[i * 3 + 2] = light.direction.z;

            spotColors[i * 3] = light.color.x;
            spotColors[i * 3 + 1] = light.color.y;
            spotColors[i * 3 + 2] = light.color.z;

            spotParams[i * 4] = light.brightness;
            spotParams[i * 4 + 1] = light.range;
            spotParams[i * 4 + 2] = (float) Math.cos(Math.toRadians(light.innerCone));
            spotParams[i * 4 + 3] = (float) Math.cos(Math.toRadians(light.outerCone));
        }

        GL33.glUniform3fv(GL33.glGetUniformLocation(shaderProgram, "uSpotlightPositions"), spotPositions);
        GL33.glUniform3fv(GL33.glGetUniformLocation(shaderProgram, "uSpotlightDirections"), spotDirections);
        GL33.glUniform3fv(GL33.glGetUniformLocation(shaderProgram, "uSpotlightColors"), spotColors);
        GL33.glUniform4fv(GL33.glGetUniformLocation(shaderProgram, "uSpotlightParams"), spotParams);

        if (invProjectionMatrix != null)
        {
            float[] matBuffer = new float[16];
            invProjectionMatrix.get(matBuffer);
            GL33.glUniformMatrix4fv(GL33.glGetUniformLocation(shaderProgram, "uInvProjection"), false, matBuffer);
        }

        if (invViewMatrix != null)
        {
            float[] matBuffer = new float[16];
            invViewMatrix.get(matBuffer);
            GL33.glUniformMatrix4fv(GL33.glGetUniformLocation(shaderProgram, "uInvView"), false, matBuffer);
        }
    }

    public List<PointLight> getPointLights() {
        return new ArrayList<>(pointLights);
    }

    public List<Spotlight> getSpotlights() {
        return new ArrayList<>(spotlights);
    }

    public List<AerialLight> getAerialLights() {
        return new ArrayList<>(aerialLights);
    }
}