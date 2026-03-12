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

  public enum BindingType { SURFACE, ORBIT, SPACE }

  public record DimensionBinding(ResourceLocation systemId, ResourceLocation planetId, BindingType type)
  {
    public boolean isSpaceDimension() { return type == BindingType.SPACE; }
    public boolean isOrbitDimension() { return type == BindingType.ORBIT; }
    public boolean isSurfaceDimension(){ return type == BindingType.SURFACE; }
  }

  public static void registerSolarSystem(ResourceLocation id, SolarSystemDefinition definition)
  {
    SYSTEMS.put(id, definition);
    for (PlanetDefinition planet : definition.planets())
    {
      planet.dimension().ifPresent(dimId -> DIM_BINDINGS.put(dimId, new DimensionBinding(id, planet.id(), BindingType.SURFACE)));
      planet.orbitDimension().ifPresent(orbitId -> DIM_BINDINGS.put(orbitId, new DimensionBinding(id, planet.id(), BindingType.ORBIT)));

      for (PlanetDefinition moon : planet.moons())
      {
        moon.dimension().ifPresent(dimId -> DIM_BINDINGS.put(dimId, new DimensionBinding(id, moon.id(), BindingType.SURFACE)));
        moon.orbitDimension().ifPresent(orbitId -> DIM_BINDINGS.put(orbitId, new DimensionBinding(id, moon.id(), BindingType.ORBIT)));
      }
    }
    definition.spaceDimension().ifPresent(spaceDim -> DIM_BINDINGS.put(spaceDim, new DimensionBinding(id, null, BindingType.SPACE)));
  }

  public static void bindDimensionToPlanet(ResourceLocation dimensionId, ResourceLocation systemId, ResourceLocation planetId)
  {
    DIM_BINDINGS.put(dimensionId, new DimensionBinding(systemId, planetId, BindingType.SURFACE));
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

  public static float getGravityFor(ResourceKey<Level> dimension)
  {
    DimensionBinding binding = DIM_BINDINGS.get(dimension.location());
    if (binding == null) return 1.0f;
    if (binding.type() != BindingType.SURFACE) return 0.0f;

    SolarSystemDefinition system = SYSTEMS.get(binding.systemId());
    if (system == null) return 1.0f;

    return system.planets().stream().filter(p -> p.id().equals(binding.planetId())).findFirst().map(PlanetDefinition::gravity).orElse(1.0f);
  }

  public static void clearAll()
  {
    SYSTEMS.clear();
    DIM_BINDINGS.clear();
  }
}
