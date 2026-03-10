package cute.ame.auralithpioneerinitiative.Client.Sky;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import cute.ame.auralithpioneerinitiative.API.AuralithAPI;
import cute.ame.auralithpioneerinitiative.Data.*;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.Optional;
import java.util.Random;

public final class SolarSystemRenderer
{
    private static final SolarSystemRenderer INSTANCE = new SolarSystemRenderer();
    public static SolarSystemRenderer getInstance() { return INSTANCE; }
    private SolarSystemRenderer() {}

    private static final float SKY_RADIUS   = 100.0f;
    private static final float REF_AU       = 1.0f;
    private static final float MIN_APPARENT = 0.3f;

    public boolean shouldHandleSky(ResourceKey<Level> dimension)
    {
        return AuralithAPI.hasSkyFor(dimension);
    }

    public void renderSky(Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick,
                          Camera camera, boolean isFoggy, Runnable skyFogSetup, ClientLevel level)
    {
        skyFogSetup.run();
        Matrix4f rotOnly = new Matrix4f(frustumMatrix);
        rotOnly.m30(0.0f).m31(0.0f).m32(0.0f);

        PoseStack ps = new PoseStack();
        ps.mulPose(rotOnly);
        renderSkyBackdrop(ps, level, camera, partialTick);

        ps.pushPose();
        renderCelestials(ps, level, partialTick);
        ps.popPose();
    }

    private void renderSkyBackdrop(PoseStack ps, ClientLevel level, Camera camera, float partialTick)
    {
        boolean isSpace = AuralithAPI.getBindingForDimension(level.dimension())
                .map(AuralithAPI.DimensionBinding::isSpaceDimension).orElse(false);
        if (isSpace) return;

        Vec3 sky = level.getSkyColor(camera.getPosition(), partialTick);
        float r = (float) sky.x, g = (float) sky.y, b = (float) sky.z;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        Tesselator    tess = Tesselator.getInstance();
        BufferBuilder buf  = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f m = ps.last().pose();
        float d = 512f;

        buf.addVertex(m, -d, 16f, -d).setColor(r, g, b, 1f);
        buf.addVertex(m, -d, 16f,  d).setColor(r, g, b, 1f);
        buf.addVertex(m,  d, 16f,  d).setColor(r, g, b, 1f);
        buf.addVertex(m,  d, 16f, -d).setColor(r, g, b, 1f);
        addSkyWall(buf, m, -d, -d,  d, -d, r, g, b, d);
        addSkyWall(buf, m,  d, -d, -d, -d, r, g, b, d);
        addSkyWall(buf, m, -d,  d, -d, -d, r, g, b, d);
        addSkyWall(buf, m,  d,  d,  d,  d, r, g, b, d);

        BufferUploader.drawWithShader(buf.buildOrThrow());
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }

    private static void addSkyWall(BufferBuilder buf, Matrix4f m,
                                   float x1, float z1, float x2, float z2,
                                   float r, float g, float b, float d)
    {
        buf.addVertex(m, x1, 16f, z1).setColor(r, g, b, 1f);
        buf.addVertex(m, x2, 16f, z2).setColor(r, g, b, 1f);
        buf.addVertex(m, x2, -d,  z2).setColor(r, g, b, 0f);
        buf.addVertex(m, x1, -d,  z1).setColor(r, g, b, 0f);
    }

    private void renderCelestials(PoseStack ps, ClientLevel level, float partialTick)
    {
        Optional<AuralithAPI.DimensionBinding> bOpt = AuralithAPI.getBindingForDimension(level.dimension());
        if (bOpt.isEmpty()) return;
        AuralithAPI.DimensionBinding binding = bOpt.get();
        Optional<SolarSystemDefinition> sOpt = AuralithAPI.getSolarSystem(binding.systemId());
        if (sOpt.isEmpty()) return;

        if (binding.isSpaceDimension()) renderFromSpace(ps, level, sOpt.get(), partialTick);
        else renderFromSurface(ps, level, sOpt.get(), binding.planetId(), partialTick);
    }

    private void renderFromSurface(PoseStack ps, ClientLevel level, SolarSystemDefinition system, ResourceLocation currentPlanetId, float partialTick)
    {
        long tick = level.getGameTime();

        float raw = ((level.getDayTime() % 24000L) + partialTick) / 24000.0f;
        float skyAngle = raw - 0.25f + 0.5f * (float) Math.sin((raw - 0.25f) * Math.PI * 2.0) / ((float) Math.PI * 2f);
        ps.mulPose(new Quaternionf().rotationY((float) Math.toRadians(-90.0)));
        ps.mulPose(new Quaternionf().rotationX(skyAngle * (float)(2.0 * Math.PI)));

        renderSun(ps, system.sun(), tick, partialTick);

        PlanetDefinition current = system.planets().stream().filter(p -> p.id().equals(currentPlanetId)).findFirst().orElse(null);
        if (current == null) return;

        double myAngle  = current.orbit().computeAngle(tick, partialTick);
        double myRadius = current.orbit().computeCurrentRadius(myAngle);
        float[] myPos   = current.orbit().compute3DPosition(myAngle, myRadius, 1.0f);

        for (PlanetDefinition planet : system.planets())
        {
            if (planet.id().equals(currentPlanetId)) continue;

            double theirAngle = planet.orbit().computeAngle(tick, partialTick);
            double theirRadius = planet.orbit().computeCurrentRadius(theirAngle);
            float[] theirPos = planet.orbit().compute3DPosition(theirAngle, theirRadius, 1.0f);

            float dx = myPos[0] - theirPos[0];
            float dy = myPos[1] - theirPos[1];
            float dz = myPos[2] - theirPos[2];
            float dist = Math.max((float) Math.sqrt(dx*dx + dy*dy + dz*dz), 0.01f);

            float apparentSize = Math.max(MIN_APPARENT, planet.size() * REF_AU / dist);
            float cdx = dx / dist, cdy = dy / dist, cdz = dz / dist;

            renderPlanetInSky(ps, planet, theirPos, apparentSize, cdx, cdy, cdz, tick, partialTick);
        }
    }

    private void renderPlanetInSky(PoseStack ps, PlanetDefinition planet, float[] theirPos, float apparentSize, float cdx, float cdy, float cdz, long tick, float partialTick)
    {
        float len = (float) Math.sqrt(theirPos[0]*theirPos[0] + theirPos[1]*theirPos[1] + theirPos[2]*theirPos[2]);
        if (len < 1e-6f) return;

        float sx = theirPos[0] / len * SKY_RADIUS;
        float sy = theirPos[1] / len * SKY_RADIUS;
        float sz = theirPos[2] / len * SKY_RADIUS;

        ps.pushPose();
        ps.translate(sx, sy, sz);

        float axialAngle = ((tick + partialTick) * planet.axialRotationSpeed() * 0.001f) % (float)(2.0 * Math.PI);
        float tiltRad    = (float) Math.toRadians(planet.axialTilt());

        Quaternionf invRot = new Quaternionf().rotationY(axialAngle).mul(new Quaternionf().rotationZ(tiltRad)).conjugate();
        Vector3f localCam = new Vector3f(cdx, cdy, cdz);
        invRot.transform(localCam);

        poseAxialRotation(ps, planet.axialRotationSpeed(), tick, partialTick);
        ps.mulPose(new Quaternionf().rotationZ(tiltRad));

        planet.rings().ifPresent(rings -> renderRings(ps, rings, apparentSize, localCam.x, localCam.y, localCam.z, false));

        ps.pushPose();
        ps.scale(apparentSize, apparentSize, apparentSize);
        renderTextureCube(ps, planet.resolveTexture());
        ps.popPose();

        planet.rings().ifPresent(rings -> renderRings(ps, rings, apparentSize, localCam.x, localCam.y, localCam.z, true));

        planet.atmosphere().ifPresent(atmo -> 
        {
            ps.pushPose();
            float as = apparentSize * atmo.scale();
            ps.scale(as, as, as);
            AtmosphereRenderer.render(ps, atmo, cdx, cdy, cdz);
            ps.popPose();
        });

        for (MoonDefinition moon : planet.moons())
            renderMoon(ps, moon, apparentSize, tick, partialTick);

        ps.popPose();
    }

    private void renderFromSpace(PoseStack ps, ClientLevel level, SolarSystemDefinition system, float partialTick)
    {
        long tick = level.getGameTime();

        ps.pushPose();
        renderSun(ps, system.sun(), tick, partialTick);
        ps.popPose();

        for (PlanetDefinition planet : system.planets())
        {
            double angle  = planet.orbit().computeAngle(tick, partialTick);
            double radius = planet.orbit().computeCurrentRadius(angle);
            float[] pos   = planet.orbit().compute3DPosition(angle, radius, 1.0f);

            float len = (float) Math.sqrt(pos[0]*pos[0] + pos[1]*pos[1] + pos[2]*pos[2]);
            if (len < 1e-6f) continue;

            float sx = pos[0] / len * SKY_RADIUS;
            float sy = pos[1] / len * SKY_RADIUS;
            float sz = pos[2] / len * SKY_RADIUS;

            float apparentSize = Math.max(MIN_APPARENT, planet.size() * 0.5f / (float) radius);
            float cdx = pos[0] / len, cdy = pos[1] / len, cdz = pos[2] / len;

            ps.pushPose();
            ps.translate(sx, sy, sz);

            poseAxialRotation(ps, planet.axialRotationSpeed(), tick, partialTick);
            ps.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(planet.axialTilt())));

            ps.pushPose();
            ps.scale(apparentSize, apparentSize, apparentSize);
            renderTextureCube(ps, planet.resolveTexture());
            ps.popPose();

            planet.rings().ifPresent(rings -> renderRings(ps, rings, apparentSize));

            planet.atmosphere().ifPresent(atmo -> {
                ps.pushPose();
                float as = apparentSize * atmo.scale();
                ps.scale(as, as, as);
                AtmosphereRenderer.render(ps, atmo, cdx, cdy, cdz);
                ps.popPose();
            });

            for (MoonDefinition moon : planet.moons())
                renderMoon(ps, moon, apparentSize, tick, partialTick);

            ps.popPose();
        }
    }

    private void renderRings(PoseStack ps, RingDefinition rings, float apparentSize)
    {
        setupRingRenderState();
        Random rng = new Random(rings.seed());
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        Matrix4f m = ps.last().pose();

        float planetRadius = apparentSize * 0.5f;
        float innerR = rings.innerRadius() * planetRadius;
        float outerR = rings.outerRadius() * planetRadius;
        int   count  = Math.max(1, rings.ringCount());

        emitRingQuads(buf, m, innerR, outerR, count, rings, rng);
        BufferUploader.drawWithShader(buf.buildOrThrow());
        teardownRingRenderState();
    }

    private void renderRings(PoseStack ps, RingDefinition rings, float apparentSize, float localCamX, float localCamY, float localCamZ, boolean front)
    {
        setupRingRenderState();

        float planetRadius = apparentSize * 0.5f;
        float innerR = rings.innerRadius() * planetRadius;
        float outerR = rings.outerRadius() * planetRadius;
        int count = Math.max(1, rings.ringCount());

        float[] quadDotX = {  0,  0, -1, +1, -1, +1, -1, +1 };
        float[] quadDotZ = { -1, +1,  0,  0, -1, -1, +1, +1 };

        Random rng  = new Random(rings.seed());
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        Matrix4f m = ps.last().pose();

        for (int ri = 0; ri < count; ri++)
        {
            float t0 = (float)  ri      / count;
            float t1 = (float) (ri + 1) / count;
            float r0 = lerp(innerR, outerR, t0);
            float r1 = lerp(innerR, outerR, t1);

            float rndT = rng.nextFloat();
            float cr = lerp(rings.minR(), rings.maxR(), rndT);
            float cg = lerp(rings.minG(), rings.maxG(), rndT);
            float cb = lerp(rings.minB(), rings.maxB(), rndT);
            float ca = rings.opacity() * (0.6f + 0.4f * rng.nextFloat());

            float[][][] quads = 
            {
                {{ -r0,0,-r1 },{  r0,0,-r1 },{  r0,0,-r0 },{ -r0,0,-r0 }},
                {{ -r0,0, r0 },{  r0,0, r0 },{  r0,0, r1 },{ -r0,0, r1 }},
                {{ -r1,0,-r0 },{ -r0,0,-r0 },{ -r0,0, r0 },{ -r1,0, r0 }},
                {{  r0,0,-r0 },{  r1,0,-r0 },{  r1,0, r0 },{  r0,0, r0 }},
                {{ -r1,0,-r1 },{ -r0,0,-r1 },{ -r0,0,-r0 },{ -r1,0,-r0 }},
                {{  r0,0,-r1 },{  r1,0,-r1 },{  r1,0,-r0 },{  r0,0,-r0 }},
                {{ -r1,0, r0 },{ -r0,0, r0 },{ -r0,0, r1 },{ -r1,0, r1 }},
                {{  r0,0, r0 },{  r1,0, r0 },{  r1,0, r1 },{  r0,0, r1 }},
            };

            for (int qi = 0; qi < quads.length; qi++)
            {
                float dot = quadDotX[qi] * localCamX + quadDotZ[qi] * localCamZ;
                if ((dot >= 0) != front) continue;
                for (float[] v : quads[qi]) buf.addVertex(m, v[0], v[1], v[2]).setUv(0f, 0f).setColor(cr, cg, cb, ca);
            }
        }

        BufferUploader.drawWithShader(buf.buildOrThrow());
        teardownRingRenderState();
    }

    private void setupRingRenderState()
    {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, RingTextureHelper.getWhite());
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE,       GlStateManager.DestFactor.ZERO);
        RenderSystem.disableCull();
    }

    private void teardownRingRenderState()
    {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
    }

    private void emitRingQuads(BufferBuilder buf, Matrix4f m, float innerR, float outerR, int count, RingDefinition rings, Random rng)
    {
        for (int ri = 0; ri < count; ri++)
        {
            float t0 = (float)  ri      / count;
            float t1 = (float) (ri + 1) / count;
            float r0 = lerp(innerR, outerR, t0);
            float r1 = lerp(innerR, outerR, t1);

            float rndT = rng.nextFloat();
            float cr = lerp(rings.minR(), rings.maxR(), rndT);
            float cg = lerp(rings.minG(), rings.maxG(), rndT);
            float cb = lerp(rings.minB(), rings.maxB(), rndT);
            float ca = rings.opacity() * (0.6f + 0.4f * rng.nextFloat());

            buf.addVertex(m, -r0,0,-r1).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,  r0,0,-r1).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,  r0,0,-r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, -r0,0,-r0).setUv(0,0).setColor(cr,cg,cb,ca);

            buf.addVertex(m, -r0,0, r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,  r0,0, r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,  r0,0, r1).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, -r0,0, r1).setUv(0,0).setColor(cr,cg,cb,ca);

            buf.addVertex(m, -r1,0,-r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, -r0,0,-r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, -r0,0, r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, -r1,0, r0).setUv(0,0).setColor(cr,cg,cb,ca);

            buf.addVertex(m,  r0,0,-r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,  r1,0,-r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,  r1,0, r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,  r0,0, r0).setUv(0,0).setColor(cr,cg,cb,ca);

            buf.addVertex(m, -r1,0,-r1).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, -r0,0,-r1).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, -r0,0,-r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, -r1,0,-r0).setUv(0,0).setColor(cr,cg,cb,ca);

            buf.addVertex(m,  r0,0,-r1).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,  r1,0,-r1).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,  r1,0,-r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,  r0,0,-r0).setUv(0,0).setColor(cr,cg,cb,ca);

            buf.addVertex(m, -r1,0, r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, -r0,0, r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, -r0,0, r1).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, -r1,0, r1).setUv(0,0).setColor(cr,cg,cb,ca);

            buf.addVertex(m,  r0,0, r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,  r1,0, r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,  r1,0, r1).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,  r0,0, r1).setUv(0,0).setColor(cr,cg,cb,ca);
        }
    }

    private void renderMoon(PoseStack ps, MoonDefinition moon, float parentApparentSize, long tick, float partialTick)
    {
        double angle = moon.orbit().computeAngle(tick, partialTick);
        double radius = moon.orbit().computeCurrentRadius(angle);

        float moonScale = parentApparentSize * 2.5f;
        float[] pos = moon.orbit().compute3DPosition(angle, radius, moonScale);

        float cdLen = (float) Math.sqrt(pos[0]*pos[0] + pos[1]*pos[1] + pos[2]*pos[2]);
        float cdx = (cdLen > 1e-6f) ? -pos[0] / cdLen : 0f;
        float cdy = (cdLen > 1e-6f) ? -pos[1] / cdLen : -1f;
        float cdz = (cdLen > 1e-6f) ? -pos[2] / cdLen : 0f;

        float moonSize = Math.max(moon.size() * parentApparentSize * 0.05f, MIN_APPARENT * 0.3f);

        ps.pushPose();
        ps.translate(pos[0], pos[1], pos[2]);
        poseAxialRotation(ps, moon.axialRotationSpeed(), tick, partialTick);
        ps.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(moon.axialTilt())));

        ps.pushPose();
        ps.scale(moonSize, moonSize, moonSize);
        renderTextureCube(ps, moon.resolveTexture());
        ps.popPose();

        moon.atmosphere().ifPresent(atmo -> 
        {
            ps.pushPose();
            float as = moonSize * atmo.scale();
            ps.scale(as, as, as);
            AtmosphereRenderer.render(ps, atmo, cdx, cdy, cdz);
            ps.popPose();
        });

        ps.popPose();
    }

    private void renderSun(PoseStack poseStack, SunDefinition sun, long absoluteTick, float partialTick)
    {
        poseStack.pushPose();
        poseStack.translate(0.0f, SKY_RADIUS, 0.0f);
        poseAxialRotation(poseStack, sun.axialRotationSpeed(), absoluteTick, partialTick);

        if (sun.glowLayers() > 0) renderSunGlow(poseStack, sun);

        float s = sun.size();
        poseStack.pushPose();
        poseStack.scale(s, s, s);
        renderTextureCube(poseStack, sun.texture());
        poseStack.popPose();

        poseStack.popPose();
    }

    private void renderSunGlow(PoseStack poseStack, SunDefinition sun)
    {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GL11.GL_SRC_ALPHA, GL11.GL_ONE,
                GL11.GL_ONE,       GL11.GL_ZERO);
        RenderSystem.disableCull();

        float r = sun.glowR(), g = sun.glowG(), b = sun.glowB();
        int   layers    = sun.glowLayers();
        float coreSize  = sun.size();
        float glowScale = sun.glowScale();

        for (int i = layers; i > 0; i--)
        {
            float t         = (float) i / layers;
            float layerSize = coreSize * (1.0f + t * (glowScale - 1.0f));
            float alpha     = (1.0f - t) * 0.06f;

            poseStack.pushPose();
            poseStack.scale(layerSize, layerSize, layerSize);
            renderColoredCube(poseStack, r, g, b, alpha);
            poseStack.popPose();
        }

        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
    }

    private void renderTextureCube(PoseStack ps, ResourceLocation texture)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);

        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();

        Tesselator    tess = Tesselator.getInstance();
        BufferBuilder buf  = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        Matrix4f m = ps.last().pose();
        float h = 0.5f;

        buf.addVertex(m,-h, h, h).setUv(0,1); buf.addVertex(m, h, h, h).setUv(1,1);
        buf.addVertex(m, h, h,-h).setUv(1,0); buf.addVertex(m,-h, h,-h).setUv(0,0);
        buf.addVertex(m,-h,-h,-h).setUv(0,0); buf.addVertex(m, h,-h,-h).setUv(1,0);
        buf.addVertex(m, h,-h, h).setUv(1,1); buf.addVertex(m,-h,-h, h).setUv(0,1);
        buf.addVertex(m, h, h, h).setUv(0,0); buf.addVertex(m,-h, h, h).setUv(1,0);
        buf.addVertex(m,-h,-h, h).setUv(1,1); buf.addVertex(m, h,-h, h).setUv(0,1);
        buf.addVertex(m,-h, h,-h).setUv(0,0); buf.addVertex(m, h, h,-h).setUv(1,0);
        buf.addVertex(m, h,-h,-h).setUv(1,1); buf.addVertex(m,-h,-h,-h).setUv(0,1);
        buf.addVertex(m, h, h,-h).setUv(0,0); buf.addVertex(m, h, h, h).setUv(1,0);
        buf.addVertex(m, h,-h, h).setUv(1,1); buf.addVertex(m, h,-h,-h).setUv(0,1);
        buf.addVertex(m,-h, h, h).setUv(0,0); buf.addVertex(m,-h, h,-h).setUv(1,0);
        buf.addVertex(m,-h,-h,-h).setUv(1,1); buf.addVertex(m,-h,-h, h).setUv(0,1);

        BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    private void renderColoredCube(PoseStack ps, float r, float g, float b, float a)
    {
        Tesselator    tess = Tesselator.getInstance();
        BufferBuilder buf  = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f m = ps.last().pose();
        for (float[][] face : CUBE_FACES)
        for (float[] v : face)
            buf.addVertex(m, v[0]*0.5f, v[1]*0.5f, v[2]*0.5f).setColor(r, g, b, a);
        
        BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    private void poseAxialRotation(PoseStack ps, float speed, long tick, float partialTick)
    {
        float angle = ((tick + partialTick) * speed * 0.001f) % (float)(2.0 * Math.PI);
        ps.mulPose(new Quaternionf().rotationY(angle));
    }

    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }

    private static final float[][][] CUBE_FACES =
    {
        {{-1, 1, 1},{1, 1, 1},{1, 1,-1},{-1, 1,-1}},
        {{-1,-1,-1},{1,-1,-1},{1,-1, 1},{-1,-1, 1}},
        {{ 1, 1, 1},{-1,1, 1},{-1,-1,1},{ 1,-1, 1}},
        {{ 1, 1,-1},{-1,1,-1},{-1,-1,-1},{1,-1,-1}},
        {{ 1, 1,-1},{1, 1, 1},{1,-1, 1},{1,-1,-1}},
        {{-1, 1, 1},{-1,1,-1},{-1,-1,-1},{-1,-1,1}},
    };
}