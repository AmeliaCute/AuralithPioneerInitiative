package cute.ame.auralithpioneerinitiative;

import com.mojang.logging.LogUtils;
import cute.ame.auralithpioneerinitiative.Registrie.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(Auralithpioneerinitiative.MODID)
public class Auralithpioneerinitiative
{
  public static final String MODID = "auralithpioneerinitiative";
  public static final Logger LOGGER = LogUtils.getLogger();

  public Auralithpioneerinitiative(IEventBus modEventBus, ModContainer modContainer)
  {
    ModItems.ITEMS.register(modEventBus);
    modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
  }
}
