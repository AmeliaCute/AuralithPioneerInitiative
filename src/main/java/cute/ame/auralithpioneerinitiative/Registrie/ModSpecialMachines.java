package cute.ame.auralithpioneerinitiative.Registrie;

import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.init.MachineRegistrationHelper;
import aztech.modern_industrialization.machines.models.MachineCasings;
import cute.ame.auralithpioneerinitiative.Machine.O2Filler.O2FillerBlockEntity;

public class ModSpecialMachines
{

  public static void init()
  {
    MachineRegistrationHelper.registerMachine(
        "O2 Filler",
        "o2_filler",
        O2FillerBlockEntity::new,
        MachineBlockEntity::registerFluidApi,
        MachineBlockEntity::registerItemApi
    );

    MachineRegistrationHelper.addMachineModel(
        "o2_filler",
        "o2_filler",
        MachineCasings.CLEAN_STAINLESS_STEEL,
        true,
        false,
        false
    );
  }
}