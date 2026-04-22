package cute.ame.auralithpioneerinitiative.vehicle.Client;

import cute.ame.auralithpioneerinitiative.vehicle.Baking.BakedVehicleMesh;
import cute.ame.auralithpioneerinitiative.vehicle.Baking.VehicleMeshBaker;
import cute.ame.auralithpioneerinitiative.vehicle.Baking.VehicleSnapshot;
import cute.ame.auralithpioneerinitiative.vehicle.Entity.AbstractVehicleEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT)
public final class VehicleClientCache
{
  private VehicleClientCache() {}

  private static final ConcurrentHashMap<UUID, WeakReference<AbstractVehicleEntity>> ENTITY_MAP= new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<UUID, BakedVehicleMesh> MESH_MAP = new ConcurrentHashMap<>();
  private static final ConcurrentLinkedQueue<Runnable> UPLOAD_QUEUE = new ConcurrentLinkedQueue<>();
  private static final int MAX_UPLOADS_PER_FRAME = 2;

  public static void registerEntity(AbstractVehicleEntity entity)
  {
    ENTITY_MAP.putIfAbsent(entity.getUUID(), new WeakReference<>(entity));
  }

  public static void unregisterEntity(UUID uuid) {
    ENTITY_MAP.remove(uuid);
  }

  public static @Nullable AbstractVehicleEntity getEntity(UUID uuid)
  {
    WeakReference<AbstractVehicleEntity> ref = ENTITY_MAP.get(uuid);
    if (ref == null) return null;

    AbstractVehicleEntity entity = ref.get();
    if (entity == null) ENTITY_MAP.remove(uuid);

    return entity;
  }

  public static @Nullable BakedVehicleMesh getMesh(UUID uuid) {
    return MESH_MAP.get(uuid);
  }

  public static void onSnapshotReceived(UUID uuid, VehicleSnapshot snapshot)
  {
    BakedVehicleMesh old = MESH_MAP.remove(uuid);
    if (old != null) UPLOAD_QUEUE.offer(old::close);
    VehicleMeshBaker.bakeAsync(uuid, snapshot);
  }

  public static void storeMesh(UUID uuid, BakedVehicleMesh mesh)
  {
    BakedVehicleMesh old = MESH_MAP.put(uuid, mesh);
    if (old != null) old.close();
  }

  public static void submitUpload(UUID uuid, Runnable uploadTask) {
    UPLOAD_QUEUE.offer(uploadTask);
  }

  public static void evict(UUID uuid)
  {
    ENTITY_MAP.remove(uuid);
    BakedVehicleMesh mesh = MESH_MAP.remove(uuid);
    if (mesh != null) UPLOAD_QUEUE.offer(mesh::close);
  }

  public static void evictAll()
  {
    List.copyOf(MESH_MAP.keySet()).forEach(VehicleClientCache::evict);
    ENTITY_MAP.clear();
  }

  public static void onRenderLevelStage(RenderLevelStageEvent event)
  {
    if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;
    drainUploadQueue();
  }

  private static void drainUploadQueue()
  {
    int drained = 0;
    Runnable task;
    while (drained < MAX_UPLOADS_PER_FRAME && (task = UPLOAD_QUEUE.poll()) != null)
    {
      task.run();
      drained++;
    }
  }
}