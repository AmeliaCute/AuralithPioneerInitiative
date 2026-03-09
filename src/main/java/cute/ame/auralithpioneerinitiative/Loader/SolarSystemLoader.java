package cute.ame.auralithpioneerinitiative.Loader;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import cute.ame.auralithpioneerinitiative.API.AuralithAPI;
import cute.ame.auralithpioneerinitiative.Data.SolarSystemDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public final class SolarSystemLoader extends SimplePreparableReloadListener<Map<ResourceLocation, SolarSystemDefinition>>
{

    public static final SolarSystemLoader INSTANCE = new SolarSystemLoader();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FOLDER  = "solar_systems";

    private SolarSystemLoader() {}

    @Override
    protected Map<ResourceLocation, SolarSystemDefinition> prepare(ResourceManager manager, ProfilerFiller profiler)
    {
        Map<ResourceLocation, SolarSystemDefinition> loaded = new HashMap<>();
        Map<ResourceLocation, net.minecraft.server.packs.resources.Resource> resources = manager.listResources(FOLDER, rl -> rl.getPath().endsWith(".json"));

        for (var entry : resources.entrySet())
        {
            ResourceLocation fileRl = entry.getKey();

            String path = fileRl.getPath();
            String name = path.substring(FOLDER.length() + 1, path.length() - ".json".length());
            ResourceLocation systemId = ResourceLocation.fromNamespaceAndPath(fileRl.getNamespace(), name);

            try (var reader = new InputStreamReader(entry.getValue().open()))
            {
                JsonElement json = GsonHelper.parse(reader);

                SolarSystemDefinition.CODEC
                .parse(JsonOps.INSTANCE, json)
                .ifSuccess(def -> {
                    loaded.put(systemId, def);
                    LOGGER.debug("[Auralith] Loaded solar system: {}", systemId);
                })
                .ifError(err ->
                        LOGGER.error("[Auralith] Failed to parse solar system '{}': {}", systemId, err.message())
                );
            } catch (Exception e)
            {
                LOGGER.error("[Auralith] Error reading solar system file '{}'", fileRl, e);
            }
        }

        LOGGER.info("[Auralith] Prepared {} solar system(s)", loaded.size());
        return loaded;
    }

    @Override
    protected void apply(Map<ResourceLocation, SolarSystemDefinition> data, ResourceManager manager, ProfilerFiller profiler) {
        AuralithAPI.clearAll();
        data.forEach(AuralithAPI::registerSolarSystem);
        LOGGER.info("[Auralith] Applied {} solar system(s)", data.size());
    }
}