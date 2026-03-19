package cute.ame.auralithpioneerinitiative.Client.HUD.Widget;

import cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;

public record HoloPanelContext(
  @Nullable ShipEntity ship,
  ClientLevel level,
  Camera camera,
  float partialTick,
  @Nullable BlockPos blockPos
)
{}
