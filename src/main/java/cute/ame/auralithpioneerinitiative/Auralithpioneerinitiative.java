package cute.ame.auralithpioneerinitiative;

import com.mojang.logging.LogUtils;
import cute.ame.auralithpioneerinitiative.Registries.PlanetBiomeRegistry;
import cute.ame.auralithpioneerinitiative.Registries.ShaderRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.slf4j.Logger;

@Mod(Auralithpioneerinitiative.MODID)
public class Auralithpioneerinitiative {
    public static final String MODID = "auralithpioneerinitiative";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Auralithpioneerinitiative(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::createRegistries);
        PlanetBiomeRegistry.PLANET_BIOMES.register(modEventBus);

        ShaderRegistry.register();
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
    }

    private void createRegistries(NewRegistryEvent event) {
        event.register(new RegistryBuilder<>(PlanetBiomeRegistry.PLANET_BIOME_REGISTRY_KEY)
                .sync(true).create());
    }


}
