package cute.ame.auralithpioneerinitiative.Data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record PlanetDefinition(
        ResourceLocation id,
        Optional<ResourceLocation>       texture,
        float size,
        float axialRotationSpeed,
        float axialTilt,
        OrbitDefinition orbit,
        Optional<ProceduralPlanetConfig> procedural,
        Optional<AtmosphereDefinition>   atmosphere,
        Optional<CloudsDefinition>       clouds,
        Optional<RingDefinition>         rings,
        List<MoonDefinition>             moons,
        Optional<ResourceLocation>       dimension
) {
    public static final Codec<PlanetDefinition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(PlanetDefinition::id),
            ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(PlanetDefinition::texture),
            Codec.FLOAT.fieldOf("size").forGetter(PlanetDefinition::size),
            Codec.FLOAT.optionalFieldOf("axial_rotation_speed", 1.0f).forGetter(PlanetDefinition::axialRotationSpeed),
            Codec.FLOAT.optionalFieldOf("axial_tilt",            0.0f).forGetter(PlanetDefinition::axialTilt),
            OrbitDefinition.CODEC.fieldOf("orbit").forGetter(PlanetDefinition::orbit),
            ProceduralPlanetConfig.CODEC.optionalFieldOf("procedural").forGetter(PlanetDefinition::procedural),
            AtmosphereDefinition.CODEC.optionalFieldOf("atmosphere").forGetter(PlanetDefinition::atmosphere),
            CloudsDefinition.CODEC.optionalFieldOf("clouds").forGetter(PlanetDefinition::clouds),
            RingDefinition.CODEC.optionalFieldOf("rings").forGetter(PlanetDefinition::rings),
            MoonDefinition.CODEC.listOf().optionalFieldOf("moons", List.of()).forGetter(PlanetDefinition::moons),
            ResourceLocation.CODEC.optionalFieldOf("dimension").forGetter(PlanetDefinition::dimension)
        ).apply(instance, PlanetDefinition::new)
    );

    public ResourceLocation resolveTexture()
    {
        return procedural.map(proceduralPlanetConfig -> PlanetTextureGenerator.getOrGenerate(proceduralPlanetConfig, id.toString())).orElseGet(() -> texture.orElse(ResourceLocation.withDefaultNamespace("missingno")));
    }
}