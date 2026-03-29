package cute.ame.auralithpioneerinitiative.HoloPanel;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import cute.ame.auralithpioneerinitiative.HoloPanel.Anchor.PanelAnchor;
import cute.ame.auralithpioneerinitiative.HoloPanel.Widget.HoloPanelContext;
import cute.ame.auralithpioneerinitiative.HoloPanel.Widget.HoloPanelWidget;
import cute.ame.auralithpioneerinitiative.HoloPanel.Widget.ScrollPanel;
import cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity;
import cute.ame.auralithpioneerinitiative.Ship.Input.FlightCameraState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public final class HoloPanelRenderer {

    private static final HoloPanelRenderer INSTANCE = new HoloPanelRenderer();
    public static HoloPanelRenderer getInstance() { return INSTANCE; }

    private static final float FOV_GATE_SMOOTH = 0.15f;
    private static final float DIST_GATE_NEAR = 3.0f;
    private static final float DIST_GATE_FAR = 8.0f;
    private static final float FOCUS_MAX_DIST = 5.0f;

    private HoloPanelType focusedPanel = null;
    private HoloPanelWidget focusedRootWidget = null;

    private HoloPanelRenderer() {}
    public void renderPanels(Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, Camera camera, ClientLevel level)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ShipEntity ship = null;
        if (mc.player.getVehicle() instanceof ShipEntity s) ship = s;

        ScrollPanel.fboHeight = 200;

        for (HoloPanelType panel : HoloPanelRegistry.getInstance().all()) renderSinglePanel(panel, ship, level, camera, partialTick, frustumMatrix, projectionMatrix);
    }

    private void renderSinglePanel(HoloPanelType panel, ShipEntity ship, ClientLevel level, Camera camera, float partialTick, Matrix4f frustumMatrix, Matrix4f projectionMatrix)
    {

        Vector3f worldPos = resolveWorldPosition(panel, ship, camera, partialTick);
        if (worldPos == null) return;

        float alpha = resolveAlpha(panel, ship, camera, worldPos);
        if (alpha <= 0.001f) return;

        Vector3f normal = resolveNormal(panel, ship);
        Vector3f shipUp = new Vector3f(0, 1, 0);
        Vector3f shipRight = new Vector3f(1, 0, 0);
        if (ship != null)
        {
            ship.getShipRotation().transform(shipUp);
            ship.getShipRotation().transform(shipRight);
        }

        RenderTarget fbo = HoloPanelRegistry.getInstance().getOrCreateFBO(panel);
        ScrollPanel.fboHeight = panel.getHeight();

        BlockPos blockPos = (panel.getAnchor() == PanelAnchor.BLOCK_RELATIVE && panel instanceof BlockSidedPanel bsp) ? bsp.getBlockPos() : null;
        HoloPanelContext ctx = new HoloPanelContext(ship, level, camera, partialTick, blockPos);

        fbo.bindWrite(true);
        RenderSystem.clearColor(0f, 0f, 0f, 0f);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.viewport(0, 0, panel.getWidth(), panel.getHeight());

        Matrix4f ortho = new Matrix4f().ortho(0, panel.getWidth(), panel.getHeight(), 0, -1000, 1000);
        RenderSystem.setProjectionMatrix(ortho, VertexSorting.ORTHOGRAPHIC_Z);

        var mvStack = RenderSystem.getModelViewStack();
        mvStack.pushMatrix();
        mvStack.identity();
        RenderSystem.applyModelViewMatrix();

        try
        {
            GuiGraphics g = new GuiGraphics(Minecraft.getInstance(), Minecraft.getInstance().renderBuffers().bufferSource());
            drawPanelFrame(g, panel.getWidth(), panel.getHeight(), panel.getId().getPath());
            panel.renderContent(g, ctx, partialTick);
            g.flush();
            Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
        }
        finally
        {
            mvStack.popMatrix();
            RenderSystem.applyModelViewMatrix();
            fbo.unbindWrite();
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
            RenderSystem.viewport(0, 0,
                Minecraft.getInstance().getWindow().getWidth(),
                Minecraft.getInstance().getWindow().getHeight());

            RenderSystem.setProjectionMatrix(projectionMatrix, VertexSorting.DISTANCE_TO_ORIGIN);
        }

        drawWorldQuad(worldPos, normal, shipUp, shipRight, panel.worldWidth(), panel.worldHeight(), alpha, fbo, frustumMatrix, projectionMatrix, camera, panel.getAnchor());
    }

   private void drawPanelFrame(GuiGraphics g, int pw, int ph, String panelName)
   {
        g.fill(0, 0, pw, ph, HoloPanelColors.opaque(HoloPanelColors.PANEL_BG));
        g.fill(0, 0, pw, 1, HoloPanelColors.opaque(HoloPanelColors.AMBER_BORDER));// top
        g.fill(0, ph - 1, pw, ph, HoloPanelColors.opaque(HoloPanelColors.AMBER_BORDER)); // bottom
        g.fill(0, 0, 1, ph, HoloPanelColors.opaque(HoloPanelColors.AMBER_BORDER));// left
        g.fill(pw - 1, 0, pw, ph, HoloPanelColors.opaque(HoloPanelColors.AMBER_BORDER)); // right
        g.drawString(Minecraft.getInstance().font, panelName, 3, 3, HoloPanelColors.opaque(HoloPanelColors.AMBER_DIM), false);
    }

    private Vector3f resolveWorldPosition(HoloPanelType panel, ShipEntity ship, Camera camera, float partialTick)
    {
        return switch (panel.getAnchor())
        {
            case COCKPIT_RELATIVE -> resolveCockpitPos(panel, ship);
            case BLOCK_RELATIVE -> resolveBlockPos(panel);
            case PLAYER_RELATIVE -> resolvePlayerPos(panel, camera);
        };
    }

    private Vector3f resolveCockpitPos(HoloPanelType panel, ShipEntity ship)
    {
        if (ship == null) return null;
        if (!(panel instanceof CockpitRelativePanel crp)) return null;

        var seatVec = ship.getDefinition()
            .map(def -> def.findCockpitOffset().toVec3())
            .orElse(net.minecraft.world.phys.Vec3.ZERO);

        Vector3f panelOffset = crp.getPanelOffset();
        Vector3f localCombined = new Vector3f((float) seatVec.x + panelOffset.x, (float) seatVec.y + panelOffset.y, (float) seatVec.z + panelOffset.z);
        ship.getShipRotation().transform(localCombined);

        return new Vector3f((float) ship.getX() + localCombined.x, (float) ship.getY() + localCombined.y, (float) ship.getZ() + localCombined.z);
    }

    private Vector3f resolveBlockPos(HoloPanelType panel)
    {
        if (!(panel instanceof BlockSidedPanel bsp)) return null;
        BlockPos bp = bsp.getBlockPos();
        if (bp == null) return null;
        return new Vector3f(bp.getX() + 0.5f, bp.getY() + 0.5f, bp.getZ() + 0.5f);
    }

    private Vector3f resolvePlayerPos(HoloPanelType panel, Camera camera)
    {
        if (!(panel instanceof PlayerRelativePanel prp)) return null;
        float dist = prp.getDistance();
        var look = camera.getLookVector();
        var pos = camera.getPosition();
        return new Vector3f((float)(pos.x + look.x * dist), (float)(pos.y + look.y * dist), (float)(pos.z + look.z * dist)
        );
    }

    private float resolveAlpha(HoloPanelType panel, ShipEntity ship, Camera camera, Vector3f worldPos) {
        return switch (panel.getAnchor())
        {
            case COCKPIT_RELATIVE -> resolveCockpitAlpha(panel, ship, camera);
            case BLOCK_RELATIVE -> resolveDistanceAlpha(worldPos, camera);
            case PLAYER_RELATIVE -> 1.0f;
        };
    }

    private float resolveCockpitAlpha(HoloPanelType panel, ShipEntity ship, Camera camera)
    {
        if (panel.getFovGate() < 0) return 1.0f;
        if (!(panel instanceof CockpitRelativePanel crp)) return 0f;

        Vector3f panelNormal = resolveNormal(panel, ship);
        var camFwd = camera.getLookVector();
        float dot = -(camFwd.x * panelNormal.x + camFwd.y * panelNormal.y + camFwd.z * panelNormal.z);
        return smoothstep(panel.getFovGate(), panel.getFovGate() + FOV_GATE_SMOOTH, dot);
    }

    private float resolveDistanceAlpha(Vector3f worldPos, Camera camera)
    {
        var camPos = camera.getPosition();
        float dx = worldPos.x - (float) camPos.x;
        float dy = worldPos.y - (float) camPos.y;
        float dz = worldPos.z - (float) camPos.z;
        float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        return smoothstep(DIST_GATE_FAR, DIST_GATE_NEAR, dist);
    }

    private Vector3f resolveNormal(HoloPanelType panel, ShipEntity ship)
    {
        if (panel instanceof CockpitRelativePanel crp)
        {
            Vector3f local = new Vector3f(crp.getFacingNormal());
            if (ship != null) ship.getShipRotation().transform(local);
            return local;
        }
        if (panel instanceof BlockSidedPanel bsp) return bsp.getWorldFacing();

        return new Vector3f(0, 0, -1);
    }

    private void drawWorldQuad(Vector3f worldPos, Vector3f normal, Vector3f shipUp, Vector3f shipRight, float quadW, float quadH, float alpha, RenderTarget fbo, Matrix4f frustumMatrix, Matrix4f projectionMatrix, Camera camera, PanelAnchor anchor)
    {
        Vector3f up = new Vector3f(shipUp).sub(new Vector3f(normal).mul(normal.dot(shipUp))).normalize();
        Vector3f right = new Vector3f(normal).cross(up).normalize();

        float hw = quadW * 0.5f;
        float hh = quadH * 0.5f;

        var camPos = camera.getPosition();
        float ox = worldPos.x - (float) camPos.x;
        float oy = worldPos.y - (float) camPos.y;
        float oz = worldPos.z - (float) camPos.z;

        float[] bl = { ox - right.x*hw - up.x*hh, oy - right.y*hw - up.y*hh, oz - right.z*hw - up.z*hh };
        float[] br = { ox + right.x*hw - up.x*hh, oy + right.y*hw - up.y*hh, oz + right.z*hw - up.z*hh };
        float[] tr = { ox + right.x*hw + up.x*hh, oy + right.y*hw + up.y*hh, oz + right.z*hw + up.z*hh };
        float[] tl = { ox - right.x*hw + up.x*hh, oy - right.y*hw + up.y*hh, oz - right.z*hw + up.z*hh };

        var mvStack = RenderSystem.getModelViewStack();
        mvStack.pushMatrix();
        mvStack.identity();
        RenderSystem.applyModelViewMatrix();
        try
        {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.disableCull();

            if (anchor == PanelAnchor.COCKPIT_RELATIVE)
            {
                GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
                GL11.glPolygonOffset(-1f, -1f);
            }

            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, fbo.getColorTextureId());
            RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

            Tesselator    tess = Tesselator.getInstance();
            BufferBuilder buf  = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

            int a = (int)(alpha * 255);
            buf.addVertex(frustumMatrix, bl[0], bl[1], bl[2]).setUv(1, 0).setColor(255, 255, 255, a);
            buf.addVertex(frustumMatrix, br[0], br[1], br[2]).setUv(0, 0).setColor(255, 255, 255, a);
            buf.addVertex(frustumMatrix, tr[0], tr[1], tr[2]).setUv(0, 1).setColor(255, 255, 255, a);
            buf.addVertex(frustumMatrix, tl[0], tl[1], tl[2]).setUv(1, 1).setColor(255, 255, 255, a);

            BufferUploader.drawWithShader(buf.buildOrThrow());
            if (anchor == PanelAnchor.COCKPIT_RELATIVE)
            {
                GL11.glPolygonOffset(0f, 0f);
                GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
            }

            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();

        }
        finally
        {
            mvStack.popMatrix();
            RenderSystem.applyModelViewMatrix();
        }
    }

    public void forwardScroll(double delta)
    {
        if (focusedPanel == null) return;
        if (focusedRootWidget == null) return;
        int px = (int)(FlightCameraState.getCursorU() * focusedPanel.getWidth());
        int py = (int)(FlightCameraState.getCursorV() * focusedPanel.getHeight());
        focusedRootWidget.onMouseScroll(px, py, delta);
    }

    private static float smoothstep(float edge0, float edge1, float x)
    {
        float t = Math.max(0f, Math.min(1f, (x - edge0) / (edge1 - edge0)));
        return t * t * (3f - 2f * t);
    }

    public interface CockpitRelativePanel
    {
        Vector3f getCockpitOffset();
        Vector3f getPanelOffset();
        Vector3f getFacingNormal();
    }

    public interface BlockSidedPanel
    {
        BlockPos getBlockPos();
        Vector3f getWorldFacing();
    }

    public interface PlayerRelativePanel
    {
        float getDistance();
    }
}