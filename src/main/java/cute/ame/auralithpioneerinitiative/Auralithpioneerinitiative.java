package cute.ame.auralithpioneerinitiative;

import com.mojang.logging.LogUtils;
import cute.ame.auralithpioneerinitiative.Registries.*;
import cute.ame.auralithpioneerinitiative.WorldGen.WorldGenProvider;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

@Mod(Auralithpioneerinitiative.MODID)
public class Auralithpioneerinitiative
{
    public static final String MODID = "auralithpioneerinitiative";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Auralithpioneerinitiative(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::createRegistries);
        modEventBus.addListener(this::gatherData);

        if(FMLEnvironment.dist == Dist.CLIENT)
        {
            modEventBus.addListener(this::clientSetup);
        }

        FeatureRegistry.FEATURES.register(modEventBus);
        BlockRegistry.register(modEventBus);
        BlockRegistry.ITEMS.register(modEventBus);
        BiomeSourceRegistry.BIOME_SOURCES.register(modEventBus);
        DimensionRegistry.register();

        PlanetBiomeRegistry.PLANET_BIOMES.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
    }

    private void clientSetup(final FMLClientSetupEvent event)
    {
        ShaderRegistry.register();

        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.THERMAL_KELP.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.THERMAL_KELP_PLANT.get(), RenderType.cutout());
    }

    private void createRegistries(NewRegistryEvent event) {
        event.register(new RegistryBuilder<>(PlanetBiomeRegistry.PLANET_BIOME_REGISTRY_KEY)
                .sync(true).create());
    }

    private void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider =
                event.getLookupProvider();

        generator.addProvider(
                event.includeServer(),
                new WorldGenProvider(output, lookupProvider)
        );
    }

}
