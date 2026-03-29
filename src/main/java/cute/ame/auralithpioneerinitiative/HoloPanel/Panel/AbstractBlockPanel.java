package cute.ame.auralithpioneerinitiative.HoloPanel.Panel;

import cute.ame.auralithpioneerinitiative.HoloPanel.HoloPanelRenderer.BlockSidedPanel;
import cute.ame.auralithpioneerinitiative.HoloPanel.HoloPanelType;
import cute.ame.auralithpioneerinitiative.HoloPanel.Anchor.PanelAnchor;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public abstract class AbstractBlockPanel implements HoloPanelType, BlockSidedPanel
{

    private static final int WIDTH  = 320;
    private static final int HEIGHT = 200;

    protected @Nullable BlockPos blockPos = null;
    protected Vector3f worldFacing = new Vector3f(0, 0, -1);

    public void setBlockContext(BlockPos pos, Vector3f facing)
    {
        this.blockPos = pos;
        this.worldFacing = facing;
    }

    @Override public int getWidth() { return WIDTH; }
    @Override public int getHeight() { return HEIGHT; }
    @Override public PanelAnchor getAnchor() { return PanelAnchor.BLOCK_RELATIVE; }
    @Override public float getFovGate() { return -1; }

    @Override public @Nullable BlockPos getBlockPos() { return blockPos; }
    @Override public Vector3f getWorldFacing() { return worldFacing; }
}
