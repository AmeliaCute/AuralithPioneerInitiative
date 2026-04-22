package cute.ame.auralithpioneerinitiative.Ship.Entity;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Registrie.ModEntities;
import cute.ame.auralithpioneerinitiative.Ship.Data.ShipDefinition;
import cute.ame.auralithpioneerinitiative.Ship.Data.ShipRegistry;
import cute.ame.auralithpioneerinitiative.Ship.Network.ShipSnapshotPacket;
import cute.ame.auralithpioneerinitiative.Ship.Network.ShipTransformPacket;
import cute.ame.auralithpioneerinitiative.Ship.Physics.FlightInput;
import cute.ame.auralithpioneerinitiative.Ship.Physics.ShipPhysics;
import cute.ame.auralithpioneerinitiative.Ship.Physics.ShipTransformData;
import cute.ame.auralithpioneerinitiative.Ship.Physics.ShipTransformRegistry;
import cute.ame.auralithpioneerinitiative.Ship.Shipyard.ShipyardManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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

    private static final double SEAT_REACH      = 6.0;
    private static final double SEAT_HIT_RADIUS = 1.8;

    private static final EntityDataAccessor<Float>          DATA_HULL_INTEGRITY  = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float>          DATA_SHIELD_STRENGTH = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float>          DATA_FUEL_LEVEL      = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Long>           DATA_EU_STORED       = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<String>         DATA_SHIP_CLASS      = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Optional<UUID>> DATA_PILOT_UUID      = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer>        DATA_SUBSYSTEM_STATE = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Quaternionf>    DATA_ROTATION        = SynchedEntityData.defineId(ShipEntity.class, ModEntities.QUATERNIONF);

    private final ShipPhysics physics = new ShipPhysics();
    private FlightInput lastFlightInput = FlightInput.IDLE;
    private int flightInputAge = 0;

    private List<ShipSnapshotPacket.BlockEntry> blockSnapshot = new ArrayList<>();
    private ShipTransformData prevTransformThisTick = null;

    private static final int STATIC_BROADCAST_INTERVAL = 5;
    private int staticBroadcastTimer = 0;

    public ShipEntity(EntityType<?> type, Level level)
    {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    public boolean isPickable() { return true; }

    @Override
    public boolean canBeCollidedWith() { return true; }

    @Override
    public net.minecraft.world.entity.EntityDimensions getDimensions(net.minecraft.world.entity.Pose pose)
    {
        return getDefinition()
            .map(def ->
            {
                int[] sz = def.computeSize();
                float w  = Math.max(1f, Math.max(sz[0], sz[2]));
                float h  = Math.max(1f, sz[1]);
                return net.minecraft.world.entity.EntityDimensions.scalable(w, h);
            }).orElse(net.minecraft.world.entity.EntityDimensions.scalable(3f, 2f));
    }

    public AABB getLocalBounds()
    {
        return ShipPhysics.getLocalBounds(this);
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

        Quaternionf q = getShipRotation();
        float mnX=Float.MAX_VALUE,mnY=Float.MAX_VALUE,mnZ=Float.MAX_VALUE;
        float mxX=-Float.MAX_VALUE,mxY=-Float.MAX_VALUE,mxZ=-Float.MAX_VALUE;
        for (float[] c : corners)
        {
            Vector3f v = new Vector3f(c[0], c[1], c[2]);
            q.transform(v);
            if(v.x<mnX)mnX=v.x; if(v.x>mxX)mxX=v.x;
            if(v.y<mnY)mnY=v.y; if(v.y>mxY)mxY=v.y;
            if(v.z<mnZ)mnZ=v.z; if(v.z>mxZ)mxZ=v.z;
        }
        double ox=getX(),oy=getY(),oz=getZ();
        return new AABB(ox+mnX,oy+mnY,oz+mnZ,ox+mxX,oy+mxY,oz+mxZ);
    }

    private void refreshBB() { setBoundingBox(computeRotatedAABB()); }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps)
    {
        setPos(x, y, z); setRot(yRot, xRot); refreshBB();
    }

    @Override
    public void tick()
    {
        super.tick();
        refreshBB();
        if (!level().isClientSide()) serverTick();
    }

    private void serverTick()
    {
        boolean hasPilot = getPilotUUID().isPresent();

        if (hasPilot)
        {
            flightInputAge++;
            if (flightInputAge > 60) lastFlightInput = FlightInput.IDLE;
        }

        prevTransformThisTick = new ShipTransformData(
            getUUID(),
            position(),
            new Quaternionf(getShipRotation()),
            getLocalBounds()
        );

        if (hasPilot || physics.isMoving()) physics.integrate(this, hasPilot ? lastFlightInput : FlightInput.IDLE);

        updateTransformRegistry();

        carryInsidePlayers();

        if (hasPilot || physics.isMoving())
        {
            broadcastTransform();
            staticBroadcastTimer = 0;
        }
        else if (++staticBroadcastTimer >= STATIC_BROADCAST_INTERVAL)
        {
            broadcastTransform();
            staticBroadcastTimer = 0;
        }
    }

    private void carryInsidePlayers()
    {
        if (!(level() instanceof ServerLevel sl)) return;
        if (prevTransformThisTick == null) return;

        ShipTransformData before = prevTransformThisTick;
        ShipTransformData after  = ShipTransformRegistry.get(getUUID()).orElse(null);
        if (after == null) return;

        boolean posChanged = before.worldPos().distanceToSqr(after.worldPos()) > 1e-10;
        boolean rotChanged = !before.rotation().equals(after.rotation());
        if (!posChanged && !rotChanged) return;

        AABB localBounds = getLocalBounds();
        AABB worldSearchBounds = buildWorldAABB(before, localBounds).inflate(2.0);

        for (ServerPlayer player : sl.getPlayers(p -> !p.isPassenger()))
        {
            Vec3 playerPos = player.position();
            if (!worldSearchBounds.contains(playerPos)) continue;

            Vec3 localPos = before.toShip(playerPos);
            AABB localCheck = localBounds.inflate(1.0).expandTowards(0, 2.0, 0);
            if (!localCheck.contains(localPos)) continue;

            Vec3 targetWorld = after.toWorld(localPos);
            Vec3 delta = targetWorld.subtract(playerPos);
            if (delta.lengthSqr() < 1e-10) continue;

            player.setPos(targetWorld.x, targetWorld.y, targetWorld.z);
            player.hurtMarked = true;
        }
    }

    private static AABB buildWorldAABB(ShipTransformData transform, AABB local)
    {
        double[] xs = { local.minX, local.maxX };
        double[] ys = { local.minY, local.maxY };
        double[] zs = { local.minZ, local.maxZ };

        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;

        org.joml.Vector3f v = new org.joml.Vector3f();
        for (double lx : xs) for (double ly : ys) for (double lz : zs)
        {
            v.set((float) lx, (float) ly, (float) lz);
            transform.rotation().transform(v);
            double wx = transform.worldPos().x + v.x;
            double wy = transform.worldPos().y + v.y;
            double wz = transform.worldPos().z + v.z;
            if (wx < minX) minX = wx; if (wx > maxX) maxX = wx;
            if (wy < minY) minY = wy; if (wy > maxY) maxY = wy;
            if (wz < minZ) minZ = wz; if (wz > maxZ) maxZ = wz;
        }
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private void updateTransformRegistry()
    {
        ShipTransformRegistry.update(getUUID(), new ShipTransformData(
            getUUID(), position(), new Quaternionf(getShipRotation()), getLocalBounds()
        ));
    }

    private void broadcastTransform()
    {
        AABB   local = getLocalBounds();
        Vec3   vel   = physics.getLinearVelocity();
        Quaternionf rot = getShipRotation();

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(this, new ShipTransformPacket(
            getUUID(),
            getX(), getY(), getZ(),
            rot.x, rot.y, rot.z, rot.w,
            (float)vel.x, (float)vel.y, (float)vel.z,
            (float)local.minX, (float)local.minY, (float)local.minZ,
            (float)local.maxX, (float)local.maxY, (float)local.maxZ
        ));
    }

    @Override
    public void remove(RemovalReason reason)
    {
        super.remove(reason);

        if (!level().isClientSide())
        {
            ShipTransformRegistry.remove(getUUID());

            boolean shouldCleanupShipyard = reason == RemovalReason.KILLED || reason == RemovalReason.DISCARDED;

            if (shouldCleanupShipyard && level() instanceof ServerLevel sl)
            {
                try
                {
                    ShipyardManager.removeFromShipyard(sl.getServer(), getUUID(), blockSnapshot);
                }
                catch (Exception e)
                {
                    Auralithpioneerinitiative.LOGGER.warn("[Auralith] Nettoyage shipyard échoué pour {}: {}", getUUID(), e.getMessage());
                }
            }
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand)
    {
        if (level().isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;

        if (getPilotUUID().isPresent())
        {
            sp.sendSystemMessage(Component.literal("[Auralith] Ce vaisseau a déjà un pilote."));
            return InteractionResult.FAIL;
        }

        Optional<ShipDefinition> defOpt = getDefinition();
        if (defOpt.isPresent())
        {
            Vec3 seatWorld = getSeatWorldPos(defOpt.get());
            Vec3 eye = player.getEyePosition();
            Vec3 lookEnd = eye.add(player.getLookAngle().scale(SEAT_REACH));
            double dist = distRayToPoint(eye, lookEnd, seatWorld);
            if (dist > SEAT_HIT_RADIUS && player.distanceTo(this) > SEAT_REACH)
            {
                sp.sendSystemMessage(Component.literal("[Auralith] Visez le siège du cockpit pour embarquer."));
                return InteractionResult.PASS;
            }
        }

        getDefinition().ifPresent(physics::loadFromDefinition);
        setPilotUUID(sp.getUUID());
        sp.startRiding(this, true);

        String shipName = getDefinition().map(ShipDefinition::displayName).orElse("Vaisseau inconnu");
        sp.sendSystemMessage(Component.literal("[Auralith] Pilotage de " + shipName + " - WASD/Espace/LCtrl : poussée | ↑↓←→ Q/E : rotation | LShift : boost | R : quitter"));
        Auralithpioneerinitiative.LOGGER.info("[Auralith] {} a embarqué sur {} ({})", sp.getScoreboardName(), getUUID(), shipName);

        return InteractionResult.CONSUME;
    }

    public Vec3 getSeatWorldPos(ShipDefinition def)
    {
        Vec3 local = def.findCockpitOffset().toVec3();
        Vector3f v = new Vector3f((float)local.x, (float)local.y + 0.5f, (float)local.z);
        getShipRotation().transform(v);
        return new Vec3(getX() + v.x, getY() + v.y, getZ() + v.z);
    }

    private static double distRayToPoint(Vec3 a, Vec3 b, Vec3 p)
    {
        Vec3 ab = b.subtract(a);
        double t = Math.max(0.0, Math.min(1.0, p.subtract(a).dot(ab) / Math.max(ab.dot(ab), 1e-9)));
        return p.distanceTo(a.add(ab.scale(t)));
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction moveFunction)
    {
        if (!hasPassenger(passenger)) return;
        Vec3 offset = getDefinition().map(def -> def.findCockpitOffset().toVec3()).orElse(new Vec3(0.0, 1.0, 0.0));
        Vector3f rotated = new Vector3f((float)offset.x, (float)offset.y, (float)offset.z);
        getShipRotation().transform(rotated);
        moveFunction.accept(passenger, getX() + rotated.x, getY() + rotated.y, getZ() + rotated.z);
    }

    @Override
    public void removePassenger(Entity passenger)
    {
        super.removePassenger(passenger);
        if (passenger instanceof Player p && p.getUUID().equals(getPilotUUID().orElse(null)))
        {
            setPilotUUID(null);
            lastFlightInput = FlightInput.IDLE;
            Auralithpioneerinitiative.LOGGER.debug("[Auralith] {} a quitté le vaisseau {}", p.getScoreboardName(), getUUID());
        }
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

    public float getHullIntegrity() { return entityData.get(DATA_HULL_INTEGRITY); }
    public float getShieldStrength() { return entityData.get(DATA_SHIELD_STRENGTH); }
    public float getFuelLevel() { return entityData.get(DATA_FUEL_LEVEL); }
    public long getEuStored() { return entityData.get(DATA_EU_STORED); }
    public String getShipClassId() { return entityData.get(DATA_SHIP_CLASS); }
    public Optional<UUID> getPilotUUID() { return entityData.get(DATA_PILOT_UUID); }
    public int getSubsystemState() { return entityData.get(DATA_SUBSYSTEM_STATE); }
    public Quaternionf getShipRotation() { return entityData.get(DATA_ROTATION); }

    public void setHullIntegrity(float v) { entityData.set(DATA_HULL_INTEGRITY,  clamp01(v)); }
    public void setShieldStrength(float v) { entityData.set(DATA_SHIELD_STRENGTH, clamp01(v)); }
    public void setFuelLevel(float v) { entityData.set(DATA_FUEL_LEVEL, clamp01(v)); }
    public void setEuStored(long v) { entityData.set(DATA_EU_STORED, Math.max(0, v)); }
    public void setShipClassId(ResourceLocation id){ entityData.set(DATA_SHIP_CLASS, id.toString()); }
    public void setPilotUUID(UUID uuid) { entityData.set(DATA_PILOT_UUID, Optional.ofNullable(uuid)); }
    public void setSubsystemState(int mask) { entityData.set(DATA_SUBSYSTEM_STATE, mask); }

    public void setShipRotation(Quaternionf q)
    {
        entityData.set(DATA_ROTATION, new Quaternionf(q).normalize());
        if (!level().isClientSide()) refreshBB();
    }

    public void setLastFlightInput(FlightInput input) { this.lastFlightInput = input; }
    public void resetFlightInputAge() { this.flightInputAge  = 0; }
    public ShipPhysics getPhysics() { return physics; }

    private static float clamp01(float v) { return Math.max(0f, Math.min(1f, v)); }

    public Optional<ShipDefinition> getDefinition() { return ShipRegistry.get(ResourceLocation.parse(getShipClassId())); }

    public boolean isSubsystemOnline(int flag) { return (getSubsystemState() & flag) != 0; }
    public boolean hasPilot() { return getPilotUUID().isPresent(); }
    public boolean isDestroyed() { return getHullIntegrity() <= 0f; }

    public void setBlockSnapshot(List<ShipSnapshotPacket.BlockEntry> snapshot)
    {
        this.blockSnapshot = new ArrayList<>(snapshot);
        refreshBB();
    }

    public List<ShipSnapshotPacket.BlockEntry> getBlockSnapshot() { return blockSnapshot; }

    @Override
    public void startSeenByPlayer(ServerPlayer connection)
    {
        super.startSeenByPlayer(connection);
        if (!blockSnapshot.isEmpty()) PacketDistributor.sendToPlayer(connection, new ShipSnapshotPacket(this.getUUID(), blockSnapshot));
        broadcastTransform();
    }

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
            setHullIntegrity(getHullIntegrity()
                - rawDamage / getDefinition().map(ShipDefinition::maxHull).orElse(200f));

            if (isDestroyed())
            {
                Auralithpioneerinitiative.LOGGER.info("[Auralith] Vaisseau {} détruit.", getId());
                kill();
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag)
    {
        setHullIntegrity(tag.getFloat("hull"));
        setShieldStrength(tag.getFloat("shield"));
        setFuelLevel(tag.getFloat("fuel"));
        setEuStored(tag.getLong("eu"));
        if (tag.contains("shipClass"))  entityData.set(DATA_SHIP_CLASS, tag.getString("shipClass"));
        if (tag.hasUUID("pilotUUID"))   setPilotUUID(tag.getUUID("pilotUUID"));
        setSubsystemState(tag.getInt("subsystems"));
        if (tag.contains("rotX")) setShipRotation(new Quaternionf(tag.getFloat("rotX"), tag.getFloat("rotY"), tag.getFloat("rotZ"), tag.getFloat("rotW")));
        physics.load(tag);

        if (tag.contains("snapX"))
        {
            int[] xs = tag.getIntArray("snapX"), ys = tag.getIntArray("snapY"),
                  zs = tag.getIntArray("snapZ"), ss = tag.getIntArray("snapStates");

            blockSnapshot = new ArrayList<>(xs.length);
            for (int i = 0; i < xs.length; i++)
                blockSnapshot.add(new ShipSnapshotPacket.BlockEntry(xs[i], ys[i], zs[i], ss[i]));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag)
    {
        tag.putFloat("hull", getHullIntegrity());
        tag.putFloat("shield", getShieldStrength());
        tag.putFloat("fuel", getFuelLevel());
        tag.putLong ("eu", getEuStored());
        tag.putString("shipClass", getShipClassId());
        getPilotUUID().ifPresent(uuid -> tag.putUUID("pilotUUID", uuid));
        tag.putInt("subsystems", getSubsystemState());

        Quaternionf rot = getShipRotation();
        tag.putFloat("rotX", rot.x); tag.putFloat("rotY", rot.y);
        tag.putFloat("rotZ", rot.z); tag.putFloat("rotW", rot.w);
        physics.save(tag);

        if (!blockSnapshot.isEmpty())
        {
            int n = blockSnapshot.size();
            int[] xs = new int[n], ys = new int[n], zs = new int[n], ss = new int[n];
            for (int i = 0; i < n; i++)
            {
                xs[i] = blockSnapshot.get(i).relX(); ys[i] = blockSnapshot.get(i).relY();
                zs[i] = blockSnapshot.get(i).relZ(); ss[i] = blockSnapshot.get(i).stateId();
            }
            tag.putIntArray("snapX", xs); tag.putIntArray("snapY", ys);
            tag.putIntArray("snapZ", zs); tag.putIntArray("snapStates", ss);
        }
    }
}