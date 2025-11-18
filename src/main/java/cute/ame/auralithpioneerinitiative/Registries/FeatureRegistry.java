package cute.ame.auralithpioneerinitiative.Registries;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Feature.Thermal.ThermalKelpFeature;
import cute.ame.auralithpioneerinitiative.Plant.Thermal.ThermalKelpPlant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FeatureRegistry
{
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(BuiltInRegistries.FEATURE, Auralithpioneerinitiative.MODID);

    public static final DeferredHolder<Feature<?>, ThermalKelpFeature> THERMAL_KELP_FEATURE =
            FEATURES.register("thermal_kelp", () -> new ThermalKelpFeature(NoneFeatureConfiguration.CODEC));
}
