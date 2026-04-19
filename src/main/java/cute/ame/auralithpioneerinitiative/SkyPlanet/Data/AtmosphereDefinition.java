package cute.ame.auralithpioneerinitiative.SkyPlanet.Data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record AtmosphereDefinition(Optional<Boolean> breathable, float scale, List<Float> color, float opacity, float fresnelPower)
{
    public static final Codec<AtmosphereDefinition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.BOOL.optionalFieldOf("breathable").forGetter(AtmosphereDefinition::breathable),
            Codec.FLOAT.optionalFieldOf("scale", 1.12f).forGetter(AtmosphereDefinition::scale),
            Codec.FLOAT.listOf().optionalFieldOf("color", List.of(0.4f, 0.6f, 1.0f)).forGetter(AtmosphereDefinition::color),
            Codec.FLOAT.optionalFieldOf("opacity", 0.80f).forGetter(AtmosphereDefinition::opacity),
            Codec.FLOAT.optionalFieldOf("fresnel_power", 3.0f).forGetter(AtmosphereDefinition::fresnelPower)
        ).apply(instance, AtmosphereDefinition::new)
    );

    public float r() { return get(0, 0.4f); }
    public float g() { return get(1, 0.6f); }
    public float b() { return get(2, 1.0f); }

    private float get(int idx, float fallback)
    {
        return (color != null && color.size() > idx) ? color.get(idx) : fallback;
    }
}