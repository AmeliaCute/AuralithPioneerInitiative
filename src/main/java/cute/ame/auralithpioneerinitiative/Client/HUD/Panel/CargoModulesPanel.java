package cute.ame.auralithpioneerinitiative.Client.HUD.Panel;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Client.HUD.HoloPanelColors;
import cute.ame.auralithpioneerinitiative.Client.HUD.Widget.*;
import cute.ame.auralithpioneerinitiative.Ship.Data.ShipDefinition;
import cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

public class CargoModulesPanel extends AbstractCockpitPanel
{

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "cargo_modules_panel");

    private static final float COS45 = (float) Math.cos(Math.toRadians(45));
    private static final float SIN45 = (float) Math.sin(Math.toRadians(45));

    public CargoModulesPanel()
    {
        super(new Vector3f(0, 0, 0), new Vector3f(COS45 * 1.2f, 0.1f, -SIN45 * 1.2f), new Vector3f(-SIN45, 0, -COS45), 0.5f);
    }

    @Override public ResourceLocation getId() { return ID; }

    @Override
    public void renderContent(GuiGraphics g, HoloPanelContext ctx, float pt)
    {
        TabBarWidget root = new TabBarWidget();
        root.add("CARGO", buildCargoTab());
        root.add("MODULES", buildModulesTab(ctx));
        root.add("SHIP INFO", buildShipInfoTab(ctx));
        root.add("STORE", buildStorePlaceholder());
        root.setBounds(0, 0, getWidth(), getHeight());
        root.render(g, ctx, pt);
    }

    private HoloPanelWidget buildCargoTab()
    {
        ColumnLayout col = new ColumnLayout(4);
        col.add(Widgets.spacer(4));
        col.add(Widgets.label("CARGO HOLD", HoloPanelColors.AMBER_PRIMARY, 0.8f, -1));
        col.add(Widgets.separator());
        col.add(Widgets.spacer(8));

        GridWidget grid = new GridWidget(4, 28, 2);
        for (int i = 0; i < 16; i++)
        {
            HoloPanelWidget cell = new HoloPanelWidget()
            {
                @Override public void render(GuiGraphics g, HoloPanelContext ctx, float pt)
                {
                    g.fill(x, y, x + w, y + h, 0xFF1A1A2A);
                    g.fill(x, y, x + w, y + 1, HoloPanelColors.opaque(HoloPanelColors.AMBER_BORDER));
                    g.fill(x, y + h - 1, x + w, y + h, HoloPanelColors.opaque(HoloPanelColors.AMBER_BORDER));
                    g.fill(x, y, x + 1, y + h, HoloPanelColors.opaque(HoloPanelColors.AMBER_BORDER));
                    g.fill(x + w - 1, y, x + w, y + h, HoloPanelColors.opaque(HoloPanelColors.AMBER_BORDER));
                }
            };
            grid.add(cell);
        }
        col.add(grid);
        col.add(Widgets.spacer(4));
        col.add(Widgets.label("Cargo system: P4.6", HoloPanelColors.MUTED_TEXT, 0.75f, 0));
        return new ScrollPanel(col);
    }

    private HoloPanelWidget buildModulesTab(HoloPanelContext ctx)
    {
        ColumnLayout col = new ColumnLayout(2);
        col.add(Widgets.spacer(4));
        col.add(Widgets.label("INSTALLED MODULES", HoloPanelColors.AMBER_PRIMARY, 0.8f, -1));
        col.add(Widgets.separator());

        if (ctx.ship() == null)
        {
            col.add(Widgets.label("No ship detected", HoloPanelColors.MUTED_TEXT, 0.9f, 0));
            return col;
        }

        addModuleRow(col, "Hull Integrity", ctx.ship().getHullIntegrity(), HoloPanelColors.hullColor(ctx.ship().getHullIntegrity()), ctx.ship().isSubsystemOnline(ShipEntity.SUBSYSTEM_ENGINE));
        addModuleRow(col, "Shield Generator", ctx.ship().getShieldStrength(), HoloPanelColors.CYAN_SHIELD, ctx.ship().isSubsystemOnline(ShipEntity.SUBSYSTEM_SHIELDS));
        addModuleRow(col, "Engine", ctx.ship().getFuelLevel(), HoloPanelColors.AMBER_PRIMARY, ctx.ship().isSubsystemOnline(ShipEntity.SUBSYSTEM_ENGINE));
        addModuleRow(col, "Weapons Array", 1.0f, HoloPanelColors.RED_DANGER, ctx.ship().isSubsystemOnline(ShipEntity.SUBSYSTEM_WEAPONS));
        addModuleRow(col, "Reactor", ctx.ship().getEuStored() > 0 ? 1.0f : 0f, HoloPanelColors.YELLOW_WARN, ctx.ship().isSubsystemOnline(ShipEntity.SUBSYSTEM_REACTOR));

        return new ScrollPanel(col);
    }

    private void addModuleRow(ColumnLayout col, String name, float health, int barColor, boolean online)
    {
        RowLayout row = new RowLayout(4);
        String status = online ? "■" : "□";
        int statusCol = online ? HoloPanelColors.GREEN_OK : HoloPanelColors.RED_DANGER;
        row.add(Widgets.label(status, statusCol), 10);
        row.add(Widgets.label(name, HoloPanelColors.LIGHT_TEXT), 160);

        HoloPanelWidget bar = Widgets.progressBar(health, HoloPanelColors.AMBER_DIM, barColor);
        bar.h = 8;
        row.add(bar, 100);
        col.add(row);
    }

    private HoloPanelWidget buildShipInfoTab(HoloPanelContext ctx)
    {
        ColumnLayout col = new ColumnLayout(3);
        col.add(Widgets.spacer(4));
        col.add(Widgets.label("VESSEL INFORMATION", HoloPanelColors.AMBER_PRIMARY, 0.8f, -1));
        col.add(Widgets.separator());
        col.add(Widgets.spacer(4));

        if (ctx.ship() == null) {
            col.add(Widgets.labelCenter("No ship data", HoloPanelColors.MUTED_TEXT));
            return col;
        }

        ShipDefinition def = ctx.ship().getDefinition().orElse(null);
        if (def == null) {
            col.add(Widgets.labelCenter("Unknown ship class", HoloPanelColors.MUTED_TEXT));
            return col;
        }

        col.add(Widgets.infoRow("Name", def.displayName()));
        col.add(Widgets.infoRow("Class", def.shipClass()));
        col.add(Widgets.infoRow("Mass", def.mass() + " kg"));
        col.add(Widgets.separator());
        col.add(Widgets.infoRow("Hull max", String.valueOf(def.maxHull())));
        col.add(Widgets.infoRow("Shield", String.valueOf(def.maxShield())));
        col.add(Widgets.infoRow("Fuel max", String.valueOf(def.maxFuel())));
        col.add(Widgets.infoRow("EU max", String.valueOf(def.maxEu())));
        col.add(Widgets.separator());
        col.add(Widgets.infoRow("Hardpoints", String.valueOf(def.hardpoints())));
        col.add(Widgets.spacer(4));
        col.add(Widgets.label("CURRENT STATUS", HoloPanelColors.AMBER_PRIMARY, 0.8f, -1));
        col.add(Widgets.separator());
        col.add(Widgets.infoRow("Hull", String.format("%.0f%%", ctx.ship().getHullIntegrity() * 100), HoloPanelColors.AMBER_PRIMARY, HoloPanelColors.hullColor(ctx.ship().getHullIntegrity())));
        col.add(Widgets.infoRow("Shield", String.format("%.0f%%", ctx.ship().getShieldStrength() * 100), HoloPanelColors.AMBER_PRIMARY, HoloPanelColors.CYAN_SHIELD));
        col.add(Widgets.infoRow("Fuel", String.format("%.0f%%", ctx.ship().getFuelLevel() * 100)));
        col.add(Widgets.infoRow("EU stored", String.valueOf(ctx.ship().getEuStored())));

        return new ScrollPanel(col);
    }

    private HoloPanelWidget buildStorePlaceholder()
    {
        ColumnLayout col = new ColumnLayout(4);
        col.add(Widgets.spacer(60));
        col.add(Widgets.labelCenter("Dock to access the store", HoloPanelColors.MUTED_TEXT));
        col.add(Widgets.spacer(8));
        col.add(Widgets.labelCenter("Store P4.9", HoloPanelColors.AMBER_DIM, 0.8f, 0));
        return col;
    }
}