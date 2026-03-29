package cute.ame.auralithpioneerinitiative.HoloPanel.Panel;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.HoloPanel.HoloPanelClientState;
import cute.ame.auralithpioneerinitiative.HoloPanel.HoloPanelColors;
import cute.ame.auralithpioneerinitiative.HoloPanel.Widget.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import cute.ame.auralithpioneerinitiative.Ship.Network.HoloPanelClickPacket;

public class StationServicesPanel extends AbstractBlockPanel {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "station_services_panel");

    @Override public ResourceLocation getId() { return ID; }

    @Override
    public void renderContent(GuiGraphics g, HoloPanelContext ctx, float pt)
    {
        CompoundTag data = blockPos != null ? HoloPanelClientState.getInstance().get(blockPos) : new CompoundTag();

        TabBarWidget root = new TabBarWidget();
        root.add("SERVICES", buildServicesTab(data));
        root.add("MARKET", placeholder("Market data unavailable", "Phase 4.9"));
        root.add("MISSIONS", placeholder("Mission board unavailable", "Phase 4.9"));
        root.add("STAR MAP", placeholder("Star map — Phase 4.5 / 4.10", "Requires nav_panel"));
        root.add("SHIPYARD", placeholder("Shipyard unavailable", "Phase 4.9"));
        root.setBounds(0, 0, getWidth(), getHeight());
        root.render(g, ctx, pt);
    }

    private HoloPanelWidget buildServicesTab(CompoundTag data)
    {
        ColumnLayout col = new ColumnLayout(4);
        col.add(Widgets.spacer(8));
        col.add(Widgets.label("STATION SERVICES", HoloPanelColors.AMBER_PRIMARY, 0.8f, -1));
        col.add(Widgets.separator());
        col.add(Widgets.spacer(8));

        boolean docked = data.getBoolean("shipDocked");
        GridWidget grid = new GridWidget(2, 36, 4);
        grid.add(makeBtn("REFUEL", "service:refuel", docked));
        grid.add(makeBtn("REPAIR", "service:repair", docked));
        grid.add(makeBtn("REARM", "service:rearm", docked));
        grid.add(makeBtn("TRANSFER EU", "service:transfer_eu", docked));
        col.add(grid);
        return col;
    }

    private HoloPanelWidget makeBtn(String label, String action, boolean enabled)
    {
        HoloPanelWidget btn = Widgets.button(label, action, act ->
        {
            if (blockPos == null) return;
            PacketDistributor.sendToServer(new HoloPanelClickPacket(ID, 0, 0, 0, act));
        });
        btn.setEnabled(enabled);
        return btn;
    }

    private HoloPanelWidget placeholder(String msg, String phase)
    {
        ColumnLayout col = new ColumnLayout(4);
        col.add(Widgets.spacer(50));
        col.add(Widgets.labelCenter(msg, HoloPanelColors.MUTED_TEXT));
        col.add(Widgets.spacer(6));
        col.add(Widgets.labelCenter(phase, HoloPanelColors.AMBER_DIM, 0.8f, 0));
        return col;
    }
}