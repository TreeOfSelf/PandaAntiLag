package me.TreeOfSelf.PandaAntiLag;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

public class PandaAntiLagCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("panda-anti-lag")
                .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.ADMINS)))
                .then(Commands.literal("reloadConfig")
                        .executes(ctx -> {
                            AntiLagSettings.loadConfig();
                            ctx.getSource().sendSuccess(() -> Component.literal("Reloaded the config file!").withStyle(ChatFormatting.GREEN), false);
                            return 1;
                        })
                )
        );
    }
}
