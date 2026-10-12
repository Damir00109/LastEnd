package com.damir00109.laststand.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.damir00109.laststand.downed.DownedManager;
import com.damir00109.laststand.player.PlayerDownedData;
import com.damir00109.laststand.util.DeathTimerCalculator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class LastStandCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("laststand")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("status")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(LastStandCommand::status)))
                .then(Commands.literal("down")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(LastStandCommand::down)))
                .then(Commands.literal("reset")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(LastStandCommand::reset))));
    }

    private static int status(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        PlayerDownedData data = DownedManager.getData(player);
        long now = System.currentTimeMillis();
        long elapsed = data.getDeathWindowStart() == 0L ? 0L : now - data.getDeathWindowStart();
        long windowRemaining = Math.max(0L, DeathTimerCalculator.TWENTY_FOUR_HOURS_MS - elapsed);

        Component message = Component.literal(String.format(
                "[Last Stand] %s | State: %s | Deaths: %d | Remaining: %ds | Window: %dms",
                player.getName().getString(),
                data.getState(),
                data.getDeathCount(),
                DownedManager.getRemainingSeconds(player),
                windowRemaining));

        ctx.getSource().sendSuccess(() -> message, true);
        return 1;
    }

    private static int down(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        DownedManager.enterDowned(player);
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        DownedManager.exitDowned(player);
        return 1;
    }
}
