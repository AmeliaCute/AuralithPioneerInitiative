package cute.ame.auralithpioneerinitiative.Ship.Multiblock;

import cute.ame.auralithpioneerinitiative.Registrie.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class HoloPanelBlockEntity extends BlockEntity
{
  private ResourceLocation panelTypeId = null;
  private float offsetX = 0, offsetY = 0.5f, offsetZ = 0;
  private String facing = "NORTH";
  private float worldWidth  = 1.5f;
  private float worldHeight = 1.0f;
  private boolean interactable = true;

  public HoloPanelBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.HOLOPANEL.get(), pos, state); }

  public @Nullable ResourceLocation getPanelTypeId() { return panelTypeId; }
  public void setPanelTypeId(ResourceLocation id) { panelTypeId = id; setChanged(); }

  public float getOffsetX() { return offsetX; }
  public float getOffsetY() { return offsetY; }
  public float getOffsetZ() { return offsetZ; }
  public void setOffset(float x, float y, float z) { offsetX = x; offsetY = y; offsetZ = z; setChanged(); }

  public String getFacing() { return facing; }
  public void setFacing(String f) { facing = f; setChanged(); }

  public float getWorldWidth() { return worldWidth; }
  public float getWorldHeight() { return worldHeight; }
  public void setWorldSize(float w, float h) { worldWidth = w; worldHeight = h; setChanged(); }

  public boolean isInteractable() { return interactable; }
  public void setInteractable(boolean v) { interactable = v; setChanged(); }

  @Override
  public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

  @Override
  public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
  {
    super.saveAdditional(tag, registries);
    if (panelTypeId != null) tag.putString("panelType", panelTypeId.toString());

    tag.putFloat("ox", offsetX); tag.putFloat("oy", offsetY); tag.putFloat("oz", offsetZ);
    tag.putString("facing", facing);
    tag.putFloat("worldWidth", worldWidth); tag.putFloat("worldHeight", worldHeight);
    tag.putBoolean("interactable", interactable);
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
  {
    super.loadAdditional(tag, registries);
    if (tag.contains("panelType")) panelTypeId = ResourceLocation.parse(tag.getString("panelType"));

    offsetX = tag.getFloat("ox"); offsetY = tag.getFloat("oy"); offsetZ = tag.getFloat("oz");
    facing = tag.getString("facing");
    worldWidth  = tag.contains("worldWidth")  ? tag.getFloat("worldWidth")  : 1.5f;
    worldHeight = tag.contains("worldHeight") ? tag.getFloat("worldHeight") : 1.0f;
    interactable = !tag.contains("interactable") || tag.getBoolean("interactable");
  }
}