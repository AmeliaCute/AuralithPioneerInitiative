package cute.ame.auralithpioneerinitiative.SkyPlanet.Rendering;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import cute.ame.auralithpioneerinitiative.API.AuralithAPI;
import cute.ame.auralithpioneerinitiative.SkyPlanet.Data.PlanetDefinition;
import cute.ame.auralithpioneerinitiative.SkyPlanet.Data.RingDefinition;
import cute.ame.auralithpioneerinitiative.SkyPlanet.Data.SolarSystemDefinition;
import cute.ame.auralithpioneerinitiative.SkyPlanet.Data.SunDefinition;
import cute.ame.auralithpioneerinitiative.SkyPlanet.Texture.RingTextureHelper;
import cute.ame.auralithpioneerinitiative.SkyPlanet.RenderingHelper.ShaderHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
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

    private static final float SKY_RADIUS = 100.0f;
    public  static final float ORBIT_PLANET_SIZE = 60.0f;
    private static final float REF_AU = 1.0f;
    private static final float MIN_APPARENT = 0.3f;

    public void renderSky(Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, Camera camera, boolean isFoggy, Runnable skyFogSetup, ClientLevel level)
    {
        skyFogSetup.run();

        Matrix4f rotOnly = new Matrix4f(frustumMatrix);
        rotOnly.m03(0.0f).m13(0.0f).m23(0.0f);

        com.mojang.blaze3d.systems.RenderSystem.getModelViewStack().pushMatrix();
        com.mojang.blaze3d.systems.RenderSystem.getModelViewStack().identity();
        com.mojang.blaze3d.systems.RenderSystem.applyModelViewMatrix();

        PoseStack ps = new PoseStack();
        ps.mulPose(rotOnly);
        renderCelestials(ps, camera, level, partialTick);

        com.mojang.blaze3d.systems.RenderSystem.getModelViewStack().popMatrix();
        com.mojang.blaze3d.systems.RenderSystem.applyModelViewMatrix();
    }

    private void renderCelestials(PoseStack ps, Camera camera, ClientLevel level, float partialTick)
    {
        Optional<AuralithAPI.DimensionBinding> bOpt = AuralithAPI.getBindingForDimension(level.dimension());
        if (bOpt.isEmpty()) return;
        AuralithAPI.DimensionBinding binding = bOpt.get();

        Optional<SolarSystemDefinition> sOpt = AuralithAPI.getSolarSystem(binding.systemId());
        if (sOpt.isEmpty()) return;

        switch (binding.type())
        {
            case SPACE -> renderFromSpace(ps, level, sOpt.get(), partialTick);
            case ORBIT -> renderFromOrbit(ps, camera,  level, sOpt.get(), binding.planetId(), partialTick);
            case SURFACE -> renderFromSurface(ps, level, sOpt.get(), binding.planetId(), partialTick);
        }
    }

    private void renderFromSurface(PoseStack ps, ClientLevel level, SolarSystemDefinition system,  ResourceLocation currentPlanetId, float partialTick)
    {
        long tick = level.getGameTime();

        float raw = ((level.getDayTime() % 24000L) + partialTick) / 24000.0f;
        float skyAngle = raw - 0.25f + 0.5f * (float) Math.sin((raw - 0.25f) * Math.PI * 2.0) / ((float) Math.PI * 2f);

        ps.pushPose();
        ps.mulPose(new Quaternionf().rotationY((float) Math.toRadians(-90.0)));
        ps.mulPose(new Quaternionf().rotationX(skyAngle * (float)(2.0 * Math.PI)));

        renderSun(ps, system.sun(), tick, partialTick);

        PlanetDefinition current = system.planets().stream().filter(p -> p.id().equals(currentPlanetId)).findFirst().orElse(null);
        if (current != null)
        {
            double myAngle  = current.orbit().computeAngle(tick, partialTick);
            double myRadius = current.orbit().computeCurrentRadius(myAngle);
            float[] myPos   = current.orbit().compute3DPosition(myAngle, myRadius, 1.0f);

            for (PlanetDefinition planet : system.planets())
            {
                if (planet.id().equals(currentPlanetId)) continue;

                double theirAngle  = planet.orbit().computeAngle(tick, partialTick);
                double theirRadius = planet.orbit().computeCurrentRadius(theirAngle);
                float[] theirPos   = planet.orbit().compute3DPosition(theirAngle, theirRadius, 1.0f);

                float dx   = myPos[0] - theirPos[0];
                float dy   = myPos[1] - theirPos[1];
                float dz   = myPos[2] - theirPos[2];
                float dist = Math.max((float) Math.sqrt(dx*dx + dy*dy + dz*dz), 0.01f);

                float apparentSize = Math.max(MIN_APPARENT, planet.size() * REF_AU / dist);
                float cdx = dx / dist, cdy = dy / dist, cdz = dz / dist;

                renderPlanetInSky(ps, planet, theirPos, apparentSize, cdx, cdy, cdz, tick, partialTick, true);
            }
        }

        ps.popPose();
    }


    private void renderFromOrbit(PoseStack ps, Camera camera, ClientLevel level, SolarSystemDefinition system, ResourceLocation currentPlanetId, float partialTick)
    {
        long tick = level.getGameTime();

        PlanetDefinition current = system.findById(currentPlanetId).orElse(null);
        if (current == null) return;

        Optional<PlanetDefinition> parentOpt = system.findParent(currentPlanetId);
        boolean isMoon = parentOpt.isPresent();

        float[] myAbsPos;
        if (isMoon)
        {
            PlanetDefinition parent = parentOpt.get();
            double parentAngle = parent.orbit().computeAngle(tick, partialTick);
            double parentRadius = parent.orbit().computeCurrentRadius(parentAngle);
            float[] parentPos = parent.orbit().compute3DPosition(parentAngle, parentRadius, 1.0f);

            double moonAngle = current.orbit().computeAngle(tick, partialTick);
            double moonRadius = current.orbit().computeCurrentRadius(moonAngle);
            float[] moonRelPos = current.orbit().compute3DPosition(moonAngle, moonRadius, 1.0f);

            myAbsPos = new float[]{parentPos[0] + moonRelPos[0], parentPos[1] + moonRelPos[1], parentPos[2] + moonRelPos[2]
            };
        }
        else
        {
            double myAngle  = current.orbit().computeAngle(tick, partialTick);
            double myRadius = current.orbit().computeCurrentRadius(myAngle);
            myAbsPos = current.orbit().compute3DPosition(myAngle, myRadius, 1.0f);
        }

        if (!isMoon) for (PlanetDefinition moon : current.moons()) renderMoonFromOrbit(ps, moon, tick, partialTick);
        if (isMoon)
        {
            PlanetDefinition parent = parentOpt.get();
            double parentAngle = parent.orbit().computeAngle(tick, partialTick);
            double parentRadius = parent.orbit().computeCurrentRadius(parentAngle);
            float[] parentPos = parent.orbit().compute3DPosition(parentAngle, parentRadius, 1.0f);

            double moonAngle = current.orbit().computeAngle(tick, partialTick);
            double moonRadius = current.orbit().computeCurrentRadius(moonAngle);
            float[] moonRel = current.orbit().compute3DPosition(moonAngle, moonRadius, 1.0f);

            float dx = -moonRel[0], dy = -moonRel[1], dz = -moonRel[2];
            float dist = Math.max((float) Math.sqrt(dx*dx + dy*dy + dz*dz), 0.01f) * 8f;
            float apparentSize = Math.max(MIN_APPARENT, parent.size() * REF_AU / dist) * 12f;

            renderPlanetInSky(ps, parent, new float[]{dx, dy, dz}, apparentSize,dx/dist, dy/dist, dz/dist, tick, partialTick, false);
        }

        for (PlanetDefinition planet : system.planets())
        {
            if (planet.id().equals(isMoon ? parentOpt.get().id() : currentPlanetId)) continue;

            double theirAngle  = planet.orbit().computeAngle(tick, partialTick);
            double theirRadius = planet.orbit().computeCurrentRadius(theirAngle);
            float[] theirPos   = planet.orbit().compute3DPosition(theirAngle, theirRadius, 1.0f);

            float dx = theirPos[0] - myAbsPos[0];
            float dy = theirPos[1] - myAbsPos[1];
            float dz = theirPos[2] - myAbsPos[2];
            float dist = Math.max((float) Math.sqrt(dx*dx + dy*dy + dz*dz), 0.01f);

            float apparentSize = Math.max(MIN_APPARENT, planet.size() * REF_AU / dist);
            renderPlanetInSky(ps, planet, new float[]{dx, dy, dz}, apparentSize,-dx/dist, -dy/dist, -dz/dist, tick, partialTick, true);
        }

        double myAbsRadius = Math.sqrt(myAbsPos[0]*myAbsPos[0] + myAbsPos[1]*myAbsPos[1] + myAbsPos[2]*myAbsPos[2]);

        float sdx = -myAbsPos[0];
        float sdy = (float)(myAbsRadius * 0.18);
        float sdz = -myAbsPos[2];
        float slen = (float) Math.sqrt(sdx*sdx + sdy*sdy + sdz*sdz);
        if (slen > 1e-6f)
        {
            float sunApparentScale = (float)(REF_AU / Math.max(myAbsRadius, 0.01)) * 64f;
            renderSunDirectional(ps, system.sun(), tick, partialTick, sdx/slen, sdy/slen, sdz/slen, sunApparentScale);
        }

        Vec3 camPos = camera.getPosition();
        double camLen = camPos.length();
        float nx = 0f, ny = -1f, nz = 0f;
        if (camLen > 0.01)
        {
            nx = (float)(-camPos.x / camLen);
            ny = (float)(-camPos.y / camLen);
            nz = (float)(-camPos.z / camLen);
        }

        ps.pushPose();
        ps.translate(nx * SKY_RADIUS, ny * SKY_RADIUS, nz * SKY_RADIUS);
        poseAxialRotation(ps, current.axialRotationSpeed(), tick, partialTick);
        ps.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(current.axialTilt())));
        current.atmosphere().ifPresent(atmo ->
        {
          ps.pushPose();
          float as = ORBIT_PLANET_SIZE * atmo.scale();
          ps.scale(as, as, as);
          AtmosphereRenderer.render(ps, atmo, 0f, 1f, 0f);
          ps.popPose();
        });
        current.rings().ifPresent(rings -> renderRings(ps, rings, ORBIT_PLANET_SIZE));
        ps.pushPose();
        ps.scale(ORBIT_PLANET_SIZE, ORBIT_PLANET_SIZE, ORBIT_PLANET_SIZE);
        renderTextureCube(ps, current.resolveTexture());
        ps.popPose();
        ps.popPose();
    }

    private void renderMoonFromOrbit(PoseStack ps, PlanetDefinition moon, long tick, float partialTick)
    {
        double angle = moon.orbit().computeAngle(tick, partialTick);
        double radius = moon.orbit().computeCurrentRadius(angle);
        float skyScale = SKY_RADIUS * 0.8f;
        float[] pos = moon.orbit().compute3DPosition(angle, radius, skyScale);

        float len = (float) Math.sqrt(pos[0]*pos[0] + pos[1]*pos[1] + pos[2]*pos[2]);
        if (len < 1e-6f) return;

        float sx = pos[0] / len * SKY_RADIUS;
        float sy = pos[1] / len * SKY_RADIUS;
        float sz = pos[2] / len * SKY_RADIUS;
        float cdx = -pos[0]/len, cdy = -pos[1]/len, cdz = -pos[2]/len;
        float moonSize = Math.max(moon.size() * 0.8f, MIN_APPARENT);

        ps.pushPose();
        ps.translate(sx, sy, sz);
        poseAxialRotation(ps, moon.axialRotationSpeed(), tick, partialTick);
        ps.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(moon.axialTilt())));

        moon.atmosphere().ifPresent(atmo ->
        {
            ps.pushPose();
            float as = moonSize * atmo.scale();
            ps.scale(as, as, as);
            AtmosphereRenderer.render(ps, atmo, cdx, cdy, cdz);
            ps.popPose();
        });

        ps.pushPose();
        ps.scale(moonSize, moonSize, moonSize);
        renderTextureCube(ps, moon.resolveTexture());
        ps.popPose();

        ps.popPose();
    }

    private void renderFromSpace(PoseStack ps, ClientLevel level, SolarSystemDefinition system, float partialTick)
    {
        long tick = level.getGameTime();

        float sunAzimuth = (tick + partialTick) / 240000.0f * (float)(2.0 * Math.PI);
        float elRad = (float) Math.toRadians(30f);
        float sunNX = (float)(Math.cos(elRad) * Math.cos(sunAzimuth));
        float sunNY = (float) Math.sin(elRad);
        float sunNZ = (float)(Math.cos(elRad) * Math.sin(sunAzimuth));
        renderSunDirectional(ps, system.sun(), tick, partialTick, sunNX, sunNY, sunNZ);

        for (PlanetDefinition planet : system.planets())
        {
            double angle = planet.orbit().computeAngle(tick, partialTick);
            double radius = planet.orbit().computeCurrentRadius(angle);
            float[] pos = planet.orbit().compute3DPosition(angle, radius, 1.0f);

            float len = (float) Math.sqrt(pos[0]*pos[0] + pos[1]*pos[1] + pos[2]*pos[2]);
            if (len < 1e-6f) continue;

            float sx = pos[0] / len * SKY_RADIUS;
            float sy = pos[1] / len * SKY_RADIUS;
            float sz = pos[2] / len * SKY_RADIUS;

            float apparentSize = Math.max(MIN_APPARENT, planet.size() * 0.5f / (float) radius);
            float cdx = pos[0]/len, cdy = pos[1]/len, cdz = pos[2]/len;

            ps.pushPose();
            ps.translate(sx, sy, sz);
            poseAxialRotation(ps, planet.axialRotationSpeed(), tick, partialTick);
            ps.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(planet.axialTilt())));

            planet.atmosphere().ifPresent(atmo ->
            {
                ps.pushPose();
                float as = apparentSize * atmo.scale();
                ps.scale(as, as, as);
                AtmosphereRenderer.render(ps, atmo, cdx, cdy, cdz);
                ps.popPose();
            });
            planet.rings().ifPresent(rings -> renderRings(ps, rings, apparentSize));

            ps.pushPose();
            ps.scale(apparentSize, apparentSize, apparentSize);
            renderTextureCube(ps, planet.resolveTexture());
            ps.popPose();

            for (PlanetDefinition moon : planet.moons())
                renderMoon(ps, moon, apparentSize, tick, partialTick);

            ps.popPose();
        }
    }

    private void renderPlanetInSky(PoseStack ps, PlanetDefinition planet, float[] dirPos, float apparentSize, float cdx, float cdy, float cdz, long tick, float partialTick, boolean renderMoon)
    {
        float len = (float) Math.sqrt(dirPos[0]*dirPos[0] + dirPos[1]*dirPos[1] + dirPos[2]*dirPos[2]);
        if (len < 1e-6f) return;

        float sx = dirPos[0] / len * SKY_RADIUS;
        float sy = dirPos[1] / len * SKY_RADIUS;
        float sz = dirPos[2] / len * SKY_RADIUS;

        ps.pushPose();
        ps.translate(sx, sy, sz);

        float tiltRad    = (float) Math.toRadians(planet.axialTilt());
        float axialAngle = ((tick + partialTick) * planet.axialRotationSpeed() * 0.001f) % (float)(2.0 * Math.PI);

        Quaternionf invRot = new Quaternionf().rotationY(axialAngle).mul(new Quaternionf().rotationZ(tiltRad)).conjugate();
        Vector3f localCam = new Vector3f(cdx, cdy, cdz);
        invRot.transform(localCam);

        poseAxialRotation(ps, planet.axialRotationSpeed(), tick, partialTick);
        ps.mulPose(new Quaternionf().rotationZ(tiltRad));

        planet.rings().ifPresent(rings -> renderRings(ps, rings, apparentSize, localCam.x, localCam.y, localCam.z, false));

        planet.atmosphere().ifPresent(atmo ->
        {
            ps.pushPose();
            float as = apparentSize * atmo.scale();
            ps.scale(as, as, as);
            AtmosphereRenderer.render(ps, atmo, cdx, cdy, cdz);
            ps.popPose();
        });

        ps.pushPose();
        ps.scale(apparentSize, apparentSize, apparentSize);
        renderTextureCube(ps, planet.resolveTexture());
        ps.popPose();

        planet.rings().ifPresent(rings -> renderRings(ps, rings, apparentSize, localCam.x, localCam.y, localCam.z, true));
        if (renderMoon) for (PlanetDefinition moon : planet.moons()) renderMoon(ps, moon, apparentSize, tick, partialTick);

        ps.popPose();
    }

    private void renderMoon(PoseStack ps, PlanetDefinition moon, float parentApparentSize, long tick, float partialTick)
    {
        double angle = moon.orbit().computeAngle(tick, partialTick);
        double radius = moon.orbit().computeCurrentRadius(angle);
        float  moonScale = parentApparentSize * 2.5f;
        float[] pos = moon.orbit().compute3DPosition(angle, radius, moonScale);

        float cdLen = (float) Math.sqrt(pos[0]*pos[0] + pos[1]*pos[1] + pos[2]*pos[2]);
        float cdx = (cdLen > 1e-6f) ? -pos[0]/cdLen : 0f;
        float cdy = (cdLen > 1e-6f) ? -pos[1]/cdLen : -1f;
        float cdz = (cdLen > 1e-6f) ? -pos[2]/cdLen : 0f;

        float moonSize = Math.max(moon.size() * parentApparentSize * 0.05f, MIN_APPARENT * 0.3f);

        ps.pushPose();
        ps.translate(pos[0], pos[1], pos[2]);
        poseAxialRotation(ps, moon.axialRotationSpeed(), tick, partialTick);
        ps.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(moon.axialTilt())));

        moon.atmosphere().ifPresent(atmo ->
        {
            ps.pushPose();
            float as = moonSize * atmo.scale();
            ps.scale(as, as, as);
            AtmosphereRenderer.render(ps, atmo, cdx, cdy, cdz);
            ps.popPose();
        });

        ps.pushPose();
        ps.scale(moonSize, moonSize, moonSize);
        renderTextureCube(ps, moon.resolveTexture());
        ps.popPose();

        ps.popPose();
    }

    private void renderSun(PoseStack ps, SunDefinition sun, long tick, float partialTick)
    {
        ps.pushPose();
        ps.translate(0.0f, SKY_RADIUS, 0.0f);
        poseAxialRotation(ps, sun.axialRotationSpeed(), tick, partialTick);
        if (sun.glowLayers() > 0) renderSunGlow(ps, sun);
        float s = sun.size();
        ps.popPose();
    }

    private void renderSunDirectional(PoseStack ps, SunDefinition sun, long tick, float partialTick, float nx, float ny, float nz)
    {
        ps.pushPose();
        ps.translate(nx * SKY_RADIUS, ny * SKY_RADIUS, nz * SKY_RADIUS);
        poseAxialRotation(ps, sun.axialRotationSpeed(), tick, partialTick);
        if (sun.glowLayers() > 0) renderSunGlow(ps, sun);
        ps.popPose();
    }

    private void renderSunGlow(PoseStack ps, SunDefinition sun)
    {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
        RenderSystem.disableCull();

        float r = sun.glowR(), g = sun.glowG(), b = sun.glowB();
        int layers = sun.glowLayers();
        float coreSize = sun.size();
        float glowScale = sun.glowScale();

        for (int i = layers; i > 0; i--)
        {
            float t = (float) i / layers;
            float layerSize = coreSize * (1.0f + t * (glowScale - 1.0f));
            float alpha = (1.0f - t) * 0.06f;
            ps.pushPose();
            ps.scale(layerSize, layerSize, layerSize);
            renderColoredCube(ps, r, g, b, alpha);
            ps.popPose();
        }

        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
    }

    private void renderSunDirectional(PoseStack ps, SunDefinition sun, long tick, float partialTick, float nx, float ny, float nz, float apparentScale)
    {
        ps.pushPose();
        ps.translate(nx * SKY_RADIUS, ny * SKY_RADIUS, nz * SKY_RADIUS);
        poseAxialRotation(ps, sun.axialRotationSpeed(), tick, partialTick);
        if (sun.glowLayers() > 0) renderSunGlow(ps, sun, apparentScale);
        ps.popPose();
    }

    private void renderSunGlow(PoseStack ps, SunDefinition sun, float apparentScale)
    {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
        RenderSystem.disableCull();

        float r = sun.glowR(), g = sun.glowG(), b = sun.glowB();
        int layers = sun.glowLayers();
        float coreSize  = sun.size() * apparentScale;
        float glowScale = sun.glowScale();

        for (int i = layers; i > 0; i--)
        {
            float t = (float) i / layers;
            float layerSize = coreSize * (1.0f + t * (glowScale - 1.0f));
            float alpha = (1.0f - t) * 0.06f;
            ps.pushPose();
            ps.scale(layerSize, layerSize, layerSize);
            renderColoredCube(ps, r, g, b, alpha);
            ps.popPose();
        }

        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
    }

    private void renderRings(PoseStack ps, RingDefinition rings, float apparentSize)
    {
        setupRingRenderState();
        Random rng = new Random(rings.seed());
        Tesselator tess = Tesselator.getInstance();
        VertexFormat fmt = ShaderHelper.shadersActive() ? DefaultVertexFormat.NEW_ENTITY : DefaultVertexFormat.POSITION_TEX_COLOR;
        BufferBuilder buf = tess.begin(VertexFormat.Mode.QUADS, fmt);
        float planetRadius = apparentSize * 0.5f;
        if(ShaderHelper.shadersActive()) emitRingQuads(buf, ps, rings.innerRadius() * planetRadius,rings.outerRadius() * planetRadius, Math.max(1, rings.ringCount()), rings, rng);
        else
        {
            Matrix4f m = ps.last().pose();
            emitRingQuads(buf, m, rings.innerRadius() * planetRadius, rings.outerRadius() * planetRadius, Math.max(1, rings.ringCount()), rings, rng);
        }
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

        int light   = net.minecraft.client.renderer.LightTexture.FULL_BRIGHT;
        int overlay = net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;

        Random rng = new Random(rings.seed());
        Tesselator tess = Tesselator.getInstance();
        VertexFormat fmt = ShaderHelper.shadersActive() ? DefaultVertexFormat.NEW_ENTITY : DefaultVertexFormat.POSITION_TEX_COLOR;
        BufferBuilder buf = tess.begin(VertexFormat.Mode.QUADS, fmt);
        Matrix4f m = ps.last().pose();

        for (int ri = 0; ri < count; ri++)
        {
            float r0 = lerp(innerR, outerR, (float) ri / count);
            float r1 = lerp(innerR, outerR, (float)(ri + 1)  / count);
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

                for (float[] v : quads[qi])
                    buf.addVertex(m, v[0], v[1], v[2]).setColor(cr, cg, cb, ca).setUv(0f, 0f).setOverlay(overlay).setLight(light).setNormal(ps.last(), 0, 1, 0);
            }
        }
        BufferUploader.drawWithShader(buf.buildOrThrow());
        teardownRingRenderState();
    }

    private void setupRingRenderState()
    {
        RenderSystem.setShader(ShaderHelper.shadersActive() ? GameRenderer::getRendertypeEntitySolidShader : GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, RingTextureHelper.getWhite());
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
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
            float r0 = lerp(innerR, outerR, (float) ri / count);
            float r1 = lerp(innerR, outerR, (float)(ri + 1) / count);
            float rndT = rng.nextFloat();
            float cr = lerp(rings.minR(), rings.maxR(), rndT);
            float cg = lerp(rings.minG(), rings.maxG(), rndT);
            float cb = lerp(rings.minB(), rings.maxB(), rndT);
            float ca = rings.opacity() * (0.6f + 0.4f * rng.nextFloat());

            buf.addVertex(m,-r0,0,-r1).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, r0,0,-r1).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, r0,0,-r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,-r0,0,-r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,-r0,0, r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, r0,0, r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, r0,0, r1).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,-r0,0, r1).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,-r1,0,-r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,-r0,0,-r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,-r0,0, r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,-r1,0, r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, r0,0,-r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, r1,0,-r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, r1,0, r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, r0,0, r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,-r1,0,-r1).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,-r0,0,-r1).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,-r0,0,-r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,-r1,0,-r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, r0,0,-r1).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, r1,0,-r1).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, r1,0,-r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, r0,0,-r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,-r1,0, r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,-r0,0, r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,-r0,0, r1).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m,-r1,0, r1).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, r0,0, r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, r1,0, r0).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, r1,0, r1).setUv(0,0).setColor(cr,cg,cb,ca);
            buf.addVertex(m, r0,0, r1).setUv(0,0).setColor(cr,cg,cb,ca);
        }
    }

    private void emitRingQuads(BufferBuilder buf, PoseStack ps, float innerR, float outerR, int count, RingDefinition rings, Random rng)
    {
        int light   = net.minecraft.client.renderer.LightTexture.FULL_BRIGHT;
        int overlay = net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;
        Matrix4f m  = ps.last().pose();

        for (int ri = 0; ri < count; ri++)
        {
            float r0   = lerp(innerR, outerR, (float) ri      / count);
            float r1   = lerp(innerR, outerR, (float)(ri + 1) / count);
            float rndT = rng.nextFloat();
            float cr   = lerp(rings.minR(), rings.maxR(), rndT);
            float cg   = lerp(rings.minG(), rings.maxG(), rndT);
            float cb   = lerp(rings.minB(), rings.maxB(), rndT);
            float ca   = rings.opacity() * (0.6f + 0.4f * rng.nextFloat());

            float[][] verts =
            {
                {-r0,0,-r1}, { r0,0,-r1}, { r0,0,-r0}, {-r0,0,-r0},
                {-r0,0, r0}, { r0,0, r0}, { r0,0, r1}, {-r0,0, r1},
                {-r1,0,-r0}, {-r0,0,-r0}, {-r0,0, r0}, {-r1,0, r0},
                { r0,0,-r0}, { r1,0,-r0}, { r1,0, r0}, { r0,0, r0},
                {-r1,0,-r1}, {-r0,0,-r1}, {-r0,0,-r0}, {-r1,0,-r0},
                { r0,0,-r1}, { r1,0,-r1}, { r1,0,-r0}, { r0,0,-r0},
                {-r1,0, r0}, {-r0,0, r0}, {-r0,0, r1}, {-r1,0, r1},
                { r0,0, r0}, { r1,0, r0}, { r1,0, r1}, { r0,0, r1},
            };

            for (float[] v : verts)
            {
                if (ShaderHelper.shadersActive()) buf.addVertex(m, v[0], v[1], v[2]).setColor(cr,cg,cb,ca).setUv(0,0).setOverlay(overlay).setLight(light).setNormal(ps.last(), 0,1,0);
                else buf.addVertex(m, v[0], v[1], v[2]).setUv(0,0).setColor(cr,cg,cb,ca);
            }
        }
    }

    private void renderTextureCube(PoseStack ps, ResourceLocation texture)
    {
        if(ShaderHelper.shadersActive()) RenderSystem.setShader(GameRenderer::getRendertypeEntitySolidShader);
        else RenderSystem.setShader(GameRenderer::getPositionTexShader);

        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf;
        if(ShaderHelper.shadersActive()) buf = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
        else buf = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        Matrix4f m = ps.last().pose();
        float h = 0.5f;

        int light = net.minecraft.client.renderer.LightTexture.FULL_BRIGHT;
        int overlay = net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;

        buf.addVertex(m,-h, h, h).setColor(1f,1f,1f,1f).setUv(0,1).setOverlay(overlay).setLight(light).setNormal(ps.last(), 0, 1, 0);
        buf.addVertex(m, h, h, h).setColor(1f,1f,1f,1f).setUv(1,1).setOverlay(overlay).setLight(light).setNormal(ps.last(), 0, 1, 0);
        buf.addVertex(m, h, h,-h).setColor(1f,1f,1f,1f).setUv(1,0).setOverlay(overlay).setLight(light).setNormal(ps.last(), 0, 1, 0);
        buf.addVertex(m,-h, h,-h).setColor(1f,1f,1f,1f).setUv(0,0).setOverlay(overlay).setLight(light).setNormal(ps.last(), 0, 1, 0);

        buf.addVertex(m,-h,-h,-h).setColor(1f,1f,1f,1f).setUv(0,0).setOverlay(overlay).setLight(light).setNormal(ps.last(), 0,-1, 0);
        buf.addVertex(m, h,-h,-h).setColor(1f,1f,1f,1f).setUv(1,0).setOverlay(overlay).setLight(light).setNormal(ps.last(), 0,-1, 0);
        buf.addVertex(m, h,-h, h).setColor(1f,1f,1f,1f).setUv(1,1).setOverlay(overlay).setLight(light).setNormal(ps.last(), 0,-1, 0);
        buf.addVertex(m,-h,-h, h).setColor(1f,1f,1f,1f).setUv(0,1).setOverlay(overlay).setLight(light).setNormal(ps.last(), 0,-1, 0);

        buf.addVertex(m, h, h, h).setColor(1f,1f,1f,1f).setUv(0,0).setOverlay(overlay).setLight(light).setNormal(ps.last(), 0, 0, 1);
        buf.addVertex(m,-h, h, h).setColor(1f,1f,1f,1f).setUv(1,0).setOverlay(overlay).setLight(light).setNormal(ps.last(), 0, 0, 1);
        buf.addVertex(m,-h,-h, h).setColor(1f,1f,1f,1f).setUv(1,1).setOverlay(overlay).setLight(light).setNormal(ps.last(), 0, 0, 1);
        buf.addVertex(m, h,-h, h).setColor(1f,1f,1f,1f).setUv(0,1).setOverlay(overlay).setLight(light).setNormal(ps.last(), 0, 0, 1);

        buf.addVertex(m,-h, h,-h).setColor(1f,1f,1f,1f).setUv(0,0).setOverlay(overlay).setLight(light).setNormal(ps.last(), 0, 0,-1);
        buf.addVertex(m, h, h,-h).setColor(1f,1f,1f,1f).setUv(1,0).setOverlay(overlay).setLight(light).setNormal(ps.last(), 0, 0,-1);
        buf.addVertex(m, h,-h,-h).setColor(1f,1f,1f,1f).setUv(1,1).setOverlay(overlay).setLight(light).setNormal(ps.last(), 0, 0,-1);
        buf.addVertex(m,-h,-h,-h).setColor(1f,1f,1f,1f).setUv(0,1).setOverlay(overlay).setLight(light).setNormal(ps.last(), 0, 0,-1);

        buf.addVertex(m, h, h,-h).setColor(1f,1f,1f,1f).setUv(0,0).setOverlay(overlay).setLight(light).setNormal(ps.last(), 1, 0, 0);
        buf.addVertex(m, h, h, h).setColor(1f,1f,1f,1f).setUv(1,0).setOverlay(overlay).setLight(light).setNormal(ps.last(), 1, 0, 0);
        buf.addVertex(m, h,-h, h).setColor(1f,1f,1f,1f).setUv(1,1).setOverlay(overlay).setLight(light).setNormal(ps.last(), 1, 0, 0);
        buf.addVertex(m, h,-h,-h).setColor(1f,1f,1f,1f).setUv(0,1).setOverlay(overlay).setLight(light).setNormal(ps.last(), 1, 0, 0);

        buf.addVertex(m,-h, h, h).setColor(1f,1f,1f,1f).setUv(0,0).setOverlay(overlay).setLight(light).setNormal(ps.last(),-1, 0, 0);
        buf.addVertex(m,-h, h,-h).setColor(1f,1f,1f,1f).setUv(1,0).setOverlay(overlay).setLight(light).setNormal(ps.last(),-1, 0, 0);
        buf.addVertex(m,-h,-h,-h).setColor(1f,1f,1f,1f).setUv(1,1).setOverlay(overlay).setLight(light).setNormal(ps.last(),-1, 0, 0);
        buf.addVertex(m,-h,-h, h).setColor(1f,1f,1f,1f).setUv(0,1).setOverlay(overlay).setLight(light).setNormal(ps.last(),-1, 0, 0);

        BufferUploader.drawWithShader(buf.buildOrThrow());

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.depthMask(true);
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
