package cute.ame.auralithpioneerinitiative.Ship.Renderer;

import cute.ame.auralithpioneerinitiative.Ship.Network.ShipSnapshotPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@OnlyIn(Dist.CLIENT)
public final class ShipClientCache
{
    private ShipClientCache() {}

    public enum MeshStatus { BAKING, READY }
    private static final Map<UUID, BakedShipMesh> MESH_MAP = new ConcurrentHashMap<>();
    private static final Map<UUID, MeshStatus>    STATUS   = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<Runnable> UPLOAD_QUEUE = new ConcurrentLinkedQueue<>();

    private static final int MAX_UPLOADS_PER_FRAME = 2;
    public static void onSnapshotReceived(UUID uuid, List<ShipSnapshotPacket.BlockEntry> blocks)
    {
        BakedShipMesh old = MESH_MAP.remove(uuid);
        if (old != null) old.close();

        STATUS.put(uuid, MeshStatus.BAKING);
        ShipMeshBaker.bakeAsync(uuid, blocks);
    }

    public static void submitUpload(UUID uuid, Runnable uploadTask)
    {
        UPLOAD_QUEUE.offer(uploadTask);
    }

    public static void storeMesh(UUID uuid, BakedShipMesh mesh)
    {
        MESH_MAP.put(uuid, mesh);
        STATUS.put(uuid, MeshStatus.READY);
    }

    public static @Nullable BakedShipMesh getMesh(UUID uuid)
    {
        return MESH_MAP.get(uuid);
    }
    public static MeshStatus getStatus(UUID uuid)
    {
        return STATUS.getOrDefault(uuid, MeshStatus.BAKING);
    }

    public static void drainUploadQueue()
    {
        int drained = 0;
        Runnable task;
        while (drained < MAX_UPLOADS_PER_FRAME && (task = UPLOAD_QUEUE.poll()) != null)
        {
            task.run();
            drained++;
        }
    }

    public static void evict(UUID uuid)
    {
        STATUS.remove(uuid);
        BakedShipMesh mesh = MESH_MAP.remove(uuid);
        if (mesh != null)
        {
            UPLOAD_QUEUE.offer(mesh::close);
        }
    }

    public static void evictAll()
    {
        for (UUID uuid : MESH_MAP.keySet()) evict(uuid);
        STATUS.clear();
    }
}