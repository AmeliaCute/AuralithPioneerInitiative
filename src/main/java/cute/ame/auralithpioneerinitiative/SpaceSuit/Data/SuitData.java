package cute.ame.auralithpioneerinitiative.SpaceSuit.Data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class SuitData {

  public static final int O2_MAX = 6_000;
  public static final int ENERGY_MAX = 3_600;

  private int o2Level = O2_MAX;
  private int energyLevel = ENERGY_MAX;
  private boolean flashlight = false;
  private boolean helmetOn = false;

  public SuitData() {}

  private SuitData(int o2, int energy, boolean fl, boolean helmet) {
    o2Level = o2; energyLevel = energy; flashlight = fl; helmetOn = helmet;
  }

  public static final Codec<SuitData> CODEC = RecordCodecBuilder.create(i -> i.group(
    Codec.INT .fieldOf("o2") .forGetter(d -> d.o2Level),
    Codec.INT .fieldOf("energy") .forGetter(d -> d.energyLevel),
    Codec.BOOL.fieldOf("flashlight").forGetter(d -> d.flashlight),
    Codec.BOOL.fieldOf("helmet") .forGetter(d -> d.helmetOn)
  ).apply(i, SuitData::new));

  public int getO2() { return o2Level; }
  public int getEnergy() { return energyLevel; }
  public boolean isFlashlight() { return flashlight; }
  public boolean isHelmetOn() { return helmetOn; }
  public float o2Frac() { return (float) o2Level / O2_MAX; }
  public float energyFrac() { return (float) energyLevel / ENERGY_MAX; }

  public void setO2(int v) { o2Level = clamp(v, 0, O2_MAX); }
  public void setEnergy(int v) { energyLevel = clamp(v, 0, ENERGY_MAX); }
  public void setFlashlight(boolean v){ flashlight  = v; }
  public void setHelmetOn(boolean v) { helmetOn = v; }
  public void drainO2(int n) { setO2(o2Level - n); }
  public void drainEnergy(int n) { setEnergy(energyLevel - n); }

  private static int clamp(int v, int lo, int hi)
  {
    return v < lo ? lo : Math.min(v, hi);
  }
}