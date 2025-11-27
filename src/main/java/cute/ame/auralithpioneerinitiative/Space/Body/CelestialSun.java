package cute.ame.auralithpioneerinitiative.Space.Body;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import cute.ame.auralithpioneerinitiative.Space.Rendering.Geometry.AuralithMesh;
import cute.ame.auralithpioneerinitiative.Space.Rendering.Geometry.AuralithTriangle;
import cute.ame.auralithpioneerinitiative.Space.Rendering.Geometry.GeometryGenerator;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class CelestialSun extends CelestialBodyBase {

    protected Vector3f coreColor;
    protected Vector3f glowColor;
    protected float glowIntensity = 1.2f;
    protected int glowLayers = 8;
    protected float pulseSpeed = 0.3f;
    protected float pulseAmount = 0.08f;

    protected AuralithMesh coreMesh;

    public CelestialSun(Vec3 pos, float size) {
        this(pos, size,
                new Vector3f(1.0f, 0.85f, 0.6f),
                new Vector3f(1.0f, 0.6f, 0.3f));
    }

    public CelestialSun(Vec3 pos, float size, Vector3f coreColor, Vector3f glowColor) {
        super(pos, size);
        this.coreColor = coreColor;
        this.glowColor = glowColor;
        generateMesh();
    }

    protected void generateMesh() {
        coreMesh = GeometryGenerator.generateCube(size, 1, true);
    }

    @Override
    public boolean emitLight() {
        return true;
    }

    @Override
    public void render(PoseStack poseStack, Camera camera) {
        poseStack.pushPose();

        Vec3 cameraPos = camera.getPosition();
        double relX = this.pos.x - cameraPos.x;
        double relY = this.pos.y - cameraPos.y;
        double relZ = this.pos.z - cameraPos.z;

        poseStack.translate(relX, relY, relZ);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        float time = (float)(System.currentTimeMillis() / 1000.0);
        float pulse = 1.0f + (float)Math.sin(time * pulseSpeed) * pulseAmount;

        for (int i = glowLayers; i > 0; i--) {
            float t = (float)i / glowLayers;
            float layerScale = 1.0f + t * 1.2f;
            float layerOpacity = (1.0f - t * t) * glowIntensity * 0.15f;

            renderGlowLayer(poseStack, layerScale * pulse, layerOpacity, t);
        }

        renderCore(poseStack, pulse);

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    protected void renderCore(PoseStack poseStack, float pulse) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        poseStack.pushPose();
        poseStack.scale(pulse, pulse, pulse);

        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(
                VertexFormat.Mode.TRIANGLES,
                DefaultVertexFormat.POSITION_COLOR
        );

        for (AuralithTriangle tri : coreMesh.triangles) {
            Vector3f color = new Vector3f(coreColor).mul(1.5f);

            buffer.addVertex(matrix, tri.v1.position().x, tri.v1.position().y, tri.v1.position().z)
                    .setColor(Math.min(color.x, 1.0f), Math.min(color.y, 1.0f), Math.min(color.z, 1.0f), 1.0f);
            buffer.addVertex(matrix, tri.v2.position().x, tri.v2.position().y, tri.v2.position().z)
                    .setColor(Math.min(color.x, 1.0f), Math.min(color.y, 1.0f), Math.min(color.z, 1.0f), 1.0f);
            buffer.addVertex(matrix, tri.v3.position().x, tri.v3.position().y, tri.v3.position().z)
                    .setColor(Math.min(color.x, 1.0f), Math.min(color.y, 1.0f), Math.min(color.z, 1.0f), 1.0f);
        }

        MeshData meshData = buffer.build();
        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }

        poseStack.popPose();
    }

    protected void renderGlowLayer(PoseStack poseStack, float scale, float opacity, float t) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);

        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(
                VertexFormat.Mode.TRIANGLES,
                DefaultVertexFormat.POSITION_COLOR
        );

        Vector3f color = new Vector3f(coreColor).lerp(glowColor, t);

        for (AuralithTriangle tri : coreMesh.triangles) {
            buffer.addVertex(matrix, tri.v1.position().x, tri.v1.position().y, tri.v1.position().z)
                    .setColor(color.x, color.y, color.z, opacity);
            buffer.addVertex(matrix, tri.v2.position().x, tri.v2.position().y, tri.v2.position().z)
                    .setColor(color.x, color.y, color.z, opacity);
            buffer.addVertex(matrix, tri.v3.position().x, tri.v3.position().y, tri.v3.position().z)
                    .setColor(color.x, color.y, color.z, opacity);
        }

        MeshData meshData = buffer.build();
        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }

        poseStack.popPose();
    }

    public void setCoreColor(Vector3f color) {
        this.coreColor = new Vector3f(color);
    }

    public void setGlowColor(Vector3f color) {
        this.glowColor = new Vector3f(color);
    }

    public void setGlowIntensity(float intensity) {
        this.glowIntensity = Math.max(0.0f, Math.min(2.0f, intensity));
    }

    public void setGlowLayers(int layers) {
        this.glowLayers = Math.max(1, Math.min(15, layers));
    }

    public void setPulse(float speed, float amount) {
        this.pulseSpeed = speed;
        this.pulseAmount = amount;
    }
}