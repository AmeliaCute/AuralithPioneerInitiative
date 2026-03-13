package cute.ame.auralithpioneerinitiative.Ship.Data;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class ShipRegistry
{
    private ShipRegistry() {}

    private static final Map<ResourceLocation, ShipDefinition> DEFINITIONS = new LinkedHashMap<>();

    public static void register(ResourceLocation id, ShipDefinition def) { DEFINITIONS.put(id, def);}
    public static Optional<ShipDefinition> get(ResourceLocation id)
    {
        return Optional.ofNullable(DEFINITIONS.get(id));
    }
    public static Collection<ShipDefinition> all()
    {
        return Collections.unmodifiableCollection(DEFINITIONS.values());
    }

    public static void clear()
    {
        DEFINITIONS.clear();
    }
    public static int size() { return DEFINITIONS.size(); }
}
