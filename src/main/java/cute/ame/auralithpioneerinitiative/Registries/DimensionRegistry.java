package cute.ame.auralithpioneerinitiative.Registries;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public class DimensionRegistry
{
    public static final ResourceKey<Level> AQUATIC_PLANET_LEVEL = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "aquatic_planet")
    );

    public static final ResourceKey<DimensionType> AQUATIC_PLANET_TYPE = ResourceKey.create(
            Registries.DIMENSION_TYPE,
            ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "aquatic_planet_type")
    );

    public static final ResourceKey<LevelStem> AQUATIC_PLANET_STEM = ResourceKey.create(
            Registries.LEVEL_STEM,
            ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "aquatic_planet")
    );

    public static void register()
    {}
}
