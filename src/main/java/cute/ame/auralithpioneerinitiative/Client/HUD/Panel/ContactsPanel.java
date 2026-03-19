package cute.ame.auralithpioneerinitiative.Client.HUD.Panel;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Client.HUD.HoloPanelColors;
import cute.ame.auralithpioneerinitiative.Client.HUD.Widget.*;
import cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import java.util.Comparator;
import java.util.List;

public class ContactsPanel extends AbstractCockpitPanel
{

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "contacts_panel");

    private static final float COS45 = (float) Math.cos(Math.toRadians(45));
    private static final float SIN45 = (float) Math.sin(Math.toRadians(45));

    public ContactsPanel()
    {
        super(new Vector3f(0, 0, 0), new Vector3f(-COS45 * 1.2f, 0.1f, -SIN45 * 1.2f), new Vector3f(SIN45, 0, -COS45), 0.5f);
    }

    @Override public ResourceLocation getId() { return ID; }

    @Override
    public void renderContent(GuiGraphics g, HoloPanelContext ctx, float pt)
    {
        TabBarWidget root = buildRoot(ctx);
        root.setBounds(0, 0, getWidth(), getHeight());
        root.render(g, ctx, pt);
    }

    private TabBarWidget buildRoot(HoloPanelContext ctx)
    {
        TabBarWidget root = new TabBarWidget();
        root.add("CONTACTS", buildContactsTab(ctx));
        root.add("NAVIGATION", buildNavigationTab());
        root.add("TRANSACTIONS", buildTransactionsTab());
        root.add("ROUTE", buildRoutePlaceholder());
        return root;
    }

    private HoloPanelWidget buildContactsTab(HoloPanelContext ctx)
    {
        ColumnLayout col = new ColumnLayout(0);

        if (ctx.ship() == null || ctx.level() == null)
        {
            col.add(Widgets.spacer(20));
            col.add(Widgets.labelCenter("No ship data", HoloPanelColors.MUTED_TEXT));
            return col;
        }

        List<ShipEntity> nearby = ctx.level().getEntitiesOfClass(ShipEntity.class, AABB.ofSize(ctx.ship().position(), 1024, 512, 1024));
        nearby.removeIf(e -> e.getUUID().equals(ctx.ship().getUUID()));
        nearby.sort(Comparator.comparingDouble(e -> e.position().distanceTo(ctx.ship().position())));

        if (nearby.isEmpty())
        {
            col.add(Widgets.spacer(20));
            col.add(Widgets.labelCenter("No contacts detected", HoloPanelColors.MUTED_TEXT));
            return new ScrollPanel(col);
        }

        for (ShipEntity contact : nearby)
        {
            col.add(buildContactRow(contact, ctx));
            col.add(Widgets.separator());
        }
        return new ScrollPanel(col);
    }

    private HoloPanelWidget buildContactRow(ShipEntity contact, HoloPanelContext ctx)
    {
        RowLayout row = new RowLayout(4);
        var badge = Widgets.label("■", HoloPanelColors.AMBER_ACTIVE);
        String idShort = contact.getUUID().toString().substring(0, 8).toUpperCase();
        double dist = contact.position().distanceTo(ctx.ship().position());
        String distStr = (dist < 1000) ? ((int) dist) + "m" : String.format("%.1fkm", dist / 1000);
        var info  = Widgets.label(idShort + " " + contact.getShipClassId().split(":")[1], HoloPanelColors.LIGHT_TEXT);
        var distW = Widgets.labelRight(distStr, HoloPanelColors.MUTED_TEXT);
        row.add(badge, 12);
        row.add(info, 200);
        row.add(distW, 90);
        return row;
    }

    private HoloPanelWidget buildNavigationTab()
    {
        ColumnLayout col = new ColumnLayout(4);
        col.add(Widgets.spacer(4));
        col.add(Widgets.label("DIMENSIONAL PORTALS", HoloPanelColors.AMBER_PRIMARY, 0.8f, -1));
        col.add(Widgets.separator());
        col.add(Widgets.label("No portals detected nearby", HoloPanelColors.MUTED_TEXT, 0.9f, 0));
        col.add(Widgets.spacer(8));
        col.add(Widgets.label("ORBITAL DATA", HoloPanelColors.AMBER_PRIMARY, 0.8f, -1));
        col.add(Widgets.separator());
        col.add(Widgets.label("Enter orbit to display data", HoloPanelColors.MUTED_TEXT, 0.9f, 0));
        return new ScrollPanel(col);
    }

    private HoloPanelWidget buildTransactionsTab()
    {
        ColumnLayout col = new ColumnLayout(1);
        col.add(Widgets.spacer(4));
        col.add(Widgets.label("TRANSACTION LOG", HoloPanelColors.AMBER_PRIMARY, 0.8f, -1));
        col.add(Widgets.separator());
        col.add(Widgets.label("No recent transactions", HoloPanelColors.MUTED_TEXT, 0.9f, 0));
        return new ScrollPanel(col);
    }

    private HoloPanelWidget buildRoutePlaceholder()
    {
        ColumnLayout col = new ColumnLayout(4);
        col.add(Widgets.spacer(60));
        col.add(Widgets.labelCenter("Route : non définie", HoloPanelColors.MUTED_TEXT));
        col.add(Widgets.spacer(8));
        col.add(Widgets.labelCenter("Route planning P4.10", HoloPanelColors.AMBER_DIM, 0.8f, 0));
        return col;
    }
}