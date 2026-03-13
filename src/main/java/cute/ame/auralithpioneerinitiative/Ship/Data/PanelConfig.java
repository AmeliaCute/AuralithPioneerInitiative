package cute.ame.auralithpioneerinitiative.Ship.Data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record PanelConfig(
    ResourceLocation type,
    BlockOffset offset,
    String facing
)
{
    public static final Codec<PanelConfig> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ResourceLocation.CODEC.fieldOf("type").forGetter(PanelConfig::type),
            BlockOffset.CODEC.optionalFieldOf("offset", BlockOffset.ZERO).forGetter(PanelConfig::offset),
            Codec.STRING.optionalFieldOf("facing", "NORTH").forGetter(PanelConfig::facing)
        ).apply(instance, PanelConfig::new)
    );
}
