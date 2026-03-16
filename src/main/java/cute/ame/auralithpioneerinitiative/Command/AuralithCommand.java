package cute.ame.auralithpioneerinitiative.Command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import cute.ame.auralithpioneerinitiative.Registrie.ModBlocks;
import cute.ame.auralithpioneerinitiative.Ship.Data.ShipDefinition;
import cute.ame.auralithpioneerinitiative.Ship.Data.ShipRegistry;
import cute.ame.auralithpioneerinitiative.Ship.Multiblock.ShipCoreBlock;
import cute.ame.auralithpioneerinitiative.Ship.Multiblock.ShipCoreBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class AuralithCommand
{
    private AuralithCommand() {}

    private static final SuggestionProvider<CommandSourceStack> SHIP_SUGGESTIONS = (ctx, builder) -> SharedSuggestionProvider.suggestResource(ShipRegistry.all().stream().map(ShipDefinition::id), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(
            Commands.literal("pioneer").requires(src -> src.hasPermission(2))
                .then(Commands.literal("list").executes(AuralithCommand::listShips))
                .then(Commands.literal("assemble").executes(AuralithCommand::assembleFirstShip).then(Commands.argument("ship_id", ResourceLocationArgument.id()).suggests(SHIP_SUGGESTIONS).executes(AuralithCommand::assembleShip)))
        );
    }

    private static int listShips(CommandContext<CommandSourceStack> ctx)
    {
        var src = ctx.getSource();
        var all = ShipRegistry.all();

        if (all.isEmpty())
        {
            src.sendFailure(Component.literal("[Auralith] No ship definitions loaded. Check data/*/ship_classes/*.json"));
            return 0;
        }

        src.sendSuccess(() -> Component.literal("[Auralith] Loaded ship definitions:"), false);
        for (ShipDefinition def : all)
        {
            int[] sz = def.computeSize();
            src.sendSuccess(() -> Component.literal("  • " + def.id() + " (" + def.displayName() + ")  "+ sz[0] + "x" + sz[1] + "x" + sz[2]), false);
        }
        return all.size();
    }

    private static int assembleShip(CommandContext<CommandSourceStack> ctx)
    {
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "ship_id");

        return ShipRegistry.get(id)
            .map(def -> doAssemble(ctx.getSource(), def))
            .orElseGet(() -> {
                ctx.getSource().sendFailure(Component.literal("[Auralith] Unknown ship definition: " + id + ", use /auralith list to see available definitions."));
                return 0;
            });
    }

    private static int assembleFirstShip(CommandContext<CommandSourceStack> ctx)
    {
        var all = ShipRegistry.all();
        if (all.isEmpty())
        {
            ctx.getSource().sendFailure(Component.literal("[Auralith] No ship definitions loaded."));
            return 0;
        }
        return doAssemble(ctx.getSource(), all.iterator().next());
    }

    private static int doAssemble(CommandSourceStack src, ShipDefinition def)
    {
        ServerPlayer player;
        try { player = src.getPlayerOrException(); }
        catch (Exception e)
        {
            src.sendFailure(Component.literal("Must be run by a player."));
            return 0;
        }

        ServerLevel level = src.getLevel();
        BlockPos pos = BlockPos.containing(player.getX(), player.getY(), player.getZ());

        if (!level.getBlockState(pos).isAir()) pos = pos.above();

        BlockState coreState = ModBlocks.SHIP_CORE.get().defaultBlockState().setValue(ShipCoreBlock.FACING, player.getDirection());
        level.setBlockAndUpdate(pos, coreState);

        if (level.getBlockEntity(pos) instanceof ShipCoreBlockEntity core)
        {
            core.forceAssemble(level, pos, player.getDirection(), def, player);
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            return 1;
        }

        src.sendFailure(Component.literal("[Auralith] Failed to place Ship Core block entity."));

        return 0;
    }
}