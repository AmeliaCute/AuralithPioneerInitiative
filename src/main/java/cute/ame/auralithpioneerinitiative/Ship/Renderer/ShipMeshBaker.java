package cute.ame.auralithpioneerinitiative.Ship.Renderer;

import com.mojang.blaze3d.vertex.*;
import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Ship.Network.ShipSnapshotPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@OnlyIn(Dist.CLIENT)
public final class ShipMeshBaker
{
    private ShipMeshBaker() {}

    private static final Executor BAKER_THREAD = Executors.newSingleThreadExecutor(r -> {Thread t = new Thread(r, "auralith-ship-baker");t.setDaemon(true);return t;});

    private static final int BYTES_PER_QUAD  = 128;
    private static final int QUADS_PER_BLOCK = 6;

    public static void bakeAsync(UUID uuid, List<ShipSnapshotPacket.BlockEntry> blocks)
    {
        if (blocks.isEmpty())
        {
            Auralithpioneerinitiative.LOGGER.warn("[Auralith] ShipMeshBaker: snapshot is empty for ship {}", uuid);
            ShipClientCache.submitUpload(uuid, () -> ShipClientCache.storeMesh(uuid, new BakedShipMesh(null, null, null, AABB.ofSize(Vec3.ZERO, 0, 0, 1), 0)));
            return;
        }

        int capacityEstimate = blocks.size() * QUADS_PER_BLOCK * BYTES_PER_QUAD;

        CompletableFuture.runAsync(() ->
        {
            Set<Long> posSet = buildPosSet(blocks);

            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
            for (var b : blocks)
            {
                minX = Math.min(minX, b.relX()); maxX = Math.max(maxX, b.relX());
                minY = Math.min(minY, b.relY()); maxY = Math.max(maxY, b.relY());
                minZ = Math.min(minZ, b.relZ()); maxZ = Math.max(maxZ, b.relZ());
            }
            final AABB localBounds = new AABB(minX, minY, minZ, maxX + 1.0, maxY + 1.0, maxZ + 1.0);
            final int  blockCount  = blocks.size();

            @Nullable MeshData shaderMesh = bakeShader(blocks, posSet, capacityEstimate);
            @Nullable MeshData vanillaMesh = bakeVanilla(blocks, posSet, capacityEstimate);
            @Nullable MeshData hullMesh = bakeHullBox(localBounds);

            ShipClientCache.submitUpload(uuid, () ->
            {
                @Nullable VertexBuffer shaderVbo = uploadMesh(shaderMesh);
                @Nullable VertexBuffer vanillaVbo = uploadMesh(vanillaMesh);
                @Nullable VertexBuffer hullVbo = uploadMesh(hullMesh);

                ShipClientCache.storeMesh(uuid, new BakedShipMesh(shaderVbo, vanillaVbo, hullVbo, localBounds, blockCount));

                Auralithpioneerinitiative.LOGGER.info("[Auralith] Ship mesh baked: {} blocks for ship {}", blockCount, uuid);
            });

        }, BAKER_THREAD).exceptionally(ex ->
        {
            Auralithpioneerinitiative.LOGGER.error("[Auralith] ShipMeshBaker failed for ship {}", uuid, ex);
            return null;
        });
    }

    private static @Nullable MeshData bakeShader(List<ShipSnapshotPacket.BlockEntry> blocks, Set<Long> posSet, int capacityEstimate)
    {
        BlockRenderDispatcher brd = Minecraft.getInstance().getBlockRenderer();
        RandomSource random = RandomSource.createNewThreadLocalInstance();

        ByteBufferBuilder storage = new ByteBufferBuilder(capacityEstimate);
        BufferBuilder builder = new BufferBuilder(storage, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        boolean anyQuads = false;

        for (ShipSnapshotPacket.BlockEntry entry : blocks)
        {
            BlockState state = entry.resolveState();
            if (state.isAir()) continue;

            var   model = brd.getBlockModel(state);
            float ox = entry.relX(), oy = entry.relY(), oz = entry.relZ();

            for (Direction dir : Direction.values())
            {
                if (posSet.contains(packPos(entry.relX() + dir.getStepX(), entry.relY() + dir.getStepY(), entry.relZ() + dir.getStepZ()))) continue;

                for (BakedQuad quad : model.getQuads(state, dir, random))
                {
                    emitQuadBlock(builder, quad, ox, oy, oz, dir);
                    anyQuads = true;
                }
            }
            for (BakedQuad quad : model.getQuads(state, null, random))
            {
                emitQuadBlock(builder, quad, ox, oy, oz, Direction.UP);
                anyQuads = true;
            }
        }

        if (!anyQuads) { storage.close(); return null; }
        return builder.build();
    }

    private static void emitQuadBlock(BufferBuilder builder, BakedQuad quad, float ox, float oy, float oz, Direction face)
    {
        int[] vd = quad.getVertices();
        float nx = face.getStepX(), ny = face.getStepY(), nz = face.getStepZ();

        for (int i = 0; i < 4; i++)
        {
            int base = i * 8;
            float x = Float.intBitsToFloat(vd[base    ]) + ox;
            float y = Float.intBitsToFloat(vd[base + 1]) + oy;
            float z = Float.intBitsToFloat(vd[base + 2]) + oz;
            int   argb = vd[base + 3];
            float u = Float.intBitsToFloat(vd[base + 4]);
            float v = Float.intBitsToFloat(vd[base + 5]);

            int qa = (argb >> 24) & 0xFF;
            int qr = (argb >> 16) & 0xFF;
            int qg = (argb >>  8) & 0xFF;
            int qb =  argb & 0xFF;

            builder.addVertex(x, y, z).setColor(qr, qg, qb, qa).setUv(u, v).setUv2(240, 240).setNormal(nx, ny, nz);
        }
    }

    private static @Nullable MeshData bakeVanilla(List<ShipSnapshotPacket.BlockEntry> blocks, Set<Long> posSet, int capacityEstimate)
    {
        BlockRenderDispatcher brd = Minecraft.getInstance().getBlockRenderer();
        RandomSource random = RandomSource.createNewThreadLocalInstance();

        ByteBufferBuilder storage = new ByteBufferBuilder(capacityEstimate);
        BufferBuilder builder = new BufferBuilder(storage, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        boolean anyQuads = false;

        for (ShipSnapshotPacket.BlockEntry entry : blocks)
        {
            BlockState state = entry.resolveState();
            if (state.isAir()) continue;

            var   model = brd.getBlockModel(state);
            float ox = entry.relX(), oy = entry.relY(), oz = entry.relZ();

            for (Direction dir : Direction.values())
            {
                if (posSet.contains(packPos(entry.relX() + dir.getStepX(), entry.relY() + dir.getStepY(), entry.relZ() + dir.getStepZ()))) continue;

                for (BakedQuad quad : model.getQuads(state, dir, random))
                {
                    emitQuadVanilla(builder, quad, ox, oy, oz);
                    anyQuads = true;
                }
            }
            for (BakedQuad quad : model.getQuads(state, null, random))
            {
                emitQuadVanilla(builder, quad, ox, oy, oz);
                anyQuads = true;
            }
        }

        if (!anyQuads) { storage.close(); return null; }
        return builder.build();
    }

    private static void emitQuadVanilla(BufferBuilder builder, BakedQuad quad, float ox, float oy, float oz)
    {
        int[] vd = quad.getVertices();

        for (int i = 0; i < 4; i++)
        {
            int   base = i * 8;
            float x = Float.intBitsToFloat(vd[base ]) + ox;
            float y = Float.intBitsToFloat(vd[base + 1]) + oy;
            float z = Float.intBitsToFloat(vd[base + 2]) + oz;
            float u = Float.intBitsToFloat(vd[base + 4]);
            float v = Float.intBitsToFloat(vd[base + 5]);

            builder.addVertex(x, y, z).setUv(u, v).setColor(255, 255, 255, 255);
        }
    }

    private static @Nullable MeshData bakeHullBox(AABB box)
    {
        ByteBufferBuilder storage = new ByteBufferBuilder(12 * BYTES_PER_QUAD);
        BufferBuilder builder = new BufferBuilder(storage, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float x0 = (float) box.minX, y0 = (float) box.minY, z0 = (float) box.minZ;
        float x1 = (float) box.maxX, y1 = (float) box.maxY, z1 = (float) box.maxZ;
        int r = 120, g = 140, b = 160, a = 200;

        builder.addVertex(x0,y0,z0).setColor(r,g,b,a);
        builder.addVertex(x1,y0,z0).setColor(r,g,b,a);
        builder.addVertex(x1,y0,z1).setColor(r,g,b,a);
        builder.addVertex(x0,y0,z1).setColor(r,g,b,a);
        // +Y face
        builder.addVertex(x0,y1,z0).setColor(r,g,b,a);
        builder.addVertex(x0,y1,z1).setColor(r,g,b,a);
        builder.addVertex(x1,y1,z1).setColor(r,g,b,a);
        builder.addVertex(x1,y1,z0).setColor(r,g,b,a);
        // -Z face
        builder.addVertex(x0,y0,z0).setColor(r,g,b,a);
        builder.addVertex(x0,y1,z0).setColor(r,g,b,a);
        builder.addVertex(x1,y1,z0).setColor(r,g,b,a);
        builder.addVertex(x1,y0,z0).setColor(r,g,b,a);
        // +Z face
        builder.addVertex(x0,y0,z1).setColor(r,g,b,a);
        builder.addVertex(x1,y0,z1).setColor(r,g,b,a);
        builder.addVertex(x1,y1,z1).setColor(r,g,b,a);
        builder.addVertex(x0,y1,z1).setColor(r,g,b,a);
        // -X face
        builder.addVertex(x0,y0,z0).setColor(r,g,b,a);
        builder.addVertex(x0,y0,z1).setColor(r,g,b,a);
        builder.addVertex(x0,y1,z1).setColor(r,g,b,a);
        builder.addVertex(x0,y1,z0).setColor(r,g,b,a);
        // +X face
        builder.addVertex(x1,y0,z0).setColor(r,g,b,a);
        builder.addVertex(x1,y1,z0).setColor(r,g,b,a);
        builder.addVertex(x1,y1,z1).setColor(r,g,b,a);
        builder.addVertex(x1,y0,z1).setColor(r,g,b,a);

        return builder.build();
    }

    private static @Nullable VertexBuffer uploadMesh(@Nullable MeshData mesh)
    {
        if (mesh == null) return null;
        VertexBuffer vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
        vbo.bind();
        vbo.upload(mesh);
        VertexBuffer.unbind();
        mesh.close();
        return vbo;
    }

    private static Set<Long> buildPosSet(List<ShipSnapshotPacket.BlockEntry> blocks)
    {
        Set<Long> set = new HashSet<>(blocks.size() * 2);
        for (var b : blocks) set.add(packPos(b.relX(), b.relY(), b.relZ()));
        return set;
    }

    private static long packPos(int x, int y, int z)
    {
        return ((long)(x + 2048) & 0xFFFF) | (((long)(y + 2048) & 0xFFFF) << 16) | (((long)(z + 2048) & 0xFFFF) << 32);
    }
}