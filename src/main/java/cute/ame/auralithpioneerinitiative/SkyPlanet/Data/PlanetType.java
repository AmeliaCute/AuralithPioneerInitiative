package cute.ame.auralithpioneerinitiative.SkyPlanet.Data;

import com.mojang.serialization.Codec;

public enum PlanetType
{
  ROCKY,
  GAS_GIANT,
  OCEAN,
  ICE,
  LAVA,
  TELLURIC;

  public static final Codec<PlanetType> CODEC = Codec.STRING.xmap(
    s -> PlanetType.valueOf(s.toUpperCase(java.util.Locale.ROOT)),
    e -> e.name().toLowerCase(java.util.Locale.ROOT)
  );
}
