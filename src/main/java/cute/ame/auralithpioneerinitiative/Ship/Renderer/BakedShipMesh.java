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

    private final @Nullable VertexBuffer solidBuffer;

    private final @Nullable VertexBuffer hullBuffer;

    private final AABB localBounds;

    private final int blockCount;

    public BakedShipMesh(@Nullable VertexBuffer solidBuffer, @Nullable VertexBuffer hullBuffer, AABB localBounds, int blockCount)
    {
        this.solidBuffer = solidBuffer;
        this.hullBuffer = hullBuffer;
        this.localBounds = localBounds;
        this.blockCount = blockCount;
    }

    public boolean isReady() { return solidBuffer != null; }
    public AABB getLocalBounds() { return localBounds; }
    public int getBlockCount() { return blockCount; }

    public @Nullable VertexBuffer getBuffer(int lodLevel)
    {
        return switch (lodLevel)
        {
            case 0, 1 -> solidBuffer;
            case 2    -> hullBuffer;
            default   -> null;
        };
    }

    @Override
    public void close()
    {
        if (solidBuffer != null) solidBuffer.close();
        if (hullBuffer  != null) hullBuffer.close();
    }
}