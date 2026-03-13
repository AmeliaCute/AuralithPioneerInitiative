package cute.ame.auralithpioneerinitiative.Ship.Data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

public record BlockOffset(double x, double y, double z)
{
    public static final BlockOffset ZERO = new BlockOffset(0, 0, 0);

    public static final Codec<BlockOffset> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.DOUBLE.optionalFieldOf("x", 0.0).forGetter(BlockOffset::x),
            Codec.DOUBLE.optionalFieldOf("y", 0.0).forGetter(BlockOffset::y),
            Codec.DOUBLE.optionalFieldOf("z", 0.0).forGetter(BlockOffset::z)
        ).apply(instance, BlockOffset::new)
    );

    public Vec3 toVec3() { return new Vec3(x, y, z); }
}
