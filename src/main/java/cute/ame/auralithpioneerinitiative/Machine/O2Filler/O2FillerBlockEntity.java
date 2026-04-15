package cute.ame.auralithpioneerinitiative.Machine.O2Filler;

import aztech.modern_industrialization.inventory.*;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.*;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.guicomponents.AutoExtract;
import aztech.modern_industrialization.machines.guicomponents.ProgressBar;
import aztech.modern_industrialization.machines.guicomponents.SlotPanel;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.Transaction;
import aztech.modern_industrialization.util.Tickable;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Module.O2TankModule;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

public class O2FillerBlockEntity extends MachineBlockEntity implements Tickable
{
  public static final int MAX_PER_TICK = 20_000;
  public static final long FLUID_CAPACITY_MB = 64_000;

  private final IsActiveComponent isActive;
  private final MachineInventoryComponent inventory;
  private final RedstoneControlComponent redstoneControl;

  private int progressTick = 0;
  private static final int FILL_TICKS = 10;

  public O2FillerBlockEntity(BEP bep)
  {
    super(bep, new MachineGuiParameters.Builder("o2_filler", true).backgroundHeight(178).build(), new OrientationComponent.Params(true, true, false));

    this.isActive = new IsActiveComponent();
    this.redstoneControl = new RedstoneControlComponent();
    List<ConfigurableItemStack> itemInputs = Collections.singletonList(ConfigurableItemStack.standardInputSlot());
    List<ConfigurableItemStack> itemOutputs = Collections.singletonList(ConfigurableItemStack.standardOutputSlot());
    List<ConfigurableFluidStack> fluidInputs = Collections.singletonList(ConfigurableFluidStack.standardInputSlot(FLUID_CAPACITY_MB));

    SlotPositions itemPos = new SlotPositions.Builder().addSlot(56, 36).addSlot(102, 36).build();
    SlotPositions fluidPos = new SlotPositions.Builder().addSlot(10, 36).build();

    this.inventory = new MachineInventoryComponent(itemInputs, itemOutputs, fluidInputs, Collections.emptyList(), itemPos, fluidPos);
    registerComponents(isActive, inventory, redstoneControl);

    registerGuiComponent(new ProgressBar(new ProgressBar.Params(77, 33, "wiremill"), () -> (float) progressTick / FILL_TICKS));
    registerGuiComponent(new AutoExtract(orientation, false));
    registerGuiComponent(new SlotPanel(this).withRedstoneControl(redstoneControl));
  }

  private boolean fillStep(boolean simulate)
  {
    ConfigurableItemStack inputSlot = inventory.getItemInputs().get(0);
    ItemStack tankStack = inputSlot.toStack();

    if (tankStack.isEmpty() || !(tankStack.getItem() instanceof O2TankModule)) return false;

    int stored = O2TankModule.getStored(tankStack);
    int capacity = O2TankModule.getCapacity(tankStack);
    int space = capacity - stored;
    if (space <= 0) return false;

    ConfigurableFluidStack fluidSlot = inventory.getFluidInputs().get(0);
    long availableDrops = fluidSlot.getAmount();
    if (availableDrops <= 0) return false;

    int toTransferMb = (int) Math.min(Math.min(space, MAX_PER_TICK), availableDrops);
    if (toTransferMb <= 0) return false;

    if (!simulate)
    {
      try (Transaction tx = Transaction.openRoot())
      {
        long dropsToRemove = toTransferMb;
        fluidSlot.setAmount(availableDrops - dropsToRemove);
        O2TankModule.refill(tankStack, toTransferMb);
        inputSlot.setKey(ItemVariant.of(tankStack));
        inputSlot.setAmount(tankStack.getCount());

        tx.commit();
      }
    }
    return true;
  }

  private void pushToOutput()
  {
    ConfigurableItemStack inputSlot = inventory.getItemInputs().get(0);
    ConfigurableItemStack outputSlot = inventory.getItemOutputs().get(0);

    if (!inputSlot.getResource().isBlank() && outputSlot.getResource().isBlank())
    {

      outputSlot.setKey(inputSlot.getResource());
      outputSlot.setAmount(inputSlot.getAmount());

      inputSlot.setKey(ItemVariant.blank());
      inputSlot.setAmount(0);
    }
  }

  @Override
  public void tick()
  {
    if (level.isClientSide) return;

    if (!redstoneControl.doAllowNormalOperation(this))
    {
      isActive.updateActive(false, this);
      progressTick = 0;
    }
    else if (fillStep(true))
    {
      progressTick++;
      isActive.updateActive(true, this);

      if (progressTick >= FILL_TICKS)
      {
        fillStep(false);
        progressTick = 0;

        ItemStack tankCheck = inventory.getItemInputs().get(0).toStack();
        if (!tankCheck.isEmpty() && O2TankModule.getStored(tankCheck) >= O2TankModule.getCapacity(tankCheck)) pushToOutput();
      }
    }
    else
    {
      isActive.updateActive(false, this);
      progressTick = 0;
      ItemStack tankIn = inventory.getItemInputs().get(0).toStack();
      if (!tankIn.isEmpty() && O2TankModule.getStored(tankIn) >= O2TankModule.getCapacity(tankIn)) pushToOutput();
    }

    if (orientation.extractItems) inventory.inventory.autoExtractItems(level, worldPosition, orientation.outputDirection);
    setChanged();
  }

  @Override
  public MIInventory getInventory()
  {
    return inventory.inventory;
  }

  @Override
  public MachineModelClientData getMachineModelData()
  {
    MachineModelClientData data = new MachineModelClientData();
    data.isActive = isActive.isActive;
    orientation.writeModelData(data);
    return data;
  }
}