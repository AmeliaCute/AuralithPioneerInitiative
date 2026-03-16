package cute.ame.auralithpioneerinitiative.Mixin.Player;

import cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity;
import cute.ame.auralithpioneerinitiative.Ship.Network.ShipSnapshotPacket;
import cute.ame.auralithpioneerinitiative.Ship.Physics.ShipTransformData;
import cute.ame.auralithpioneerinitiative.Ship.Physics.ShipTransformRegistry;
import cute.ame.auralithpioneerinitiative.Ship.Renderer.ShipSnapshotBlockView;
import cute.ame.auralithpioneerinitiative.Ship.Shipyard.ShipyardAllocator;
import cute.ame.auralithpioneerinitiative.Ship.Shipyard.ShipyardBlockView;
import cute.ame.auralithpioneerinitiative.Ship.Shipyard.ShipyardManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityMoveMixin
{
  private static final ThreadLocal<Boolean> IN_SHIP_MOVE = ThreadLocal.withInitial(() -> false);

  @Inject(method = "move", at = @At("HEAD"), cancellable = true)
  private void auralith$interceptMove(MoverType type, Vec3 movement, CallbackInfo ci)
  {
    if (IN_SHIP_MOVE.get()) return;

    Entity self = (Entity)(Object) this;
    if (!(self instanceof LivingEntity)) return;
    if (self instanceof ShipEntity)      return;
    if (self.isPassenger())              return;
    if (self instanceof Player player && player.isSpectator()) return;
    if (movement.lengthSqr() < 1e-12)   return;

    Optional<ShipTransformData> shipOpt = ShipTransformRegistry.findContaining(self.position());
    if (shipOpt.isEmpty()) return;
    ShipTransformData ship = shipOpt.get();

    BlockGetter view = getBlockView(self, ship.shipId());
    if (view == null) return;

    IN_SHIP_MOVE.set(true);
    try
    {
      CollisionContext ctx = CollisionContext.of(self);
      Vec3 localPos = ship.toShip(self.position());

      Vector3f mv = new Vector3f((float)movement.x, (float)movement.y, (float)movement.z);
      new Quaternionf(ship.rotation()).conjugate().transform(mv);

      float hw = self.getBbWidth() * 0.5f;
      float h  = self.getBbHeight();

      Vec3 resolved = resolveMovement(localPos, new Vec3(mv.x, mv.y, mv.z), hw, h, view, ctx);

      Vec3 newWorldPos = ship.toWorld(resolved);
      self.setPos(newWorldPos.x, newWorldPos.y, newWorldPos.z);

      Vec3 delta = resolved.subtract(localPos);
      Vector3f worldDelta = new Vector3f((float)delta.x, (float)delta.y, (float)delta.z);
      ship.rotation().transform(worldDelta);
      self.setDeltaMovement(worldDelta.x, worldDelta.y, worldDelta.z);

      self.horizontalCollision = Math.abs(delta.x) < Math.abs(mv.x)
          || Math.abs(delta.z) < Math.abs(mv.z);
      self.verticalCollision   = Math.abs(delta.y - mv.y) > 1e-5;
      self.setOnGround(self.verticalCollision && mv.y < 0);

      ci.cancel();
    }
    finally { IN_SHIP_MOVE.set(false); }
  }

  private static BlockGetter getBlockView(Entity self, UUID shipId)
  {
    if (!self.level().isClientSide())
    {
      try
      {
        ServerLevel sy = ShipyardManager.getShipyardLevel(
            ((ServerLevel) self.level()).getServer());
        ShipyardAllocator alloc = ShipyardAllocator.getOrCreate(sy);
        if (!alloc.has(shipId)) return null;
        return new ShipyardBlockView(sy, alloc.getSlot(shipId));
      }
      catch (Exception e) { return null; }
    }
    else
    {
      ClientLevel clientLevel = (ClientLevel) self.level();
      for (Entity e : clientLevel.entitiesForRendering())
      {
        if (e instanceof ShipEntity ship && ship.getUUID().equals(shipId))
        {
          List<ShipSnapshotPacket.BlockEntry> snapshot = ship.getBlockSnapshot();
          if (snapshot.isEmpty()) return null;
          return ShipSnapshotBlockView.from(snapshot);
        }
      }
      return null;
    }
  }

  private Vec3 resolveMovement(Vec3 localPos, Vec3 localMove, float hw, float h, BlockGetter view, CollisionContext ctx)
  {
    double dx = localMove.x, dy = localMove.y, dz = localMove.z;
    AABB bb = new AABB(localPos.x - hw, localPos.y, localPos.z - hw,
        localPos.x + hw, localPos.y + h, localPos.z + hw);

    if (dy != 0)
    {
      AABB probe = dy < 0 ? new AABB(bb.minX, bb.minY + dy, bb.minZ, bb.maxX, bb.minY, bb.maxZ) : new AABB(bb.minX, bb.maxY, bb.minZ, bb.maxX, bb.maxY + dy, bb.maxZ);

      if (hasBlockIn(view, probe, ctx))
        dy = binarySearchY(bb.minY, bb.maxY, dy, bb.minX, bb.minZ, bb.maxX, bb.maxZ, view, ctx);

      bb = bb.move(0, dy, 0);
    }

    if (dx != 0)
    {
      AABB probe = dx < 0 ? new AABB(bb.minX + dx, bb.minY, bb.minZ, bb.minX, bb.maxY, bb.maxZ) : new AABB(bb.maxX, bb.minY, bb.minZ, bb.maxX + dx, bb.maxY, bb.maxZ);
      if (hasBlockIn(view, probe, ctx)) dx = 0;
      bb = bb.move(dx, 0, 0);
    }

    if (dz != 0)
    {
      AABB probe = dz < 0 ? new AABB(bb.minX, bb.minY, bb.minZ + dz, bb.maxX, bb.maxY, bb.minZ) : new AABB(bb.minX, bb.minY, bb.maxZ, bb.maxX, bb.maxY, bb.maxZ + dz);
      if (hasBlockIn(view, probe, ctx)) dz = 0;
    }

    return localPos.add(dx, dy, dz);
  }

  private double binarySearchY(double bbMinY, double bbMaxY, double dy, double bx0, double bz0, double bx1, double bz1, BlockGetter view, CollisionContext ctx)
  {
    boolean goingDown = dy < 0;
    double lo = 0.0, hi = dy;

    for (int i = 0; i < 8; i++)
    {
      double mid = (lo + hi) * 0.5;
      AABB probe = goingDown ? new AABB(bx0, bbMinY + mid, bz0, bx1, bbMinY, bz1) : new AABB(bx0, bbMaxY, bz0, bx1, bbMaxY + mid, bz1);
      if (hasBlockIn(view, probe, ctx)) hi = mid;
      else lo = mid;
    }
    return lo;
  }

  private boolean hasBlockIn(BlockGetter view, AABB probe, CollisionContext ctx)
  {
    if (probe.minY >= probe.maxY || probe.minX >= probe.maxX || probe.minZ >= probe.maxZ)
      return false;

    int x0 = (int)Math.floor(probe.minX), x1 = (int)Math.ceil(probe.maxX);
    int y0 = (int)Math.floor(probe.minY), y1 = (int)Math.ceil(probe.maxY);
    int z0 = (int)Math.floor(probe.minZ), z1 = (int)Math.ceil(probe.maxZ);

    for (int bx = x0; bx < x1; bx++)
      for (int by = y0; by < y1; by++)
        for (int bz = z0; bz < z1; bz++)
        {
          BlockPos pos = new BlockPos(bx, by, bz);
          var state = view.getBlockState(pos);
          if (state.isAir()) continue;

          VoxelShape shape = state.getCollisionShape(view, pos, ctx);
          if (shape.isEmpty()) continue;

          for (AABB blockBB : shape.move(bx, by, bz).toAabbs())
            if (probe.intersects(blockBB)) return true;
        }
    return false;
  }
}