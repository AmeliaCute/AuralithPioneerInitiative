package cute.ame.auralithpioneerinitiative.HoloPanel.Panel;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.HoloPanel.HoloPanelClientState;
import cute.ame.auralithpioneerinitiative.HoloPanel.HoloPanelColors;
import cute.ame.auralithpioneerinitiative.HoloPanel.Widget.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import cute.ame.auralithpioneerinitiative.Ship.Network.HoloPanelClickPacket;

public class LandingPadPanel extends AbstractBlockPanel
{

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "landing_pad_panel");

    @Override public ResourceLocation getId() { return ID; }

    @Override
    public void renderContent(GuiGraphics g, HoloPanelContext ctx, float pt)
    {
        CompoundTag data = blockPos != null ? HoloPanelClientState.getInstance().get(blockPos) : new CompoundTag();

        TabBarWidget root = new TabBarWidget();
        root.add("STATUS", buildStatusTab(data));
        root.add("SERVICES", buildServicesTab(data));
        root.add("ACCESS LOG", buildLogTab(data));
        root.setBounds(0, 0, getWidth(), getHeight());
        root.render(g, ctx, pt);
    }

    private HoloPanelWidget buildStatusTab(CompoundTag data)
    {
        ColumnLayout col = new ColumnLayout(3);
        col.add(Widgets.spacer(4));

        boolean docked  = data.getBoolean("shipDocked");
        boolean service = data.getBoolean("servicing");
        String  state = docked ? (service ? "SERVICING" : "DOCKED") : "VACANT";
        int stateCol = docked ? (service ? HoloPanelColors.AMBER_ACTIVE : HoloPanelColors.GREEN_OK) : HoloPanelColors.MUTED_TEXT;

        col.add(Widgets.labelCenter(state, stateCol, 2.0f, 0));
        col.add(Widgets.spacer(6));
        col.add(Widgets.separator());

        if (docked)
        {
            col.add(Widgets.spacer(4));
            col.add(Widgets.label("DOCKED VESSEL", HoloPanelColors.AMBER_PRIMARY, 0.8f, -1));
            col.add(Widgets.separator());
            float hull = data.getFloat("hullPct");
            float shield = data.getFloat("shieldPct");
            float fuel = data.getFloat("fuelPct");
            long  eu = data.getLong("euStored");
            String cls = data.contains("shipClass") ? data.getString("shipClass") : "Unknown";
            col.add(Widgets.infoRow("Class", cls));
            col.add(Widgets.infoRow("Hull", String.format("%.0f%%", hull * 100), HoloPanelColors.AMBER_PRIMARY, HoloPanelColors.hullColor(hull)));
            col.add(Widgets.infoRow("Shield", String.format("%.0f%%", shield * 100), HoloPanelColors.AMBER_PRIMARY, HoloPanelColors.CYAN_SHIELD));
            col.add(Widgets.infoRow("Fuel", String.format("%.0f%%", fuel * 100)));
            col.add(Widgets.infoRow("EU", String.valueOf(eu)));
        }
        else
        {
            col.add(Widgets.spacer(20));
            col.add(Widgets.labelCenter("Pad is vacant", HoloPanelColors.MUTED_TEXT));
        }
        return col;
    }

    private HoloPanelWidget buildServicesTab(CompoundTag data)
    {
        ColumnLayout col = new ColumnLayout(4);
        col.add(Widgets.spacer(8));
        col.add(Widgets.label("PAD SERVICES", HoloPanelColors.AMBER_PRIMARY, 0.8f, -1));
        col.add(Widgets.separator());
        col.add(Widgets.spacer(8));

        boolean docked  = data.getBoolean("shipDocked");
        boolean hasFuel = data.getBoolean("hasFuel");
        boolean hasAmmo = data.getBoolean("hasAmmo");

        GridWidget grid = new GridWidget(2, 36, 4);
        grid.add(makeServiceBtn("REFUEL", "service:refuel", docked && hasFuel));
        grid.add(makeServiceBtn("REPAIR", "service:repair", docked));
        grid.add(makeServiceBtn("REARM", "service:rearm", docked && hasAmmo));
        grid.add(makeServiceBtn("TRANSFER EU", "service:transfer_eu", docked));
        col.add(grid);

        if (!docked)
        {
            col.add(Widgets.spacer(8));
            col.add(Widgets.labelCenter("Services unavailable: no ship docked", HoloPanelColors.MUTED_TEXT, 0.8f, 0));
        }
        return col;
    }

    private HoloPanelWidget makeServiceBtn(String label, String action, boolean enabled)
    {
        HoloPanelWidget btn = Widgets.button(label, action, act ->
        {
            if (blockPos == null) return;
            PacketDistributor.sendToServer(new HoloPanelClickPacket(ID, 0, 0, 0, act));
        });
        btn.setEnabled(enabled);
        return btn;
    }

    private HoloPanelWidget buildLogTab(CompoundTag data)
    {
        ColumnLayout col = new ColumnLayout(1);
        col.add(Widgets.spacer(2));
        col.add(Widgets.label("ACCESS LOG", HoloPanelColors.AMBER_PRIMARY, 0.8f, -1));
        col.add(Widgets.separator());

        ListTag logList = data.getList("log", 8);
        if (logList.isEmpty())
        {
            col.add(Widgets.spacer(10));
            col.add(Widgets.label("No log entries", HoloPanelColors.MUTED_TEXT, 0.85f, 0));
        } else for (int i = logList.size() - 1; i >= 0; i--) col.add(Widgets.label(logList.getString(i), HoloPanelColors.LIGHT_TEXT, 0.8f, -1));
        return new ScrollPanel(col);
    }
}