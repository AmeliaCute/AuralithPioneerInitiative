package cute.ame.auralithpioneerinitiative.vehicle.Baking;

import com.mojang.blaze3d.vertex.*;
import cute.ame.auralithpioneerinitiative.vehicle.Client.VehicleClientCache;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@OnlyIn(Dist.CLIENT)
public final class VehicleMeshBaker
{
  private VehicleMeshBaker() {}

  private static final Logger LOGGER = LoggerFactory.getLogger("auralith-baker");

  private static final Executor BAKER_THREAD = Executors.newSingleThreadExecutor(r ->
  {
    Thread t = new Thread(r, "auralith-vehicle-baker");
    t.setDaemon(true);
    return t;
  });

  private static final int BYTES_PER_QUAD = 128;
  private static final int QUADS_PER_BLOCK = 6;

  public static void bakeAsync(UUID uuid, VehicleSnapshot snapshot)
  {
    if (snapshot.isEmpty())
    {
      VehicleClientCache.submitUpload(uuid, () ->
          VehicleClientCache.storeMesh(uuid, new BakedVehicleMesh(
              null, null, null,
              AABB.ofSize(Vec3.ZERO, 1, 1, 1), 0
          ))
      );
      return;
    }

    final BlockRenderDispatcher brd = Minecraft.getInstance().getBlockRenderer();
    final int capacityEstimate = snapshot.blockCount() * QUADS_PER_BLOCK * BYTES_PER_QUAD;
    final AABB localBounds = new AABB(snapshot.minX - 0.5, snapshot.minY - 0.5, snapshot.minZ - 0.5, snapshot.maxX + 1.5, snapshot.maxY + 1.5, snapshot.maxZ + 1.5);

    CompletableFuture.runAsync(() ->
    {
      try
      {
        Set<Long> posSet = buildPosSet(snapshot.blocks());

        MeshData[] results = bakeBoth(snapshot.blocks(), posSet, capacityEstimate, brd);
        MeshData shaderMesh = results[0];
        MeshData vanillaMesh = results[1];
        MeshData hullMesh = bakeHullBox(localBounds);

        int blockCount = snapshot.blockCount();
        VehicleClientCache.submitUpload(uuid, () ->
        {
          VertexBuffer shaderVbo = uploadMesh(shaderMesh);
          VertexBuffer vanillaVbo = uploadMesh(vanillaMesh);
          VertexBuffer hullVbo = uploadMesh(hullMesh);

          VehicleClientCache.storeMesh(uuid, new BakedVehicleMesh(shaderVbo, vanillaVbo, hullVbo, localBounds, blockCount));
        });
      }
      catch (Exception ex)
      {
        LOGGER.error("[Pioneer] Baking failed for vehicle: {}", uuid, ex);
      }
    }, BAKER_THREAD);
  }

  private static MeshData[] bakeBoth(List<VehicleSnapshot.BlockEntry> blocks, Set<Long> posSet, int capacityEstimate, BlockRenderDispatcher brd)
  {
    RandomSource random = RandomSource.createNewThreadLocalInstance();
    ByteBufferBuilder sBuf = new ByteBufferBuilder(capacityEstimate);
    ByteBufferBuilder vBuf = new ByteBufferBuilder(capacityEstimate);
    BufferBuilder sBuilder = new BufferBuilder(sBuf, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
    BufferBuilder vBuilder = new BufferBuilder(vBuf, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

    boolean anyShader = false;
    boolean anyVanilla = false;

    for (VehicleSnapshot.BlockEntry entry : blocks)
    {
      BlockState state = entry.resolveState();
      if (state.isAir()) continue;

      var model = brd.getBlockModel(state);
      float ox = entry.relX(), oy = entry.relY(), oz = entry.relZ();

      for (Direction dir : Direction.values())
      {
        long neighborKey = VehicleSnapshot.packPos(entry.relX() + dir.getStepX(), entry.relY() + dir.getStepY(), entry.relZ() + dir.getStepZ());
        if (posSet.contains(neighborKey)) continue;

        List<BakedQuad> quads = model.getQuads(state, dir, random);
        for (BakedQuad quad : quads)
        {
          emitQuadShader(sBuilder, quad, ox, oy, oz, dir);
          emitQuadVanilla(vBuilder, quad, ox, oy, oz);
          anyShader = anyVanilla = true;
        }
      }

      List<BakedQuad> nullQuads = model.getQuads(state, null, random);
      for (BakedQuad quad : nullQuads)
      {
        emitQuadShader(sBuilder, quad, ox, oy, oz, Direction.UP);
        emitQuadVanilla(vBuilder, quad, ox, oy, oz);
        anyShader = anyVanilla = true;
      }
    }

    MeshData shaderMesh = null;
    MeshData vanillaMesh = null;
    if (anyShader) shaderMesh = sBuilder.build(); else sBuf.close();
    if (anyVanilla) vanillaMesh = vBuilder.build(); else vBuf.close();

    return new MeshData[]{ shaderMesh, vanillaMesh };
  }

  private static @Nullable MeshData bakeHullBox(AABB box)
  {
    ByteBufferBuilder storage = new ByteBufferBuilder(12 * BYTES_PER_QUAD);
    BufferBuilder builder = new BufferBuilder(storage, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

    float x0 = (float)box.minX, y0 = (float)box.minY, z0 = (float)box.minZ;
    float x1 = (float)box.maxX, y1 = (float)box.maxY, z1 = (float)box.maxZ;
    int r = 120, g = 140, b = 160, a = 200;

    quad(builder, x0,y0,z0, x1,y0,z0, x1,y1,z0, x0,y1,z0, r,g,b,a);
    quad(builder, x1,y0,z1, x0,y0,z1, x0,y1,z1, x1,y1,z1, r,g,b,a);
    quad(builder, x0,y0,z1, x0,y0,z0, x0,y1,z0, x0,y1,z1, r,g,b,a);
    quad(builder, x1,y0,z0, x1,y0,z1, x1,y1,z1, x1,y1,z0, r,g,b,a);
    quad(builder, x0,y0,z1, x1,y0,z1, x1,y0,z0, x0,y0,z0, r,g,b,a);
    quad(builder, x0,y1,z0, x1,y1,z0, x1,y1,z1, x0,y1,z1, r,g,b,a);

    return builder.build();
  }

  private static void quad(BufferBuilder b, float x0,float y0,float z0, float x1,float y1,float z1, float x2,float y2,float z2, float x3,float y3,float z3, int r,int g,int bv,int a)
  {
    b.addVertex(x0,y0,z0).setColor(r,g,bv,a);
    b.addVertex(x1,y1,z1).setColor(r,g,bv,a);
    b.addVertex(x2,y2,z2).setColor(r,g,bv,a);
    b.addVertex(x3,y3,z3).setColor(r,g,bv,a);
  }

  private static void emitQuadShader(BufferBuilder builder, BakedQuad quad, float ox, float oy, float oz, Direction face)
  {
    int[] vd = quad.getVertices();
    float nx = face.getStepX(), ny = face.getStepY(), nz = face.getStepZ();
    for (int i = 0; i < 4; i++)
    {
      int base = i * 8;
      float x = Float.intBitsToFloat(vd[base]) + ox;
      float y = Float.intBitsToFloat(vd[base + 1]) + oy;
      float z = Float.intBitsToFloat(vd[base + 2]) + oz;
      int argb = vd[base + 3];
      float u = Float.intBitsToFloat(vd[base + 4]);
      float v = Float.intBitsToFloat(vd[base + 5]);
      builder.addVertex(x, y, z).setColor((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >> 24) & 0xFF).setUv(u, v).setUv2(240, 240).setNormal(nx, ny, nz);
    }
  }

  private static void emitQuadVanilla(BufferBuilder builder, BakedQuad quad, float ox, float oy, float oz)
  {
    int[] vd = quad.getVertices();
    for (int i = 0; i < 4; i++)
    {
      int base = i * 8;
      float x = Float.intBitsToFloat(vd[base]) + ox;
      float y = Float.intBitsToFloat(vd[base + 1]) + oy;
      float z = Float.intBitsToFloat(vd[base + 2]) + oz;
      float u = Float.intBitsToFloat(vd[base + 4]);
      float v = Float.intBitsToFloat(vd[base + 5]);
      builder.addVertex(x, y, z).setUv(u, v).setColor(255, 255, 255, 255);
    }
  }

  private static @Nullable VertexBuffer uploadMesh(@Nullable MeshData mesh)
  {
    if (mesh == null) return null;

    VertexBuffer vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
    vbo.bind();
    vbo.upload(mesh);
    VertexBuffer.unbind();
    return vbo;
  }

  private static Set<Long> buildPosSet(List<VehicleSnapshot.BlockEntry> blocks)
  {
    Set<Long> set = new HashSet<>((int)(blocks.size() / 0.75f) + 1);
    for (VehicleSnapshot.BlockEntry b : blocks) set.add(b.packPos());
    return set;
  }
}