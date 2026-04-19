package cute.ame.auralithpioneerinitiative.SkyPlanet.Data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cute.ame.auralithpioneerinitiative.SkyPlanet.Texture.PlanetTextureHelper;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record PlanetDefinition(
    ResourceLocation                 id,
    float                            size,
    float                            axialRotationSpeed,
    float                            axialTilt,
    OrbitDefinition                  orbit,
    Optional<ProceduralPlanetConfig> procedural,
    Optional<AtmosphereDefinition>   atmosphere,
    Optional<CloudsDefinition>       clouds,
    Optional<RingDefinition>         rings,
    List<PlanetDefinition>           moons,
    Optional<ResourceLocation>       dimension,
    float                            gravity,
    Optional<ResourceLocation>       orbitDimension
)
{
  private static final ResourceLocation MISSING = ResourceLocation.withDefaultNamespace("missingno");
  private static final PlanetTextureHelper.CubemapTextures MISSING_CUBEMAP =
      new PlanetTextureHelper.CubemapTextures(MISSING, MISSING, MISSING, MISSING, MISSING, MISSING);

  public static final Codec<PlanetDefinition> CODEC = RecordCodecBuilder.create(instance ->
      instance.group(
          ResourceLocation.CODEC.fieldOf("id").forGetter(PlanetDefinition::id),
          Codec.FLOAT.fieldOf("size").forGetter(PlanetDefinition::size),
          Codec.FLOAT.optionalFieldOf("axial_rotation_speed", 1.0f).forGetter(PlanetDefinition::axialRotationSpeed),
          Codec.FLOAT.optionalFieldOf("axial_tilt", 0.0f).forGetter(PlanetDefinition::axialTilt),
          OrbitDefinition.CODEC.fieldOf("orbit").forGetter(PlanetDefinition::orbit),
          ProceduralPlanetConfig.CODEC.optionalFieldOf("procedural").forGetter(PlanetDefinition::procedural),
          AtmosphereDefinition.CODEC.optionalFieldOf("atmosphere").forGetter(PlanetDefinition::atmosphere),
          CloudsDefinition.CODEC.optionalFieldOf("clouds").forGetter(PlanetDefinition::clouds),
          RingDefinition.CODEC.optionalFieldOf("rings").forGetter(PlanetDefinition::rings),
          Codec.lazyInitialized(() -> PlanetDefinition.CODEC).listOf().optionalFieldOf("moons", List.of()).forGetter(PlanetDefinition::moons),
          ResourceLocation.CODEC.optionalFieldOf("dimension").forGetter(PlanetDefinition::dimension),
          Codec.FLOAT.optionalFieldOf("gravity", 1.0f).forGetter(PlanetDefinition::gravity),
          ResourceLocation.CODEC.optionalFieldOf("orbit_dimension").forGetter(PlanetDefinition::orbitDimension)
      ).apply(instance, PlanetDefinition::new)
  );

  public PlanetTextureHelper.CubemapTextures resolveTexture()
  {
    return procedural
        .map(cfg -> PlanetTextureHelper.getOrGenerate(cfg, id.toString()))
        .orElse(MISSING_CUBEMAP);
  }
}