package cute.ame.auralithpioneerinitiative.Client.HUD.Panel;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Client.HUD.HoloPanelColors;
import cute.ame.auralithpioneerinitiative.Client.HUD.Widget.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import cute.ame.auralithpioneerinitiative.Ship.Network.HoloPanelClickPacket;

public class EquipmentKioskPanel extends AbstractBlockPanel
{

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "equipment_kiosk_panel");
    @Override public ResourceLocation getId() { return ID; }

    @Override
    public void renderContent(GuiGraphics g, HoloPanelContext ctx, float pt)
    {
        TabBarWidget root = new TabBarWidget();
        root.add("ARMAMENT", shopTab("Weapon modules",  "Phase 4.9"));
        root.add("ENGINES", shopTab("Engine modules",  "Phase 4.9"));
        root.add("SHIELDS", shopTab("Shield modules",  "Phase 4.9"));
        root.add("UTILITIES", shopTab("FTL, Nav, etc.",  "Phase 4.9"));
        root.add("INSTALLED", buildInstalledTab(ctx));
        root.setBounds(0, 0, getWidth(), getHeight());
        root.render(g, ctx, pt);
    }

    private HoloPanelWidget shopTab(String msg, String phase)
    {
        ColumnLayout col = new ColumnLayout(4);
        col.add(Widgets.spacer(50));
        col.add(Widgets.labelCenter(msg + ": not available", HoloPanelColors.MUTED_TEXT));
        col.add(Widgets.spacer(6));
        col.add(Widgets.labelCenter(phase, HoloPanelColors.AMBER_DIM, 0.8f, 0));
        return col;
    }

    private HoloPanelWidget buildInstalledTab(HoloPanelContext ctx)
    {
        ColumnLayout col = new ColumnLayout(2);
        col.add(Widgets.spacer(4));
        col.add(Widgets.label("INSTALLED MODULES", HoloPanelColors.AMBER_PRIMARY, 0.8f, -1));
        col.add(Widgets.separator());

        if (ctx.ship() == null)
        {
            col.add(Widgets.spacer(20));
            col.add(Widgets.labelCenter("No ship docked", HoloPanelColors.MUTED_TEXT));
            return col;
        }

        addInstalledRow(col, "Engine", ctx.ship().isSubsystemOnline(cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity.SUBSYSTEM_ENGINE));
        addInstalledRow(col, "Shield Generator", ctx.ship().isSubsystemOnline(cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity.SUBSYSTEM_SHIELDS));
        addInstalledRow(col, "Weapons Array", ctx.ship().isSubsystemOnline(cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity.SUBSYSTEM_WEAPONS));
        addInstalledRow(col, "Reactor", ctx.ship().isSubsystemOnline(cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity.SUBSYSTEM_REACTOR));
        addInstalledRow(col, "Cargo Hold", ctx.ship().isSubsystemOnline(cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity.SUBSYSTEM_CARGO));

        col.add(Widgets.spacer(4));
        col.add(Widgets.label("Component removal P4.6", HoloPanelColors.MUTED_TEXT, 0.75f, 0));
        return new ScrollPanel(col);
    }

    private void addInstalledRow(ColumnLayout col, String name, boolean online)
    {
        RowLayout row = new RowLayout(4);
        int statusCol = online ? HoloPanelColors.GREEN_OK : HoloPanelColors.RED_DANGER;
        row.add(Widgets.label(online ? "● ONLINE" : "○ OFFLINE", statusCol, 0.8f, -1), 70);
        row.add(Widgets.label(name, HoloPanelColors.LIGHT_TEXT), 170);

        HoloPanelWidget removeBtn = Widgets.button("REMOVE", "module:remove:" + name.toLowerCase(), act ->
        {
            if (blockPos != null) PacketDistributor.sendToServer(new HoloPanelClickPacket(ID, 0, 0, 0, act));
        });
        removeBtn.setEnabled(false); // locked until P4.6
        row.add(removeBtn, 60);
        col.add(row);
    }
}