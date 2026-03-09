package cute.ame.auralithpioneerinitiative.Data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record SolarSystemDefinition(
    SunDefinition sun,
    List<PlanetDefinition> planets,
    Optional<ResourceLocation> spaceDimension
)
{
    public static final Codec<SolarSystemDefinition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            SunDefinition.CODEC.fieldOf("sun").forGetter(SolarSystemDefinition::sun),
            PlanetDefinition.CODEC.listOf().fieldOf("planets").forGetter(SolarSystemDefinition::planets),
            ResourceLocation.CODEC.optionalFieldOf("space_dimension").forGetter(SolarSystemDefinition::spaceDimension)
        ).apply(instance, SolarSystemDefinition::new)
    );
}