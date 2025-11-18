package cute.ame.auralithpioneerinitiative.Plant.Thermal;

import cute.ame.auralithpioneerinitiative.Registries.BlockRegistry;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.KelpPlantBlock;

public class ThermalKelpPlant extends KelpPlantBlock
{
    public ThermalKelpPlant(Properties properties) {
        super(properties);
    }

    @Override
    protected GrowingPlantHeadBlock getHeadBlock() {
        return (GrowingPlantHeadBlock) BlockRegistry.THERMAL_KELP.get();
    }
}
