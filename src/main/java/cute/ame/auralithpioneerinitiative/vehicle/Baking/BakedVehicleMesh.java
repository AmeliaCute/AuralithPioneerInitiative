package cute.ame.auralithpioneerinitiative.vehicle.Baking;

import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public final class BakedVehicleMesh implements AutoCloseable
{
  public static final int LOD_DETAILED = 0;
  public static final int LOD_REDUCED = 1;
  public static final int LOD_WIREFRAME = 2;

  private final @Nullable VertexBuffer shaderVbo;
  private final @Nullable VertexBuffer vanillaVbo;
  private final @Nullable VertexBuffer hullVbo;

  private final AABB localBounds;
  private final int blockCount;
  private boolean closed = false;

  public BakedVehicleMesh(@Nullable VertexBuffer shaderVbo, @Nullable VertexBuffer vanillaVbo, @Nullable VertexBuffer hullVbo, AABB localBounds, int blockCount)
  {
    this.shaderVbo = shaderVbo;
    this.vanillaVbo = vanillaVbo;
    this.hullVbo = hullVbo;
    this.localBounds = localBounds;
    this.blockCount = blockCount;
  }

  public @Nullable VertexBuffer getBuffer(int lod, boolean shadersActive)
  {
    return switch (lod)
    {
      case LOD_DETAILED, LOD_REDUCED -> shadersActive ? shaderVbo : vanillaVbo;
      case LOD_WIREFRAME -> hullVbo;
      default -> vanillaVbo;
    };
  }

  public boolean isReady()
  {
    return !closed && (shaderVbo != null || vanillaVbo != null || hullVbo != null);
  }

  public AABB getLocalBounds() { return localBounds; }
  public int getBlockCount() { return blockCount; }

  @Override
  public void close()
  {
    if (closed) return;
    closed = true;
    if (shaderVbo != null) shaderVbo.close();
    if (vanillaVbo != null) vanillaVbo.close();
    if (hullVbo != null) hullVbo.close();
  }
}
