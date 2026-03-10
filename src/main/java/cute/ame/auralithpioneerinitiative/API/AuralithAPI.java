package cute.ame.auralithpioneerinitiative.API;

import cute.ame.auralithpioneerinitiative.Data.PlanetDefinition;
import cute.ame.auralithpioneerinitiative.Data.SolarSystemDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.*;

public final class AuralithAPI
{
    private AuralithAPI() {}

    private static final Map<ResourceLocation, SolarSystemDefinition> SYSTEMS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, DimensionBinding> DIM_BINDINGS = new HashMap<>();

    public record DimensionBinding(ResourceLocation systemId, ResourceLocation planetId)
    {
        public boolean isSpaceDimension() { return planetId == null; }
    }

    public static void registerSolarSystem(ResourceLocation id, SolarSystemDefinition definition)
    {
        SYSTEMS.put(id, definition);

        for (PlanetDefinition planet : definition.planets())
            planet.dimension().ifPresent(dimId -> DIM_BINDINGS.put(dimId, new DimensionBinding(id, planet.id())));

        definition.spaceDimension().ifPresent(spaceDim -> DIM_BINDINGS.put(spaceDim, new DimensionBinding(id, null)));
    }

    public static void bindDimensionToPlanet(ResourceLocation dimensionId, ResourceLocation systemId, ResourceLocation planetId)
    {
        DIM_BINDINGS.put(dimensionId, new DimensionBinding(systemId, planetId));
    }

    public static Optional<DimensionBinding> getBindingForDimension(ResourceKey<Level> dimension)
    {
        return Optional.ofNullable(DIM_BINDINGS.get(dimension.location()));
    }

    public static Optional<SolarSystemDefinition> getSolarSystem(ResourceLocation id)
    {
        return Optional.ofNullable(SYSTEMS.get(id));
    }

    public static Map<ResourceLocation, SolarSystemDefinition> getAllSystems()
    {
        return Collections.unmodifiableMap(SYSTEMS);
    }

    public static boolean hasSkyFor(ResourceKey<Level> dimension)
    {
        return DIM_BINDINGS.containsKey(dimension.location());
    }

    public static void clearAll()
    {
        SYSTEMS.clear();
        DIM_BINDINGS.clear();
    }
}
