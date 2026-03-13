package cute.ame.auralithpioneerinitiative.Ship.Data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ShipAnimationConfig(
    String animationType,
    BlockOffset offset
)
{
    public static final Codec<ShipAnimationConfig> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("animation_type").forGetter(ShipAnimationConfig::animationType),
            BlockOffset.CODEC.optionalFieldOf("offset", BlockOffset.ZERO).forGetter(ShipAnimationConfig::offset)
        ).apply(instance, ShipAnimationConfig::new)
    );
}
