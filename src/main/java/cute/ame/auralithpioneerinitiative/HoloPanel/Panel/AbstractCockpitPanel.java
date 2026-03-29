package cute.ame.auralithpioneerinitiative.HoloPanel.Panel;

import cute.ame.auralithpioneerinitiative.HoloPanel.HoloPanelRenderer.CockpitRelativePanel;
import cute.ame.auralithpioneerinitiative.HoloPanel.HoloPanelType;
import cute.ame.auralithpioneerinitiative.HoloPanel.Anchor.PanelAnchor;
import org.joml.Vector3f;

public abstract class AbstractCockpitPanel implements HoloPanelType, CockpitRelativePanel {

    private static final int WIDTH  = 320;
    private static final int HEIGHT = 200;

    private final Vector3f cockpitOffset;
    private final Vector3f panelOffset;
    private final Vector3f facingNormal;
    private final float fovGate;

    protected AbstractCockpitPanel(Vector3f cockpitOffset, Vector3f panelOffset, Vector3f facingNormal, float fovGate)
    {
        this.cockpitOffset = cockpitOffset;
        this.panelOffset = panelOffset;
        this.facingNormal = facingNormal;
        this.fovGate = fovGate;
    }

    @Override public int getWidth() { return WIDTH; }
    @Override public int getHeight() { return HEIGHT; }
    @Override public PanelAnchor getAnchor() { return PanelAnchor.COCKPIT_RELATIVE; }
    @Override public float getFovGate() { return fovGate; }

    @Override public Vector3f getCockpitOffset() { return new Vector3f(cockpitOffset); }
    @Override public Vector3f getPanelOffset() { return new Vector3f(panelOffset); }
    @Override public Vector3f getFacingNormal() { return new Vector3f(facingNormal); }
}
