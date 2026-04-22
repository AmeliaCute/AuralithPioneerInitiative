package cute.ame.auralithpioneerinitiative.Ship.Multiblock;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.Registrie.ModBlockEntities;
import cute.ame.auralithpioneerinitiative.Ship.Data.ShipDefinition;
import cute.ame.auralithpioneerinitiative.Ship.Data.ShipRegistry;
import cute.ame.auralithpioneerinitiative.Registrie.ModEntities;
import cute.ame.auralithpioneerinitiative.Ship.Entity.ShipEntity;
import cute.ame.auralithpioneerinitiative.Ship.Network.ShipSnapshotPacket;
import cute.ame.auralithpioneerinitiative.Ship.Shipyard.ShipyardManager;
import gg.amecute.auralithutilities.Multiblock.Data.BlockDefinition;
import gg.amecute.auralithutilities.Multiblock.Data.MultiblockStructure;
import gg.amecute.auralithutilities.Multiblock.Data.MultiblockValidator;
import gg.amecute.auralithutilities.Multiblock.Data.Vec3i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ShipCoreBlockEntity extends BlockEntity
{
    private boolean assembled = false;
    private @Nullable ResourceLocation shipDefinitionId = null;
    private @Nullable UUID shipEntityUUID = null;
    private boolean snapshotFromWorld = false;

    public ShipCoreBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.SHIP_CORE.get(), pos, state);
    }

    public void tryAssemble(Level level, BlockPos pos, Direction facing, Player player)
    {
        if (!(level instanceof ServerLevel serverLevel)) return;

        for (ShipDefinition def : ShipRegistry.all())
        {
            MultiblockStructure structure = buildStructure(def);
            MultiblockValidator.ValidationResult result = MultiblockValidator.validate(serverLevel, pos, structure, facing);

            if (result.valid())
            {
                List<ShipSnapshotPacket.BlockEntry> snapshot = captureSnapshot(serverLevel, pos, def);
                onAssemblySuccess(serverLevel, pos, def, snapshot, true, player);
                return;
            }
        }
        player.sendSystemMessage(Component.literal("[Auralith] No valid ship structure detected. Check the multiblock layout."));
    }

    public void forceAssemble(ServerLevel level, BlockPos pos, Direction facing, ShipDefinition def, @Nullable Player player)
    {
        List<ShipSnapshotPacket.BlockEntry> snapshot = buildSnapshotFromDefinition(def);
        onAssemblySuccess(level, pos, def, snapshot, false, player);
    }

    private void onAssemblySuccess(ServerLevel level, BlockPos pos, ShipDefinition def, List<ShipSnapshotPacket.BlockEntry> snapshot, boolean fromWorld, @Nullable Player player)
    {
        ShipEntity ship = ModEntities.SHIP.get().create(level);
        if (ship == null)
        {
            Auralithpioneerinitiative.LOGGER.error("[Auralith] Failed to create ShipEntity.");
            return;
        }

        ship.setShipClassId(def.id());
        ship.setHullIntegrity(1.0f);
        ship.setShieldStrength(1.0f);
        ship.setFuelLevel(1.0f);
        ship.setEuStored(def.maxEu());
        ship.setBlockSnapshot(snapshot);
        ship.setShipRotation(def.computeInitialRotation());
        ship.moveTo(pos.getX(), pos.getY(), pos.getZ());
        level.addFreshEntity(ship);

        assembled = true;
        shipDefinitionId = def.id();
        shipEntityUUID = ship.getUUID();
        snapshotFromWorld = fromWorld;
        setChanged();

        try
        {
            ShipyardManager.writeSnapshotToShipyard(level.getServer(), ship.getUUID(), snapshot);
        }
        catch (Exception e)
        {
            Auralithpioneerinitiative.LOGGER.error("[Auralith] Erreur écriture shipyard: {}", e.getMessage());
        }

        if (!snapshot.isEmpty())
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(ship, new ShipSnapshotPacket(ship.getUUID(), snapshot));

        String msg = "[Auralith] Ship assembled: " + def.displayName() + " (" + snapshot.size() + " blocks, entity " + ship.getUUID() + ")";
        if (player != null) player.sendSystemMessage(Component.literal(msg));
        Auralithpioneerinitiative.LOGGER.info("[Auralith] {} assembled '{}' at {} - {} blocks - entity {}", player != null ? player.getScoreboardName() : "<command>", def.id(), pos, snapshot.size(), ship.getUUID());
    }

    public void markDisassembled()
    {
        assembled = false;
        shipEntityUUID = null;
        setChanged();
        Auralithpioneerinitiative.LOGGER.debug("[Auralith] ShipCoreBlockEntity marqué désassemblé à {}", getBlockPos());
    }

    static List<ShipSnapshotPacket.BlockEntry> captureSnapshot(ServerLevel level, BlockPos corePos, ShipDefinition def)
    {
        int[] size = def.computeSize();
        int halfW = size[0] / 2 + 2;
        int height = size[1] + 2;
        int depth = size[2] + 2;

        List<ShipSnapshotPacket.BlockEntry> result = new ArrayList<>();
        for (int dy = -1; dy <= height; dy++)
            for (int dz = -depth; dz <= depth; dz++)
                for (int dx = -halfW; dx <= halfW; dx++)
                {
                    BlockPos worldPos = corePos.offset(dx, dy, dz);
                    BlockState state  = level.getBlockState(worldPos);
                    if (state.isAir()) continue;
                    result.add(new ShipSnapshotPacket.BlockEntry(dx, dy, dz, Block.getId(state)));
                }
        return result;
    }

    static List<ShipSnapshotPacket.BlockEntry> buildSnapshotFromDefinition(ShipDefinition def)
    {
        int ctrlX = 0, ctrlY = 0, ctrlZ = 0;
        outer:
        for (int ly = 0; ly < def.layers().size(); ly++)
        {
            List<String> rows = def.layers().get(ly);
            for (int lz = 0; lz < rows.size(); lz++)
            {
                String row = rows.get(lz);
                for (int lx = 0; lx < row.length(); lx++)
                {
                    BlockDefinition bd = def.palette().get(row.charAt(lx));
                    if (bd != null && bd.isController()) { ctrlX = lx; ctrlY = ly; ctrlZ = lz; break outer; }
                }
            }
        }

        List<ShipSnapshotPacket.BlockEntry> result = new ArrayList<>();
        for (int ly = 0; ly < def.layers().size(); ly++)
        {
            List<String> rows = def.layers().get(ly);
            for (int lz = 0; lz < rows.size(); lz++)
            {
                String row = rows.get(lz);
                for (int lx = 0; lx < row.length(); lx++)
                {
                    char c = row.charAt(lx);
                    if (c == ' ') continue;
                    BlockDefinition bd = def.palette().get(c);
                    if (bd == null) continue;
                    var blockOpt = BuiltInRegistries.BLOCK.getOptional(bd.blockId());
                    if (blockOpt.isEmpty()) continue;
                    result.add(new ShipSnapshotPacket.BlockEntry(lx - ctrlX, ly - ctrlY, lz - ctrlZ, Block.getId(blockOpt.get().defaultBlockState())));
                }
            }
        }
        return result;
    }

    private static MultiblockStructure buildStructure(ShipDefinition def)
    {
        int[] size = def.computeSize();
        gg.amecute.auralithutilities.Multiblock.Data.RecipeConfig dummyRecipe = new gg.amecute.auralithutilities.Multiblock.Data.RecipeConfig(ResourceLocation.fromNamespaceAndPath("auralithpioneerinitiative", "ship_dummy"), 0L, 0L, 0);
        return new MultiblockStructure(
            def.id(), def.displayName(),
            "auralithpioneerinitiative:ship_hull",
            gg.amecute.auralithutilities.Multiblock.Data.MultiblockType.MACHINE,
            new Vec3i(size[0], size[1], size[2]),
            def.palette(), def.layers(),
            Optional.empty(), Optional.empty(),
            dummyRecipe
        );
    }

    public boolean isAssembled() { return assembled; }
    public @Nullable ResourceLocation getShipDefinitionId() { return shipDefinitionId; }
    public @Nullable UUID getShipEntityUUID() { return shipEntityUUID; }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        tag.putBoolean("assembled", assembled);
        tag.putBoolean("snapshotFromWorld", snapshotFromWorld);
        if (shipDefinitionId != null) tag.putString("shipDef", shipDefinitionId.toString());
        if (shipEntityUUID != null) tag.putUUID("shipEntity", shipEntityUUID);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        assembled = tag.getBoolean("assembled");
        snapshotFromWorld = tag.getBoolean("snapshotFromWorld");
        if (tag.contains("shipDef")) shipDefinitionId = ResourceLocation.parse(tag.getString("shipDef"));
        if (tag.hasUUID("shipEntity")) shipEntityUUID = tag.getUUID("shipEntity");
    }
}