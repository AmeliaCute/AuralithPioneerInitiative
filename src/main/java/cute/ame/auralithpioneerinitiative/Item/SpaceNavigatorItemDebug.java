package cute.ame.auralithpioneerinitiative.Item;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import cute.ame.auralithpioneerinitiative.API.AuralithAPI;
import cute.ame.auralithpioneerinitiative.SkyPlanet.Data.PlanetDefinition;
import cute.ame.auralithpioneerinitiative.SkyPlanet.Data.SolarSystemDefinition;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SpaceNavigatorItemDebug extends Item
{
  private static final double MIN_DOT = 0.90;
  private static final double ORBIT_SPAWN_Y = 256.0;
  private static final int WARP_COOLDOWN = 100;

  public SpaceNavigatorItemDebug(Properties arg0) { super(arg0); }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand)
  {
    ItemStack stack = player.getItemInHand(usedHand);

    if (level.isClientSide()) return InteractionResultHolder.success(stack);
    if (!(player instanceof ServerPlayer sp)) return InteractionResultHolder.pass(stack);

    Optional<AuralithAPI.DimensionBinding> bindingOpt = AuralithAPI.getBindingForDimension(level.dimension());
    if (bindingOpt.isEmpty() || !bindingOpt.get().isSpaceDimension())
    {
      player.sendSystemMessage(Component.literal("Only usable in the space dimension."));
      return InteractionResultHolder.fail(stack);
    }

    AuralithAPI.DimensionBinding binding = bindingOpt.get();
    Optional<SolarSystemDefinition> systemOpt = AuralithAPI.getSolarSystem(binding.systemId());
    if (systemOpt.isEmpty()) return InteractionResultHolder.fail(stack);

    SolarSystemDefinition system = systemOpt.get();
    long tick = level.getGameTime();
    Vec3 look = player.getLookAngle().normalize();

    PlanetDefinition target = null;
    double bestDot = MIN_DOT - 0.001;


    for (PlanetDefinition planet : system.planets())
    {
      if (planet.orbitDimension().isEmpty()) continue;

      double angle = planet.orbit().computeAngle(tick, 0f);
      double radius = planet.orbit().computeCurrentRadius(angle);
      float[] pos = planet.orbit().compute3DPosition(angle, radius, 1.0f);
      float len = (float) Math.sqrt(pos[0] * pos[0] + pos[1] * pos[1] + pos[2] * pos[2]);
      if (len < 1e-6f) continue;

      double dot = look.dot(new Vec3(pos[0] / len, pos[1] / len, pos[2] / len));
      if (dot > bestDot)
      {
        bestDot = dot;
        target = planet;
      }
    }

    if (target == null)
    {
      player.sendSystemMessage(Component.literal("No planet in range, aim directly at a planet in the sky."));
      return InteractionResultHolder.success(stack);
    }

    ResourceLocation orbitId  = target.orbitDimension().get();
    ResourceKey<Level> orbitKey = ResourceKey.create(Registries.DIMENSION, orbitId);
    ServerLevel orbitLevel = sp.getServer().getLevel(orbitKey);
    if (orbitLevel == null)
    {
      player.sendSystemMessage(Component.literal("Orbit dimension not loaded: " + orbitId));
      return InteractionResultHolder.fail(stack);
    }

    player.sendSystemMessage(Component.literal("Warping to orbit of " + target.id().getPath()));
    sp.teleportTo(orbitLevel, 0.5, ORBIT_SPAWN_Y, 0.5, Set.of(), sp.getYRot(), sp.getXRot());
    sp.getCooldowns().addCooldown(this, WARP_COOLDOWN);

    return InteractionResultHolder.success(stack);
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag)
  {
    tooltip.add(Component.literal("Aim at a planet and right-click to warp to its orbit."));
    tooltip.add(Component.literal("Only works in the space dimension."));
  }

  @Override
  public int getUseDuration(ItemStack stack, net.minecraft.world.entity.LivingEntity entity)
  {
    return 1;
  }
}
