package cute.ame.auralithpioneerinitiative.Ship.Data;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public final class ShipDefinitionLoader extends SimplePreparableReloadListener<Map<ResourceLocation, ShipDefinition>>
{
    public static final ShipDefinitionLoader INSTANCE = new ShipDefinitionLoader();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FOLDER = "ship_classes";

    private ShipDefinitionLoader() {}

    @Override
    protected Map<ResourceLocation, ShipDefinition> prepare(ResourceManager manager, ProfilerFiller profiler)
    {
        Map<ResourceLocation, ShipDefinition> loaded = new HashMap<>();

        Map<ResourceLocation, net.minecraft.server.packs.resources.Resource> resources =
            manager.listResources(FOLDER, rl -> rl.getPath().endsWith(".json"));

        for (var entry : resources.entrySet())
        {
            ResourceLocation fileRl = entry.getKey();
            String path = fileRl.getPath();
            String name = path.substring(FOLDER.length() + 1, path.length() - ".json".length());
            ResourceLocation defId = ResourceLocation.fromNamespaceAndPath(fileRl.getNamespace(), name);

            try (var reader = new InputStreamReader(entry.getValue().open()))
            {
                JsonElement json = GsonHelper.parse(reader);
                ShipDefinition.CODEC
                    .parse(JsonOps.INSTANCE, json)
                    .ifSuccess(def ->
                    {
                        loaded.put(def.id(), def);
                        LOGGER.debug("[Auralith] Prepared ship definition: {}", def.id());
                    })
                    .ifError(err -> LOGGER.error("[Auralith] Failed to parse ship definition '{}': {}", defId, err.message()));
            }
            catch (Exception e)
            {
                LOGGER.error("[Auralith] Error reading ship definition file '{}'", fileRl, e);
            }
        }

        LOGGER.info("[Auralith] Prepared {} ship definition(s)", loaded.size());
        return loaded;
    }

    @Override
    protected void apply(Map<ResourceLocation, ShipDefinition> data, ResourceManager manager, ProfilerFiller profiler)
    {
        ShipRegistry.clear();
        data.forEach(ShipRegistry::register);
        LOGGER.info("[Auralith] Applied {} ship definition(s)", data.size());
    }
}
