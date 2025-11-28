package cute.ame.auralithpioneerinitiative.Space.Rendering.Custom.PBR;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL20;

import java.io.IOException;

public class PBRShaderProgram
{
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

    public PBRShaderProgram() throws IOException
    {
        programId = GL20.glCreateProgram();
        if (programId == 0)
            throw new IOException("Could not create shader program");
    }

    public void createVertexShader(String shaderCode) throws IOException
    {
        vertexShaderId = createShader(shaderCode, GL20.GL_VERTEX_SHADER);
    }

    public void createFragmentShader(String shaderCode) throws IOException
    {
        fragmentShaderId = createShader(shaderCode, GL20.GL_FRAGMENT_SHADER);
    }

    private int createShader(String shaderCode, int shaderType) throws IOException
    {
        int shaderId = GL20.glCreateShader(shaderType);
        if (shaderId == 0)
            throw new IOException("Error creating shader. Type: " + shaderType);

        GL20.glShaderSource(shaderId, shaderCode);
        GL20.glCompileShader(shaderId);

        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == 0)
            throw new IOException("Error compiling Shader code: " + GL20.glGetShaderInfoLog(shaderId, 1024));

        GL20.glAttachShader(programId, shaderId);
        return shaderId;
    }

    public void link() throws IOException
    {
        GL20.glLinkProgram(programId);
        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0)
            throw new IOException("Error linking Shader code: " + GL20.glGetProgramInfoLog(programId, 1024));

        if (vertexShaderId != 0)
            GL20.glDetachShader(programId, vertexShaderId);
        if (fragmentShaderId != 0)
            GL20.glDetachShader(programId, fragmentShaderId);

        GL20.glValidateProgram(programId);
        if (GL20.glGetProgrami(programId, GL20.GL_VALIDATE_STATUS) == 0)
            System.err.println("Warning validating Shader code: " + GL20.glGetProgramInfoLog(programId, 1024));

        uModelMatrix = GL20.glGetUniformLocation(programId, "uModel");
        uViewMatrix = GL20.glGetUniformLocation(programId, "uView");
        uProjectionMatrix = GL20.glGetUniformLocation(programId, "uProjection");
        uNormalMatrix = GL20.glGetUniformLocation(programId, "uNormalMatrix");
        uCameraPos = GL20.glGetUniformLocation(programId, "uCameraPos");
        uLightPos = GL20.glGetUniformLocation(programId, "uLightPos");
        uLightColor = GL20.glGetUniformLocation(programId, "uLightColor");
        uAlbedo = GL20.glGetUniformLocation(programId, "uAlbedo");
        uMetallic = GL20.glGetUniformLocation(programId, "uMetallic");
        uRoughness = GL20.glGetUniformLocation(programId, "uRoughness");
        uAO = GL20.glGetUniformLocation(programId, "uAO");
        uPlanetCenter = GL20.glGetUniformLocation(programId, "uPlanetCenter");
    }

    public void bind() 
    {
        GL20.glUseProgram(programId);
    }

    public void unbind() 
    {
        GL20.glUseProgram(0);
    }

    public void setModelMatrix(Matrix4f matrix) 
    {
        float[] buffer = new float[16];
        matrix.get(buffer);
        GL20.glUniformMatrix4fv(uModelMatrix, false, buffer);
    }

    public void setViewMatrix(Matrix4f matrix)
{
        float[] buffer = new float[16];
        matrix.get(buffer);
        GL20.glUniformMatrix4fv(uViewMatrix, false, buffer);
    }

    public void setProjectionMatrix(Matrix4f matrix)
    {
        float[] buffer = new float[16];
        matrix.get(buffer);
        GL20.glUniformMatrix4fv(uProjectionMatrix, false, buffer);
    }

    public void setNormalMatrix(Matrix4f matrix)
    {
        float[] buffer = new float[16];
        matrix.get(buffer);
        GL20.glUniformMatrix4fv(uNormalMatrix, false, buffer);
    }

    public void setCameraPos(Vector3f pos)
    {
        GL20.glUniform3f(uCameraPos, pos.x, pos.y, pos.z);
    }

    public void setLightPos(Vector3f pos)
    {
        GL20.glUniform3f(uLightPos, pos.x, pos.y, pos.z);
    }

    public void setLightColor(Vector3f color)
    {
        GL20.glUniform3f(uLightColor, color.x, color.y, color.z);
    }

    public void setAlbedo(Vector3f albedo)
    {
        GL20.glUniform3f(uAlbedo, albedo.x, albedo.y, albedo.z);
    }

    public void setMetallic(float metallic)
    {
        GL20.glUniform1f(uMetallic, metallic);
    }

    public void setRoughness(float roughness)
    {
        GL20.glUniform1f(uRoughness, roughness);
    }

    public void setAO(float ao)
    {
        GL20.glUniform1f(uAO, ao);
    }

    public void setPlanetCenter(Vector3f center)
    {
        GL20.glUniform3f(uPlanetCenter, center.x, center.y, center.z);
    }

    public void cleanup()
    {
        unbind();
        if (programId != 0)
            GL20.glDeleteProgram(programId);
    }
}