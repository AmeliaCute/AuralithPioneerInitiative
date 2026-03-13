package cute.ame.auralithpioneerinitiative.Ship.Renderer;

import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class BakedShipMesh implements AutoCloseable
{
    public static final double LOD0_MAX_DIST = 64.0;
    public static final double LOD1_MAX_DIST = 256.0;
    public static final double LOD2_MAX_DIST = 512.0;

    private final @Nullable VertexBuffer shaderBuffer;
    private final @Nullable VertexBuffer vanillaBuffer;
    private final @Nullable VertexBuffer hullBuffer;

    private final AABB localBounds;
    private final int blockCount;

    public BakedShipMesh(@Nullable VertexBuffer shaderBuffer, @Nullable VertexBuffer vanillaBuffer, @Nullable VertexBuffer hullBuffer, AABB localBounds, int blockCount)
    {
        this.shaderBuffer  = shaderBuffer;
        this.vanillaBuffer = vanillaBuffer;
        this.hullBuffer    = hullBuffer;
        this.localBounds   = localBounds;
        this.blockCount    = blockCount;
    }

    public boolean isReady() { return shaderBuffer != null || vanillaBuffer != null; }
    public AABB getLocalBounds() { return localBounds; }
    public int getBlockCount() { return blockCount;  }

    public @Nullable VertexBuffer getBuffer(int lodLevel, boolean shadersActive)
    {
        return switch (lodLevel)
        {
            case 0, 1 -> shadersActive ? shaderBuffer : vanillaBuffer;
            case 2 -> hullBuffer;
            default -> null;
        };
    }

    @Override
    public void close()
    {
        if (shaderBuffer != null) shaderBuffer.close();
        if (vanillaBuffer != null) vanillaBuffer.close();
        if (hullBuffer != null) hullBuffer.close();
    }
}