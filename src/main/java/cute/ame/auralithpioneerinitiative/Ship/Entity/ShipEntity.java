package cute.ame.auralithpioneerinitiative.Ship.Entity;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Ship.Data.ShipDefinition;
import cute.ame.auralithpioneerinitiative.Ship.Data.ShipRegistry;
import cute.ame.auralithpioneerinitiative.Ship.Network.ShipSnapshotPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ShipEntity extends Entity
{
    public static final int SUBSYSTEM_ENGINE  = 0x01;
    public static final int SUBSYSTEM_REACTOR = 0x02;
    public static final int SUBSYSTEM_SHIELDS = 0x04;
    public static final int SUBSYSTEM_WEAPONS = 0x08;
    public static final int SUBSYSTEM_CARGO   = 0x10;

    private static final EntityDataAccessor<Float> DATA_HULL_INTEGRITY = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_SHIELD_STRENGTH = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_FUEL_LEVEL = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Long> DATA_EU_STORED = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<String> DATA_SHIP_CLASS = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Optional<UUID>> DATA_PILOT_UUID = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> DATA_SUBSYSTEM_STATE = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Quaternionf> DATA_ROTATION = SynchedEntityData.defineId(ShipEntity.class, ModEntities.QUATERNIONF);

    private List<ShipSnapshotPacket.BlockEntry> blockSnapshot = new ArrayList<>();

    public ShipEntity(EntityType<?> type, Level level)
    {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        builder.define(DATA_HULL_INTEGRITY, 1.0f);
        builder.define(DATA_SHIELD_STRENGTH, 1.0f);
        builder.define(DATA_FUEL_LEVEL, 1.0f);
        builder.define(DATA_EU_STORED, 0L);
        builder.define(DATA_SHIP_CLASS, "auralithpioneerinitiative:unknown");
        builder.define(DATA_PILOT_UUID, Optional.empty());
        builder.define(DATA_SUBSYSTEM_STATE, 0xFF);
        builder.define(DATA_ROTATION, new Quaternionf());
    }


    public float getHullIntegrity()  { return entityData.get(DATA_HULL_INTEGRITY);  }
    public float getShieldStrength() { return entityData.get(DATA_SHIELD_STRENGTH); }
    public float getFuelLevel() { return entityData.get(DATA_FUEL_LEVEL); }
    public long getEuStored() { return entityData.get(DATA_EU_STORED); }
    public String getShipClassId() { return entityData.get(DATA_SHIP_CLASS); }
    public Optional<UUID> getPilotUUID() { return entityData.get(DATA_PILOT_UUID); }
    public int getSubsystemState() { return entityData.get(DATA_SUBSYSTEM_STATE);  }
    public Quaternionf getShipRotation() { return entityData.get(DATA_ROTATION); }

    public void setHullIntegrity(float v) { entityData.set(DATA_HULL_INTEGRITY,  clamp01(v)); }
    public void setShieldStrength(float v) { entityData.set(DATA_SHIELD_STRENGTH, clamp01(v)); }
    public void setFuelLevel(float v) { entityData.set(DATA_FUEL_LEVEL, clamp01(v)); }
    public void setEuStored(long v) { entityData.set(DATA_EU_STORED, Math.max(0, v));   }
    public void setShipClassId(ResourceLocation id) { entityData.set(DATA_SHIP_CLASS, id.toString()); }
    public void setPilotUUID(UUID uuid) { entityData.set(DATA_PILOT_UUID, Optional.ofNullable(uuid)); }
    public void setSubsystemState(int mask) { entityData.set(DATA_SUBSYSTEM_STATE, mask); }
    public void setShipRotation(Quaternionf q) { entityData.set(DATA_ROTATION, new Quaternionf(q).normalize()); }

    private static float clamp01(float v) { return Math.max(0f, Math.min(1f, v)); }

    public Optional<ShipDefinition> getDefinition()
    {
        return ShipRegistry.get(ResourceLocation.parse(getShipClassId()));
    }

    public boolean isSubsystemOnline(int flag) { return (getSubsystemState() & flag) != 0; }
    public boolean hasPilot()   { return getPilotUUID().isPresent(); }
    public boolean isDestroyed(){ return getHullIntegrity() <= 0f; }

    public void setBlockSnapshot(List<ShipSnapshotPacket.BlockEntry> snapshot)
    {
        this.blockSnapshot = new ArrayList<>(snapshot);
    }

    public List<ShipSnapshotPacket.BlockEntry> getBlockSnapshot()
    {
        return blockSnapshot;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer connection)
    {
        super.startSeenByPlayer(connection);
        if (!blockSnapshot.isEmpty())
            PacketDistributor.sendToPlayer(connection, new ShipSnapshotPacket(this.getUUID(), blockSnapshot));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag)
    {
        setHullIntegrity(tag.getFloat("hull"));
        setShieldStrength(tag.getFloat("shield"));
        setFuelLevel(tag.getFloat("fuel"));
        setEuStored(tag.getLong("eu"));
        if (tag.contains("shipClass")) entityData.set(DATA_SHIP_CLASS, tag.getString("shipClass"));
        if (tag.hasUUID("pilotUUID")) setPilotUUID(tag.getUUID("pilotUUID"));
        setSubsystemState(tag.getInt("subsystems"));

        if (tag.contains("rotX")) setShipRotation(new Quaternionf(tag.getFloat("rotX"), tag.getFloat("rotY"), tag.getFloat("rotZ"), tag.getFloat("rotW")));
        if (tag.contains("snapX"))
        {
            int[] xs = tag.getIntArray("snapX");
            int[] ys = tag.getIntArray("snapY");
            int[] zs = tag.getIntArray("snapZ");
            int[] ss = tag.getIntArray("snapStates");
            blockSnapshot = new ArrayList<>(xs.length);
            for (int i = 0; i < xs.length; i++) blockSnapshot.add(new ShipSnapshotPacket.BlockEntry(xs[i], ys[i], zs[i], ss[i]));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag)
    {
        tag.putFloat("hull", getHullIntegrity());
        tag.putFloat("shield", getShieldStrength());
        tag.putFloat("fuel", getFuelLevel());
        tag.putLong("eu", getEuStored());
        tag.putString("shipClass", getShipClassId());
        getPilotUUID().ifPresent(uuid -> tag.putUUID("pilotUUID", uuid));
        tag.putInt("subsystems", getSubsystemState());

        Quaternionf rot = getShipRotation();
        tag.putFloat("rotX", rot.x); tag.putFloat("rotY", rot.y);
        tag.putFloat("rotZ", rot.z); tag.putFloat("rotW", rot.w);

        if (!blockSnapshot.isEmpty())
        {
            int n = blockSnapshot.size();
            int[] xs = new int[n], ys = new int[n], zs = new int[n], ss = new int[n];
            for (int i = 0; i < n; i++)
            {
                xs[i] = blockSnapshot.get(i).relX();
                ys[i] = blockSnapshot.get(i).relY();
                zs[i] = blockSnapshot.get(i).relZ();
                ss[i] = blockSnapshot.get(i).stateId();
            }
            tag.putIntArray("snapX", xs);
            tag.putIntArray("snapY", ys);
            tag.putIntArray("snapZ", zs);
            tag.putIntArray("snapStates", ss);
        }
    }

    @Override
    public void tick()
    {
        super.tick();
        //TODO: CALL ShipPhysics.integrate() HERE
    }

    // TODO: P4.8 FOR DAMAGE
    public void applyDamage(float rawDamage, int targetSubsystem)
    {
        if (!level().isClientSide())
        {
            float shield = getShieldStrength();
            if (shield > 0f)
            {
                float absorbed = Math.min(rawDamage * 0.85f, shield);
                rawDamage -= absorbed;
                setShieldStrength(shield - absorbed);
            }
            setHullIntegrity(getHullIntegrity() - rawDamage / getDefinition().map(ShipDefinition::maxHull).orElse(200f));

            if (isDestroyed()) Auralithpioneerinitiative.LOGGER.info("[Auralith] Ship {} destroyed.", getId());
        }
    }
}