package cute.ame.auralithpioneerinitiative.Registrie;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.*;

public class ModArmorMaterials
{
  private ModArmorMaterials() {}

  public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
      DeferredRegister.create(Registries.ARMOR_MATERIAL, Auralithpioneerinitiative.MODID);

  public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ARID =
      ARMOR_MATERIALS.register("arid", () -> new ArmorMaterial(
          Map.of(
              ArmorItem.Type.HELMET, 4,
              ArmorItem.Type.CHESTPLATE, 7,
              ArmorItem.Type.LEGGINGS, 5,
              ArmorItem.Type.BOOTS, 2
          ),
          18,
          SoundEvents.ARMOR_EQUIP_IRON,
          () -> Ingredient.EMPTY,
          List.of(new ArmorMaterial.Layer(
              ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "arid_suit")
          )),
          1.5f,
          0.0f
      ));
}
