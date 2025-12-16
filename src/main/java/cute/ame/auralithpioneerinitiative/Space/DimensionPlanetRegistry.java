package cute.ame.auralithpioneerinitiative.Space;

import cute.ame.auralithpioneerinitiative.Space.Body.CelestialPlanet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry that binds dimensions to planets for rendering perspective
 */
public class DimensionPlanetRegistry
{
    private static final Map<ResourceKey<Level>, CelestialPlanet> DIMENSION_TO_PLANET = new HashMap<>();
    private static final Map<CelestialPlanet, ResourceKey<Level>> PLANET_TO_DIMENSION = new HashMap<>();

    /**
     * Bind a dimension to a planet
     * @param dimension The dimension key
     * @param planet The planet that represents this dimension
     */
    public static void bindDimensionToPlanet(ResourceKey<Level> dimension, CelestialPlanet planet)
    {
        DIMENSION_TO_PLANET.put(dimension, planet);
        PLANET_TO_DIMENSION.put(planet, dimension);
    }

    /**
     * Get the planet associated with a dimension
     * @param dimension The dimension key
     * @return The planet, or null if not bound
     */
    public static CelestialPlanet getPlanetForDimension(ResourceKey<Level> dimension)
    {
        return DIMENSION_TO_PLANET.get(dimension);
    }

    /**
     * Get the dimension associated with a planet
     * @param planet The planet
     * @return The dimension key, or null if not bound
     */
    public static ResourceKey<Level> getDimensionForPlanet(CelestialPlanet planet)
    {
        return PLANET_TO_DIMENSION.get(planet);
    }

    /**
     * Check if a dimension has a bound planet
     */
    public static boolean hasPlanetForDimension(ResourceKey<Level> dimension)
    {
        return DIMENSION_TO_PLANET.containsKey(dimension);
    }

    /**
     * Remove a dimension-planet binding
     */
    public static void unbindDimension(ResourceKey<Level> dimension)
    {
        CelestialPlanet planet = DIMENSION_TO_PLANET.remove(dimension);
        if (planet != null)
        {
            PLANET_TO_DIMENSION.remove(planet);
        }
    }

    /**
     * Clear all bindings
     */
    public static void clearAll()
    {
        DIMENSION_TO_PLANET.clear();
        PLANET_TO_DIMENSION.clear();
    }
}