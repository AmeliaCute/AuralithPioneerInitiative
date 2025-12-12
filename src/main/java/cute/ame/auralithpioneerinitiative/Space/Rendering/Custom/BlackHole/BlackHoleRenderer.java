package cute.ame.auralithpioneerinitiative.Space.Rendering.Custom.BlackHole;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

import java.io.IOException;

public class BlackHoleRenderer
{

    private static BlackHoleRenderer instance;
    private BlackHoleShaderProgram shader;
    private boolean initialized = false;

    private int quadVAO = 0;
    private int quadVBO = 0;

    private BlackHoleRenderer() {}

    public static BlackHoleRenderer getInstance()
    {
        if (instance == null) instance = new BlackHoleRenderer();
        return instance;
    }

    public void init()
    {
        if (initialized) return;

        try
        {
            shader = new BlackHoleShaderProgram();
            shader.createVertexShader(BlackHoleShaders.VERTEX_SHADER);
            shader.createFragmentShader(BlackHoleShaders.FRAGMENT_SHADER);
            shader.link();

            createFullscreenQuad();

            initialized = true;
            System.out.println("[BlackHoleRenderer] Shader initialized successfully");
        } catch (IOException e)
        {
            System.err.println("[BlackHoleRenderer] Failed to initialize shader:");
            e.printStackTrace();
        }
    }

    private void createFullscreenQuad()
    {
        float[] quadVertices =
        {
                -1.0f,  1.0f,  0.0f, 1.0f,
                -1.0f, -1.0f,  0.0f, 0.0f,
                1.0f, -1.0f,  1.0f, 0.0f,

                -1.0f,  1.0f,  0.0f, 1.0f,
                1.0f, -1.0f,  1.0f, 0.0f,
                1.0f,  1.0f,  1.0f, 1.0f
        };

        quadVAO = GL33.glGenVertexArrays();
        quadVBO = GL33.glGenBuffers();

        GL33.glBindVertexArray(quadVAO);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, quadVBO);

        java.nio.FloatBuffer buffer = org.lwjgl.BufferUtils.createFloatBuffer(quadVertices.length);
        buffer.put(quadVertices).flip();
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, buffer, GL33.GL_STATIC_DRAW);

        GL33.glVertexAttribPointer(0, 2, GL33.GL_FLOAT, false, 4 * Float.BYTES, 0);
        GL33.glEnableVertexAttribArray(0);

        GL33.glVertexAttribPointer(1, 2, GL33.GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        GL33.glEnableVertexAttribArray(1);

        GL33.glBindVertexArray(0);
    }

    public void render(PoseStack poseStack, Camera camera, Vec3 blackholePos, Vec3 cameraPos, float size, Vector3f color, float speed, float intensity, float steps, float scale, float renderDistance)
    {
        if (!initialized)
        {
            System.err.println("[BlackHoleRenderer] Renderer not initialized!");
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        RenderTarget mainTarget = mc.getMainRenderTarget();

        shader.bind();

        float fov = (float) Math.toRadians(mc.options.fov().get());
        shader.setFov(fov);
        shader.setSize(size);
        shader.setSpeed(speed);
        shader.setScale(scale);
        shader.setIntensity(intensity);
        shader.setSteps(steps);

        Matrix4f projMatrix = new Matrix4f(RenderSystem.getProjectionMatrix());
        Matrix4f invProjMatrix = new Matrix4f(projMatrix).invert();

        Matrix4f viewMatrix = createViewMatrix(camera);
        Matrix4f invViewMatrix = new Matrix4f(viewMatrix).invert();

        Matrix4f rotationMatrix = createRotationMatrix(camera);

        Matrix4f fullMatN = new Matrix4f(viewMatrix);
        Matrix4f fullMatP = new Matrix4f(invViewMatrix);

        shader.setInvProjMatrix(invProjMatrix);
        shader.setInvViewMatrix(invViewMatrix);
        shader.setRotationMatrix(rotationMatrix);
        shader.setFullMatN(fullMatN);
        shader.setFullMatP(fullMatP);

        shader.setCameraPosition(new Vector3f((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z));

        shader.setBlackholePosition(new Vector3f((float) blackholePos.x, (float) blackholePos.y, (float) blackholePos.z));
        shader.setColor(color);
        shader.setResolution(mainTarget.width, mainTarget.height);
        shader.setGameTime((float) (System.currentTimeMillis() / 1000.0));
        shader.setRenderDistance(renderDistance);
        shader.setOutSize(mainTarget.width, mainTarget.height);

        RenderSystem.activeTexture(GL33.GL_TEXTURE0);
        RenderSystem.bindTexture(mainTarget.getColorTextureId());
        shader.setDiffuseSampler(0);

        RenderSystem.activeTexture(GL33.GL_TEXTURE1);
        RenderSystem.bindTexture(mainTarget.getDepthTextureId());
        shader.setDepthSampler(1);

        GL33.glBindVertexArray(quadVAO);
        GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, 6);
        GL33.glBindVertexArray(0);

        shader.unbind();
    }

    private Matrix4f createViewMatrix(Camera camera)
    {
        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.rotateX((float) Math.toRadians(camera.getXRot()));
        viewMatrix.rotateY((float) Math.toRadians(camera.getYRot() + 180.0f));
        return viewMatrix;
    }

    private Matrix4f createRotationMatrix(Camera camera)
    {
        Matrix4f rotationMatrix = new Matrix4f();
        rotationMatrix.rotateY((float) Math.toRadians(-camera.getYRot()));
        rotationMatrix.rotateX((float) Math.toRadians(-camera.getXRot()));
        return rotationMatrix;
    }

    public void cleanup()
    {
        if (quadVBO != 0)
        {
            GL33.glDeleteBuffers(quadVBO);
            quadVBO = 0;
        }
        if (quadVAO != 0)
        {
            GL33.glDeleteVertexArrays(quadVAO);
            quadVAO = 0;
        }
        if (shader != null)
        {
            shader.cleanup();
            shader = null;
        }
        initialized = false;
        System.out.println("[BlackHoleRenderer] Cleaned up resources");
    }

    public boolean isInitialized()
    {
        return initialized;
    }
}