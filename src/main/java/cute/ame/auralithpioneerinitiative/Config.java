package cute.ame.auralithpioneerinitiative;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static ModConfigSpec.IntValue TRANSITION_COOLDOWN;

    static final ModConfigSpec SPEC;

    static
    {
        BUILDER.push("Server");

        TRANSITION_COOLDOWN = BUILDER.defineInRange("TransitionCooldown", 500, 500, 1000);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
