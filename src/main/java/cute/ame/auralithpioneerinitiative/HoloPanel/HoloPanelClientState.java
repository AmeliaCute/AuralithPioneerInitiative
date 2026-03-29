package cute.ame.auralithpioneerinitiative.HoloPanel;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public final class HoloPanelClientState
{
  private static final HoloPanelClientState INSTANCE = new HoloPanelClientState();
  public static HoloPanelClientState getInstance() { return INSTANCE; }

  private final Map<BlockPos, CompoundTag> blockData = new HashMap<>();
  private final Map<String, String> sharedState = new HashMap<>();

  private HoloPanelClientState() {}

  public void update(BlockPos pos, CompoundTag data) { blockData.put(pos, data); }
  public CompoundTag get(BlockPos pos) { return blockData.getOrDefault(pos, new CompoundTag()); }
  public void remove(BlockPos pos) { blockData.remove(pos); }

  public void setState(String key, String value) { sharedState.put(key, value); }
  public String getState(String key) { return sharedState.getOrDefault(key, ""); }
  public void removeState(String key) { sharedState.remove(key); }

  public void clear()
  {
    blockData.clear();
    sharedState.clear();
  }
}