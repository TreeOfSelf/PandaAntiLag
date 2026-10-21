package me.TreeOfSelf.PandaAntiLag;

import com.mojang.brigadier.CommandDispatcher;
import me.TreeOfSelf.PandaAntiLag.mixin.accessor.TadpoleEntityAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.frog.Tadpole;

import java.util.List;

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
                .then(Commands.literal("fixTadpoles")
                        .executes(ctx -> {
                            int fixed = 0;
                            int locked = 0;
                            for (ServerLevel level : ctx.getSource().getServer().getAllLevels()) {
                                for (Tadpole tadpole : List.copyOf(level.getEntities(EntityTypes.TADPOLE, entity -> true))) {
                                    if (tadpole.isAgeLocked()) {
                                        locked++;
                                        continue;
                                    }
                                    TadpoleEntityAccessor accessor = (TadpoleEntityAccessor) tadpole;
                                    int age = accessor.getAge();
                                    if (age >= Tadpole.ticksToBeFrog) {
                                        // invokeSetAge runs the vanilla conversion check.
                                        accessor.invokeSetAge(age);
                                        fixed++;
                                    }
                                }
                            }
                            final int fixedCount = fixed;
                            final int lockedCount = locked;
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "Converted " + fixedCount + " stuck tadpole(s)"
                                            + (lockedCount > 0 ? "; skipped " + lockedCount + " age-locked" : ""))
                                    .withStyle(ChatFormatting.GREEN), true);
                            return fixedCount;
                        })
                )
        );
    }
}
