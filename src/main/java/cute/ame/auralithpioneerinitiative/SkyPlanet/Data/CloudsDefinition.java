package cute.ame.auralithpioneerinitiative.SkyPlanet.Data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record CloudsDefinition(
ResourceLocation texture,
float scale,
float rotationSpeed
)
{
  public static final Codec<CloudsDefinition> CODEC = RecordCodecBuilder.create(instance ->
    instance.group(
      ResourceLocation.CODEC.fieldOf("texture").forGetter(CloudsDefinition::texture),
      Codec.FLOAT.optionalFieldOf("scale", 1.06f).forGetter(CloudsDefinition::scale),
      Codec.FLOAT.optionalFieldOf("rotation_speed", 0.3f).forGetter(CloudsDefinition::rotationSpeed)
    ).apply(instance, CloudsDefinition::new)
  );
}
