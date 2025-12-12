package cute.ame.auralithpioneerinitiative.Space.Rendering.Custom.BlackHole;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

import java.io.IOException;

public class BlackHoleShaderProgram
{
    private int programId;
    private int vertexShaderId;
    private int fragmentShaderId;

    private int uFov;
    private int uSize;
    private int uSpeed;
    private int uScale;
    private int uIntensity;
    private int uSteps;
    private int uInvProjMatrix;
    private int uInvViewMatrix;
    private int uRotationMatrix;
    private int uFullMatN;
    private int uFullMatP;
    private int uCameraPosition;
    private int uBlackholePosition;
    private int uColor;
    private int uResolution;
    private int uGameTime;
    private int uRenderDistance;
    private int uDiffuseSampler;
    private int uDepthSampler;
    private int uOutSize;

    public BlackHoleShaderProgram() throws IOException
    {
        programId = GL33.glCreateProgram();
        if (programId == 0) {
            throw new IOException("Could not create shader program");
        }
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
        if (shaderId == 0) throw new IOException("Error creating shader. Type: " + shaderType);

        GL33.glShaderSource(shaderId, shaderCode);
        GL33.glCompileShader(shaderId);

        if (GL33.glGetShaderi(shaderId, GL33.GL_COMPILE_STATUS) == 0) throw new IOException("Error compiling Shader code: " + GL33.glGetShaderInfoLog(shaderId, 1024));

        GL33.glAttachShader(programId, shaderId);
        return shaderId;
    }

    public void link() throws IOException
    {
        GL33.glLinkProgram(programId);
        if (GL33.glGetProgrami(programId, GL33.GL_LINK_STATUS) == 0) throw new IOException("Error linking Shader code: " + GL33.glGetProgramInfoLog(programId, 1024));


        if (vertexShaderId != 0) GL33.glDetachShader(programId, vertexShaderId);
        if (fragmentShaderId != 0) GL33.glDetachShader(programId, fragmentShaderId);

        GL33.glValidateProgram(programId);
        if (GL33.glGetProgrami(programId, GL33.GL_VALIDATE_STATUS) == 0) System.err.println("Warning validating Shader code: " + GL33.glGetProgramInfoLog(programId, 1024));


        uFov = GL33.glGetUniformLocation(programId, "Fov");
        uSize = GL33.glGetUniformLocation(programId, "_Size");
        uSpeed = GL33.glGetUniformLocation(programId, "_Speed");
        uScale = GL33.glGetUniformLocation(programId, "Scale");
        uIntensity = GL33.glGetUniformLocation(programId, "Intensity");
        uSteps = GL33.glGetUniformLocation(programId, "_Steps");
        uInvProjMatrix = GL33.glGetUniformLocation(programId, "InvProjMatrix");
        uInvViewMatrix = GL33.glGetUniformLocation(programId, "InvViewModleMatrix");
        uRotationMatrix = GL33.glGetUniformLocation(programId, "RotationMatrix");
        uFullMatN = GL33.glGetUniformLocation(programId, "FullMatN");
        uFullMatP = GL33.glGetUniformLocation(programId, "FullMatP");
        uCameraPosition = GL33.glGetUniformLocation(programId, "CameraPosition");
        uBlackholePosition = GL33.glGetUniformLocation(programId, "BlackholePosition");
        uColor = GL33.glGetUniformLocation(programId, "Color");
        uResolution = GL33.glGetUniformLocation(programId, "Resolution");
        uGameTime = GL33.glGetUniformLocation(programId, "GameTime");
        uRenderDistance = GL33.glGetUniformLocation(programId, "RenderDistance");
        uDiffuseSampler = GL33.glGetUniformLocation(programId, "DiffuseSampler");
        uDepthSampler = GL33.glGetUniformLocation(programId, "depth");
        uOutSize = GL33.glGetUniformLocation(programId, "OutSize");
    }

    public void bind()
    {
        GL33.glUseProgram(programId);
    }

    public void unbind()
    {
        GL33.glUseProgram(0);
    }

    public void setFov(float fov)
    {
        GL33.glUniform1f(uFov, fov);
    }

    public void setSize(float size)
    {
        GL33.glUniform1f(uSize, size);
    }

    public void setSpeed(float speed)
    {
        GL33.glUniform1f(uSpeed, speed);
    }

    public void setScale(float scale)
    {
        GL33.glUniform1f(uScale, scale);
    }

    public void setIntensity(float intensity)
    {
        GL33.glUniform1f(uIntensity, intensity);
    }

    public void setSteps(float steps)
    {
        GL33.glUniform1f(uSteps, steps);
    }

    public void setInvProjMatrix(Matrix4f matrix)
    {
        float[] buffer = new float[16];
        matrix.get(buffer);
        GL33.glUniformMatrix4fv(uInvProjMatrix, false, buffer);
    }

    public void setInvViewMatrix(Matrix4f matrix)
    {
        float[] buffer = new float[16];
        matrix.get(buffer);
        GL33.glUniformMatrix4fv(uInvViewMatrix, false, buffer);
    }

    public void setRotationMatrix(Matrix4f matrix)
    {
        float[] buffer = new float[16];
        matrix.get(buffer);
        GL33.glUniformMatrix4fv(uRotationMatrix, false, buffer);
    }

    public void setFullMatN(Matrix4f matrix)
    {
        float[] buffer = new float[16];
        matrix.get(buffer);
        GL33.glUniformMatrix4fv(uFullMatN, false, buffer);
    }

    public void setFullMatP(Matrix4f matrix)
    {
        float[] buffer = new float[16];
        matrix.get(buffer);
        GL33.glUniformMatrix4fv(uFullMatP, false, buffer);
    }

    public void setCameraPosition(Vector3f pos)
    {
        GL33.glUniform3f(uCameraPosition, pos.x, pos.y, pos.z);
    }

    public void setBlackholePosition(Vector3f pos)
    {
        GL33.glUniform3f(uBlackholePosition, pos.x, pos.y, pos.z);
    }

    public void setColor(Vector3f color)
    {
        GL33.glUniform3f(uColor, color.x, color.y, color.z);
    }

    public void setResolution(float width, float height)
    {
        GL33.glUniform2f(uResolution, width, height);
    }

    public void setGameTime(float time)
    {
        GL33.glUniform1f(uGameTime, time);
    }

    public void setRenderDistance(float distance)
    {
        GL33.glUniform1f(uRenderDistance, distance);
    }

    public void setDiffuseSampler(int textureUnit)
    {
        GL33.glUniform1i(uDiffuseSampler, textureUnit);
    }

    public void setDepthSampler(int textureUnit)
    {
        GL33.glUniform1i(uDepthSampler, textureUnit);
    }

    public void setOutSize(float width, float height)
    {
        GL33.glUniform2f(uOutSize, width, height);
    }

    public void cleanup()
    {
        unbind();
        if (programId != 0) GL33.glDeleteProgram(programId);
    }
}