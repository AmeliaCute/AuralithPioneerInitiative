package cute.ame.auralithpioneerinitiative.vehicle.Entity;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class AbstractVehicleEntity extends Entity
{
  protected Quaternionf vehicleRotation = new Quaternionf();
  private boolean boundsDirty = true;

  private final List<VehicleSeat> seats = new ArrayList<>();

  @OnlyIn(Dist.CLIENT) public Vec3 renderPrevPos;
  @OnlyIn(Dist.CLIENT) public Vec3 renderTargetPos;
  @OnlyIn(Dist.CLIENT) public Quaternionf renderPrevRot;
  @OnlyIn(Dist.CLIENT) public Quaternionf renderTargetRot;
  @OnlyIn(Dist.CLIENT) public int lerpSteps;

  protected AbstractVehicleEntity(EntityType<?> type, Level level)
  {
    super(type, level);
    if (level.isClientSide())
    {
      renderPrevPos = Vec3.ZERO;
      renderTargetPos = Vec3.ZERO;
      renderPrevRot = new Quaternionf();
      renderTargetRot = new Quaternionf();
      lerpSteps = 0;
    }
  }

  public abstract VehicleType getVehicleType();
  public abstract void serverTick();

  public Quaternionf getVehicleRotation() {return vehicleRotation;}
  public void setVehicleRotation(Quaternionf q)
  {
    vehicleRotation.set(q);
    boundsDirty = true;
  }

  public List<VehicleSeat> getSeats()
  {
    return seats;
  }

  public @Nullable VehicleSeat getSeatOf(Player player)
  {
    UUID id = player.getUUID();
    for (VehicleSeat seat : seats) if (id.equals(seat.getOccupantUUID())) return seat;

    return null;
  }

  public void addSeat(VehicleSeat seat)
  {
    seats.add(seat);
  }

  protected void clearSeats()
  {
    seats.clear();
  }

  @Override
  public void setPos(double x, double y, double z)
  {
    super.setPos(x, y, z);
    boundsDirty = true;
  }

  private AABB computeRotatedAABB()
  {
    AABB local = getLocalBounds();

    float[][] corners =
      {
        {(float)local.minX,(float)local.minY,(float)local.minZ},
        {(float)local.maxX,(float)local.minY,(float)local.minZ},
        {(float)local.minX,(float)local.maxY,(float)local.minZ},
        {(float)local.maxX,(float)local.maxY,(float)local.minZ},
        {(float)local.minX,(float)local.minY,(float)local.maxZ},
        {(float)local.maxX,(float)local.minY,(float)local.maxZ},
        {(float)local.minX,(float)local.maxY,(float)local.maxZ},
        {(float)local.maxX,(float)local.maxY,(float)local.maxZ},
    };

    float mnX = Float.MAX_VALUE, mnY = Float.MAX_VALUE, mnZ = Float.MAX_VALUE;
    float mxX = -Float.MAX_VALUE, mxY = -Float.MAX_VALUE, mxZ = -Float.MAX_VALUE;
    for (float[] c : corners)
    {
      Vector3f v = new Vector3f(c[0], c[1], c[2]);
      vehicleRotation.transform(v);
      if (v.x < mnX) mnX = v.x; if (v.x > mxX) mxX = v.x;
      if (v.y < mnY) mnY = v.y; if (v.y > mxY) mxY = v.y;
      if (v.z < mnZ) mnZ = v.z; if (v.z > mxZ) mxZ = v.z;
    }

    double ox = getX(), oy = getY(), oz = getZ();
    return new AABB(ox+mnX, oy+mnY, oz+mnZ, ox+mxX, oy+mxY, oz+mxZ);
  }

  private void refreshBB()
  {
    setBoundingBox(computeRotatedAABB());
    boundsDirty = false;
  }

  public AABB getLocalBounds()
  {
    return new AABB(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5);
  }

  @Override
  public void tick()
  {
    super.tick();

    if (boundsDirty)
    {
      refreshBB();
    }

    if (level().isClientSide()) clientLerpTick();
    else serverTick();
  }

  @OnlyIn(Dist.CLIENT)
  private void clientLerpTick()
  {
    if (lerpSteps <= 0) return;
    lerpSteps--;

    float t = lerpSteps == 0 ? 1.0f : 1.0f / (lerpSteps + 1);

    Vec3 pos = renderPrevPos.lerp(renderTargetPos, t);
    setPos(pos.x, pos.y, pos.z);

    Quaternionf rot = new Quaternionf(renderPrevRot).slerp(renderTargetRot, t);
    setVehicleRotation(rot);

    renderPrevPos = pos;
    renderPrevRot = rot;
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder)
  {
  }

  @Override
  protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag)
  {
    if (tag.contains("rotX"))
    {
      vehicleRotation.set(tag.getFloat("rotX"), tag.getFloat("rotY"), tag.getFloat("rotZ"), tag.getFloat("rotW"));
      boundsDirty = true;
    }
  }

  @Override
  protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag)
  {
    tag.putFloat("rotX", vehicleRotation.x);
    tag.putFloat("rotY", vehicleRotation.y);
    tag.putFloat("rotZ", vehicleRotation.z);
    tag.putFloat("rotW", vehicleRotation.w);
  }
}