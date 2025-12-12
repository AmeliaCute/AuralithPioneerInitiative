package cute.ame.auralithpioneerinitiative.Space.Rendering.Custom.PBR;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

import java.io.IOException;
import java.util.List;

public class PBRShaderProgram {
    private int programId;
    private int vertexShaderId;
    private int fragmentShaderId;

    private int uModelMatrix;
    private int uViewMatrix;
    private int uProjectionMatrix;
    private int uNormalMatrix;
    private int uCameraPos;
    private int uLightPos;
    private int uLightColor;
    private int uAlbedo;
    private int uMetallic;
    private int uRoughness;
    private int uAO;
    private int uPlanetCenter;
    private int uNumShadowCasters;
    private int uShadowCasterPositions;
    private int uShadowCasterRadii;

    private int uNumDynamicPointLights;
    private int uDynamicPointLightPositions;
    private int uDynamicPointLightColors;
    private int uDynamicPointLightParams;

    private int uNumDynamicSpotlights;
    private int uDynamicSpotlightPositions;
    private int uDynamicSpotlightDirections;
    private int uDynamicSpotlightColors;
    private int uDynamicSpotlightParams;

    public PBRShaderProgram() throws IOException
    {
        programId = GL33.glCreateProgram();
        if (programId == 0)
            throw new IOException("Could not create shader program");
    }

    public void createVertexShader(String shaderCode) throws IOException
    {
        vertexShaderId = createShader(shaderCode, GL33.GL_VERTEX_SHADER);
    }

    public void createFragmentShader(String shaderCode) throws IOException
    {
        fragmentShaderId = createShader(shaderCode, GL33.GL_FRAGMENT_SHADER);
    }

    private int createShader(String shaderCode, int shaderType) throws IOException
    {
        int shaderId = GL33.glCreateShader(shaderType);
        if (shaderId == 0)
            throw new IOException("Error creating shader. Type: " + shaderType);

        GL33.glShaderSource(shaderId, shaderCode);
        GL33.glCompileShader(shaderId);

        if (GL33.glGetShaderi(shaderId, GL33.GL_COMPILE_STATUS) == 0)
            throw new IOException("Error compiling Shader code: " + GL33.glGetShaderInfoLog(shaderId, 1024));

        GL33.glAttachShader(programId, shaderId);
        return shaderId;
    }

    public void link() throws IOException {
        GL33.glLinkProgram(programId);
        if (GL33.glGetProgrami(programId, GL33.GL_LINK_STATUS) == 0)
            throw new IOException("Error linking Shader code: " + GL33.glGetProgramInfoLog(programId, 1024));

        if (vertexShaderId != 0)
            GL33.glDetachShader(programId, vertexShaderId);
        if (fragmentShaderId != 0)
            GL33.glDetachShader(programId, fragmentShaderId);

        GL33.glValidateProgram(programId);
        if (GL33.glGetProgrami(programId, GL33.GL_VALIDATE_STATUS) == 0)
            System.err.println("Warning validating Shader code: " + GL33.glGetProgramInfoLog(programId, 1024));

        // Get all uniform locations
        uModelMatrix = GL33.glGetUniformLocation(programId, "uModel");
        uViewMatrix = GL33.glGetUniformLocation(programId, "uView");
        uProjectionMatrix = GL33.glGetUniformLocation(programId, "uProjection");
        uNormalMatrix = GL33.glGetUniformLocation(programId, "uNormalMatrix");
        uCameraPos = GL33.glGetUniformLocation(programId, "uCameraPos");
        uLightPos = GL33.glGetUniformLocation(programId, "uLightPos");
        uLightColor = GL33.glGetUniformLocation(programId, "uLightColor");
        uAlbedo = GL33.glGetUniformLocation(programId, "uAlbedo");
        uMetallic = GL33.glGetUniformLocation(programId, "uMetallic");
        uRoughness = GL33.glGetUniformLocation(programId, "uRoughness");
        uAO = GL33.glGetUniformLocation(programId, "uAO");
        uPlanetCenter = GL33.glGetUniformLocation(programId, "uPlanetCenter");
        uNumShadowCasters = GL33.glGetUniformLocation(programId, "uNumShadowCasters");
        uShadowCasterPositions = GL33.glGetUniformLocation(programId, "uShadowCasterPositions");
        uShadowCasterRadii = GL33.glGetUniformLocation(programId, "uShadowCasterRadii");

        // NEW: Get dynamic light uniform locations
        uNumDynamicPointLights = GL33.glGetUniformLocation(programId, "uNumDynamicPointLights");
        uDynamicPointLightPositions = GL33.glGetUniformLocation(programId, "uDynamicPointLightPositions");
        uDynamicPointLightColors = GL33.glGetUniformLocation(programId, "uDynamicPointLightColors");
        uDynamicPointLightParams = GL33.glGetUniformLocation(programId, "uDynamicPointLightParams");

        uNumDynamicSpotlights = GL33.glGetUniformLocation(programId, "uNumDynamicSpotlights");
        uDynamicSpotlightPositions = GL33.glGetUniformLocation(programId, "uDynamicSpotlightPositions");
        uDynamicSpotlightDirections = GL33.glGetUniformLocation(programId, "uDynamicSpotlightDirections");
        uDynamicSpotlightColors = GL33.glGetUniformLocation(programId, "uDynamicSpotlightColors");
        uDynamicSpotlightParams = GL33.glGetUniformLocation(programId, "uDynamicSpotlightParams");
    }

    public void bind() {
        GL33.glUseProgram(programId);
    }

    public void unbind() {
        GL33.glUseProgram(0);
    }

    public void setModelMatrix(Matrix4f matrix) {
        float[] buffer = new float[16];
        matrix.get(buffer);
        GL33.glUniformMatrix4fv(uModelMatrix, false, buffer);
    }

    public void setViewMatrix(Matrix4f matrix) {
        float[] buffer = new float[16];
        matrix.get(buffer);
        GL33.glUniformMatrix4fv(uViewMatrix, false, buffer);
    }

    public void setProjectionMatrix(Matrix4f matrix) {
        float[] buffer = new float[16];
        matrix.get(buffer);
        GL33.glUniformMatrix4fv(uProjectionMatrix, false, buffer);
    }

    public void setNormalMatrix(Matrix4f matrix) {
        float[] buffer = new float[16];
        matrix.get(buffer);
        GL33.glUniformMatrix4fv(uNormalMatrix, false, buffer);
    }

    public void setCameraPos(Vector3f pos) {
        GL33.glUniform3f(uCameraPos, pos.x, pos.y, pos.z);
    }

    public void setLightPos(Vector3f pos) {
        GL33.glUniform3f(uLightPos, pos.x, pos.y, pos.z);
    }

    public void setLightColor(Vector3f color) {
        GL33.glUniform3f(uLightColor, color.x, color.y, color.z);
    }

    public void setAlbedo(Vector3f albedo) {
        GL33.glUniform3f(uAlbedo, albedo.x, albedo.y, albedo.z);
    }

    public void setMetallic(float metallic) {
        GL33.glUniform1f(uMetallic, metallic);
    }

    public void setRoughness(float roughness) {
        GL33.glUniform1f(uRoughness, roughness);
    }

    public void setAO(float ao) {
        GL33.glUniform1f(uAO, ao);
    }

    public void setPlanetCenter(Vector3f center) {
        GL33.glUniform3f(uPlanetCenter, center.x, center.y, center.z);
    }

    public void setShadowCasters(List<Vector3f> positions, List<Float> radii) {
        int count = Math.min(positions.size(), 10);
        GL33.glUniform1i(uNumShadowCasters, count);

        float[] posArray = new float[count * 3];
        float[] radiiArray = new float[count];

        for (int i = 0; i < count; i++) {
            Vector3f pos = positions.get(i);
            posArray[i * 3] = pos.x;
            posArray[i * 3 + 1] = pos.y;
            posArray[i * 3 + 2] = pos.z;
            radiiArray[i] = radii.get(i);
        }

        GL33.glUniform3fv(uShadowCasterPositions, posArray);
        GL33.glUniform1fv(uShadowCasterRadii, radiiArray);
    }

    // NEW: Set dynamic point lights
    public void setDynamicPointLights(List<Vector3f> positions, List<Vector3f> colors,
                                      List<Float> brightness, List<Float> range,
                                      List<Float> linear, List<Float> quadratic) {
        int count = Math.min(positions.size(), 16);
        GL33.glUniform1i(uNumDynamicPointLights, count);

        if (count == 0) return;

        float[] posArray = new float[count * 3];
        float[] colorArray = new float[count * 3];
        float[] paramArray = new float[count * 4];

        for (int i = 0; i < count; i++) {
            Vector3f pos = positions.get(i);
            posArray[i * 3] = pos.x;
            posArray[i * 3 + 1] = pos.y;
            posArray[i * 3 + 2] = pos.z;

            Vector3f col = colors.get(i);
            colorArray[i * 3] = col.x;
            colorArray[i * 3 + 1] = col.y;
            colorArray[i * 3 + 2] = col.z;

            paramArray[i * 4] = brightness.get(i);
            paramArray[i * 4 + 1] = range.get(i);
            paramArray[i * 4 + 2] = linear.get(i);
            paramArray[i * 4 + 3] = quadratic.get(i);
        }

        GL33.glUniform3fv(uDynamicPointLightPositions, posArray);
        GL33.glUniform3fv(uDynamicPointLightColors, colorArray);
        GL33.glUniform4fv(uDynamicPointLightParams, paramArray);
    }

    // NEW: Set dynamic spotlights
    public void setDynamicSpotlights(List<Vector3f> positions, List<Vector3f> directions,
                                     List<Vector3f> colors, List<Float> brightness,
                                     List<Float> range, List<Float> innerCone,
                                     List<Float> outerCone) {
        int count = Math.min(positions.size(), 16);
        GL33.glUniform1i(uNumDynamicSpotlights, count);

        if (count == 0) return;

        float[] posArray = new float[count * 3];
        float[] dirArray = new float[count * 3];
        float[] colorArray = new float[count * 3];
        float[] paramArray = new float[count * 4];

        for (int i = 0; i < count; i++) {
            Vector3f pos = positions.get(i);
            posArray[i * 3] = pos.x;
            posArray[i * 3 + 1] = pos.y;
            posArray[i * 3 + 2] = pos.z;

            Vector3f dir = directions.get(i);
            dirArray[i * 3] = dir.x;
            dirArray[i * 3 + 1] = dir.y;
            dirArray[i * 3 + 2] = dir.z;

            Vector3f col = colors.get(i);
            colorArray[i * 3] = col.x;
            colorArray[i * 3 + 1] = col.y;
            colorArray[i * 3 + 2] = col.z;

            paramArray[i * 4] = brightness.get(i);
            paramArray[i * 4 + 1] = range.get(i);
            paramArray[i * 4 + 2] = (float) Math.cos(Math.toRadians(innerCone.get(i)));
            paramArray[i * 4 + 3] = (float) Math.cos(Math.toRadians(outerCone.get(i)));
        }

        GL33.glUniform3fv(uDynamicSpotlightPositions, posArray);
        GL33.glUniform3fv(uDynamicSpotlightDirections, dirArray);
        GL33.glUniform3fv(uDynamicSpotlightColors, colorArray);
        GL33.glUniform4fv(uDynamicSpotlightParams, paramArray);
    }

    public void cleanup() {
        unbind();
        if (programId != 0)
            GL33.glDeleteProgram(programId);
    }
}