package cute.ame.auralithpioneerinitiative.SpaceSuit.Data;

public enum O2TankTier
{

  BASIC ("basic", 6_000),
  ADVANCED("advanced", 18_000),
  ELITE ("elite", 54_000);

  public final String id;
  public final int capacity;

  O2TankTier(String id, int capacity)
  {
    this.id = id;
    this.capacity = capacity;
  }
}