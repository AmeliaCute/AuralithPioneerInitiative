package cute.ame.auralithpioneerinitiative.Client.HUD;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public final class HoloPanelRegistry
{
  private static final Logger LOGGER = LogUtils.getLogger();
  private static final HoloPanelRegistry INSTANCE = new HoloPanelRegistry();

  public static HoloPanelRegistry getInstance() { return INSTANCE; }

  private final Map<ResourceLocation, HoloPanelType> panels = new LinkedHashMap<>();
  private final Map<ResourceLocation, RenderTarget> fbos = new HashMap<>();

  private HoloPanelRegistry() {}

  public void register(HoloPanelType panel)
  {
    panels.put(panel.getId(), panel);
    LOGGER.debug("[Auralith] Registered HoloPanelType: {}", panel.getId());
  }

  public Optional<HoloPanelType> get(ResourceLocation id)
  { return Optional.ofNullable(panels.get(id)); }

  public Collection<HoloPanelType> all() { return Collections.unmodifiableCollection(panels.values()); }

  public RenderTarget getOrCreateFBO(HoloPanelType panel)
  {
    return fbos.computeIfAbsent(panel.getId(), id ->
    {
      var target = new TextureTarget(panel.getWidth(), panel.getHeight(), false, Minecraft.ON_OSX);
      target.setClearColor(0f, 0f, 0f, 0f);
      LOGGER.debug("[Auralith] Created FBO {}×{} for {}", panel.getWidth(), panel.getHeight(), id);
      return target;
    });
  }

  public void destroyAllFBOs()
  {
    for (RenderTarget rt : fbos.values()) rt.destroyBuffers();
    fbos.clear();
    LOGGER.debug("[Auralith] Destroyed all HoloPanel FBOs");
  }

  public void destroyFBO(ResourceLocation id)
  {
    RenderTarget rt = fbos.remove(id);
    if (rt != null) rt.destroyBuffers();
  }
}