package cute.ame.auralithpioneerinitiative.Data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record MoonDefinition(
    ResourceLocation id,
    Optional<ResourceLocation>       texture,
    Optional<ProceduralPlanetConfig> procedural,
    float size,
    float axialRotationSpeed,
    float axialTilt,
    OrbitDefinition orbit,
    Optional<AtmosphereDefinition>   atmosphere
)
{
    public static final Codec<MoonDefinition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(MoonDefinition::id),
            ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(MoonDefinition::texture),
            ProceduralPlanetConfig.CODEC.optionalFieldOf("procedural").forGetter(MoonDefinition::procedural),
            Codec.FLOAT.fieldOf("size").forGetter(MoonDefinition::size),
            Codec.FLOAT.optionalFieldOf("axial_rotation_speed", 0.0f).forGetter(MoonDefinition::axialRotationSpeed),
            Codec.FLOAT.optionalFieldOf("axial_tilt", 0.0f).forGetter(MoonDefinition::axialTilt),
            OrbitDefinition.CODEC.fieldOf("orbit").forGetter(MoonDefinition::orbit),
            AtmosphereDefinition.CODEC.optionalFieldOf("atmosphere").forGetter(MoonDefinition::atmosphere)
        ).apply(instance, MoonDefinition::new)
    );

    public ResourceLocation resolveTexture()
    {
        return procedural.map(proceduralPlanetConfig -> PlanetTextureGenerator.getOrGenerate(proceduralPlanetConfig, id.toString())).orElseGet(() -> texture.orElse(ResourceLocation.withDefaultNamespace("missingno")));
    }
}