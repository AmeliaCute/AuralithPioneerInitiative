package cute.ame.auralithpioneerinitiative.Registrie;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Planet.Arid.Item.AridCrystalShard;
import cute.ame.auralithpioneerinitiative.Item.PortableThrusterItem;
import cute.ame.auralithpioneerinitiative.Item.SpaceNavigatorItemDebug;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Data.ModuleSlotType;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Data.O2TankTier;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Data.SuitPieceDefinition;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Item.SuitArmorItem;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Module.O2TankModule;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Module.SuitModule;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item.Properties;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems
{
  public ModItems() {}
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Auralithpioneerinitiative.MODID);

  public static final DeferredItem<PortableThrusterItem> PORTABLE_THRUSTER =
      ITEMS.register("portable_thruster", () -> new PortableThrusterItem(new Properties().stacksTo(1)));

  public static final DeferredItem<SpaceNavigatorItemDebug> SPACE_NAVIGATOR =
      ITEMS.register("space_navigator", () -> new SpaceNavigatorItemDebug(new Properties().stacksTo(1)));

  public static final DeferredItem<BlockItem> SHIP_CORE =
      ITEMS.register("ship_core", () -> new BlockItem(ModBlocks.SHIP_CORE.get(), new Properties()));

  public static final DeferredItem<BlockItem> HOLOPANEL =
      ITEMS.register("holopanel", () -> new BlockItem(ModBlocks.HOLOPANEL.get(), new Properties()));

  public static final DeferredItem<AridCrystalShard> ARID_CRYSTAL_SHARD =
      ITEMS.register("arid_crystal_shard", AridCrystalShard::new);

  public static final DeferredItem<BlockItem> ARID_DUST =
      ITEMS.register("arid_dust", () -> new BlockItem(ModBlocks.ARID_DUST.get(), new Properties()));

  public static final DeferredItem<BlockItem> ARID_ROCK =
      ITEMS.register("arid_rock", () -> new BlockItem(ModBlocks.ARID_ROCK.get(), new Properties()));

  public static final DeferredItem<BlockItem> ARID_ROCK_CRACKED =
      ITEMS.register("arid_rock_cracked", () -> new BlockItem(ModBlocks.ARID_ROCK_CRACKED.get(), new Properties()));

  public static final DeferredItem<BlockItem> ARID_ROCK_POLISHED =
      ITEMS.register("arid_rock_polished", () -> new BlockItem(ModBlocks.ARID_ROCK_POLISHED.get(), new Properties()));

  public static final DeferredItem<BlockItem> ARID_STRATA_RED =
      ITEMS.register("arid_strata_red", () -> new BlockItem(ModBlocks.ARID_STRATA_RED.get(), new Properties()));

  public static final DeferredItem<BlockItem> ARID_STRATA_ORANGE =
      ITEMS.register("arid_strata_orange", () -> new BlockItem(ModBlocks.ARID_STRATA_ORANGE.get(), new Properties()));

  public static final DeferredItem<BlockItem> ARID_STRATA_WHITE =
      ITEMS.register("arid_strata_white", () -> new BlockItem(ModBlocks.ARID_STRATA_WHITE.get(), new Properties()));

  public static final DeferredItem<BlockItem> ARID_STRATA_MAROON =
      ITEMS.register("arid_strata_maroon", () -> new BlockItem(ModBlocks.ARID_STRATA_MAROON.get(), new Properties()));

  public static final DeferredItem<BlockItem> ARID_CRYSTAL_SMALL =
      ITEMS.register("arid_crystal_small", () -> new BlockItem(ModBlocks.ARID_CRYSTAL_SMALL.get(), new Properties()));

  public static final DeferredItem<BlockItem> ARID_CRYSTAL_MEDIUM =
      ITEMS.register("arid_crystal_medium", () -> new BlockItem(ModBlocks.ARID_CRYSTAL_MEDIUM.get(), new Properties()));

  public static final DeferredItem<BlockItem> ARID_CRYSTAL_LARGE =
      ITEMS.register("arid_crystal_large", () -> new BlockItem(ModBlocks.ARID_CRYSTAL_LARGE.get(), new Properties()));

  public static final DeferredItem<BlockItem> ARID_GLYPH =
      ITEMS.register("arid_glyph", () -> new BlockItem(ModBlocks.ARID_GLYPH.get(), new Properties()));

  public static final DeferredItem<BlockItem> ALIEN_PANEL =
      ITEMS.register("alien_panel", () -> new BlockItem(ModBlocks.ALIEN_PANEL.get(), new Properties()));

  public static final DeferredItem<BlockItem> ALIEN_CONDUIT =
      ITEMS.register("alien_conduit", () -> new BlockItem(ModBlocks.ALIEN_CONDUIT.get(), new Properties()));

  public static final DeferredItem<SuitArmorItem> ARID_HELMET =
      ITEMS.register("arid_helmet", () -> new SuitArmorItem(
          ModArmorMaterials.ARID,
          ArmorItem.Type.HELMET,
          new SuitPieceDefinition(ModuleSlotType.REGULATOR, ModuleSlotType.FLASHLIGHT),
          new Properties()
      ));

  public static final DeferredItem<SuitArmorItem> ARID_CHESTPLATE =
      ITEMS.register("arid_chestplate", () -> new SuitArmorItem(
          ModArmorMaterials.ARID,
          ArmorItem.Type.CHESTPLATE,
          new SuitPieceDefinition(
              ModuleSlotType.REBREATHER,
              ModuleSlotType.O2_TANK,
              ModuleSlotType.O2_TANK,
              ModuleSlotType.BATTERY,
              ModuleSlotType.BATTERY
          ),
          new Properties()
      ));

  public static final DeferredItem<SuitArmorItem> ARID_LEGGINGS =
      ITEMS.register("arid_leggings", () -> new SuitArmorItem(
          ModArmorMaterials.ARID,
          ArmorItem.Type.LEGGINGS,
          new SuitPieceDefinition(ModuleSlotType.FREE, ModuleSlotType.FREE),
          new Properties()
      ));

  public static final DeferredItem<SuitModule> MODULE_REGULATOR =
      ITEMS.register("module_regulator", () ->
          new SuitModule(ModuleSlotType.REGULATOR, new Properties().stacksTo(1).durability(800)));

  public static final DeferredItem<SuitModule> MODULE_REBREATHER =
      ITEMS.register("module_rebreather", () ->
          new SuitModule(ModuleSlotType.REBREATHER, new Properties().stacksTo(1).durability(800)));

  public static final DeferredItem<O2TankModule> MODULE_O2_TANK_BASIC =
      ITEMS.register("module_o2_tank_basic",
          () -> new O2TankModule(O2TankTier.BASIC));

  public static final DeferredItem<O2TankModule> MODULE_O2_TANK_ADVANCED =
      ITEMS.register("module_o2_tank_advanced",
          () -> new O2TankModule(O2TankTier.ADVANCED));

  public static final DeferredItem<O2TankModule> MODULE_O2_TANK_ELITE =
      ITEMS.register("module_o2_tank_elite",
          () -> new O2TankModule(O2TankTier.ELITE));

  public static final DeferredItem<SuitModule> MODULE_O2_GENERATOR =
      ITEMS.register("module_o2_generator", () ->
          new SuitModule(ModuleSlotType.O2_TANK, new Properties().stacksTo(1).durability(500)));

  public static final DeferredItem<SuitModule> MODULE_FLASHLIGHT =
      ITEMS.register("module_flashlight", () ->
          new SuitModule(ModuleSlotType.FLASHLIGHT, new Properties().stacksTo(1).durability(400)));

  public static final DeferredItem<SuitModule> MODULE_JETPACK =
      ITEMS.register("module_jetpack", () ->
          new SuitModule(ModuleSlotType.JETPACK, new Properties().stacksTo(1).durability(600)));

}