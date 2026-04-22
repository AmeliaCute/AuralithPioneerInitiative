package cute.ame.auralithpioneerinitiative.vehicle.Types;

import cute.ame.auralithpioneerinitiative.API.AuralithAPI;
import cute.ame.auralithpioneerinitiative.vehicle.Baking.VehicleSnapshot;
import cute.ame.auralithpioneerinitiative.vehicle.Input.FlightInput;
import cute.ame.auralithpioneerinitiative.vehicle.Physics.CapabilityScanner;
import cute.ame.auralithpioneerinitiative.vehicle.Physics.DynamicPhysics;
import cute.ame.auralithpioneerinitiative.vehicle.Physics.VehicleCapabilities;
import cute.ame.auralithpioneerinitiative.vehicle.Entity.BlockVehicleEntity;
import cute.ame.auralithpioneerinitiative.vehicle.Entity.VehicleSeat;
import cute.ame.auralithpioneerinitiative.vehicle.Entity.VehicleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.*;

public class SpaceshipEntity extends BlockVehicleEntity
{
  private final DynamicPhysics physics = new DynamicPhysics();
  private VehicleCapabilities capabilities = null;

  private FlightInput lastInput = FlightInput.IDLE;
  private int inputAge  = 0;
  private static final int INPUT_TIMEOUT_TICKS = 60;
  private UUID pilotUUID = null;

  public SpaceshipEntity(EntityType<?> type, Level level)
  {
    super(type, level);
    this.noPhysics = true;
  }

  @Override
  public VehicleType getVehicleType() { return VehicleType.SPACESHIP; }

  @Override
  public void setBlockSnapshot(List<BlockVehicleEntity.BlockEntry> entries)
  {
    super.setBlockSnapshot(entries);
    if (!level().isClientSide())
      rescanCapabilities();
  }

  private void rescanCapabilities()
  {
    List<BlockEntry> snap = getBlockSnapshot();
    if (snap.isEmpty())
    {
      capabilities = VehicleCapabilities.empty(500f);
      return;
    }
    List<VehicleSnapshot.BlockEntry> scanEntries = new ArrayList<>(snap.size());
    for (BlockEntry e : snap)
    {
      scanEntries.add(new VehicleSnapshot.BlockEntry(e.relX(), e.relY(), e.relZ(), e.stateId()));
    }
    capabilities = CapabilityScanner.scan(scanEntries);
  }

  private VehicleCapabilities getCaps()
  {
    if (capabilities == null) rescanCapabilities();
    return capabilities;
  }


  public Optional<UUID> getPilotUUID() { return Optional.ofNullable(pilotUUID); }
  public void setPilotUUID(UUID uuid) { this.pilotUUID = uuid; }
  public boolean hasPilot() { return pilotUUID != null; }

  public void applyFlightInput(FlightInput input)
  {
    this.lastInput = input;
    this.inputAge = 0;
  }

  @Override
  public void serverTick()
  {
    boolean pilotPresent = hasPilot();

    if (pilotPresent)
    {
      inputAge++;
      if (inputAge > INPUT_TIMEOUT_TICKS) lastInput = FlightInput.IDLE;

      if (lastInput.dismounting())
      {
        Player pilot = level().getPlayerByUUID(pilotUUID);
        if (pilot != null) pilot.stopRiding();

        lastInput = FlightInput.IDLE;
      }
    }

    VehicleCapabilities caps = getCaps();

    if (pilotPresent || physics.isMoving())
    {
      float gravity = AuralithAPI.getGravityFor(level().dimension());
      physics.integrate(this, caps, pilotPresent ? lastInput : FlightInput.IDLE, gravity);
    }

    carryPassengers();
    super.serverTick();
  }

  private void carryPassengers()
  {
    for (var rawSeat : getSeats())
    {
      if (!(rawSeat instanceof VehicleSeat seat)) continue;

      UUID occ = seat.getOccupantUUID();
      if (occ == null) continue;

      Player player = level().getPlayerByUUID(occ);
      if (player == null || player.getVehicle() != this) continue;

      var pos = seat.getWorldPosition(this);
      player.setPos(pos.x, pos.y, pos.z);
    }
  }

  @Override
  protected boolean isMoving() { return physics.isMoving(); }

  @Override
  public boolean startRiding(Entity vehicle, boolean force)
  {
    return super.startRiding(vehicle, force);
  }

  @Override
  public void positionRider(Entity passenger, Entity.MoveFunction moveFunc)
  {
    if (!hasPassenger(passenger)) return;

    net.minecraft.world.phys.Vec3 worldPos = null;
    if (passenger instanceof Player player)
    {
      var raw = getSeatOf(player);
      if (raw instanceof VehicleSeat seat) worldPos = seat.getWorldPosition(this);
    }

    if (worldPos == null) worldPos = position().add(0, 1.0, 0);
    moveFunc.accept(passenger, worldPos.x, worldPos.y, worldPos.z);
  }

  @Override
  public void removePassenger(Entity passenger)
  {
    super.removePassenger(passenger);
    if (passenger instanceof Player p && p.getUUID().equals(pilotUUID))
    {
      pilotUUID = null;
      lastInput = FlightInput.IDLE;
      inputAge = 0;
    }
  }

  @Override public boolean isPickable() { return true; }
  @Override public boolean canBeCollidedWith() { return true; }

  public DynamicPhysics getPhysics() { return physics; }
  public VehicleCapabilities getCapabilities(){ return getCaps(); }

  public float getCurrentFuel() { return getCaps().currentFuel; }
  public float getTotalFuelCapacity() { return getCaps().totalFuelCapacity; }

  @Override
  protected void readAdditionalSaveData(CompoundTag tag)
  {
    super.readAdditionalSaveData(tag);
    if (tag.hasUUID("pilotUUID")) pilotUUID = tag.getUUID("pilotUUID");

    physics.load(tag);
    float savedFuel = tag.contains("currentFuel") ? tag.getFloat("currentFuel") : -1f;
    rescanCapabilities();
    if (savedFuel >= 0 && capabilities != null) capabilities.currentFuel = savedFuel;
  }

  @Override
  protected void addAdditionalSaveData(CompoundTag tag)
  {
    super.addAdditionalSaveData(tag);
    if (pilotUUID != null) tag.putUUID("pilotUUID", pilotUUID);

    physics.save(tag);
    if (capabilities != null) tag.putFloat("currentFuel", capabilities.currentFuel);
  }
}