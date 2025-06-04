package com.rouesvm.servback.technical.config.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.technical.config.Configuration;
import com.rouesvm.servback.technical.ui.BackpackGui;
import com.rouesvm.servback.data.BackpackInstance;
import com.rouesvm.servback.data.BackpackManager;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Set;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BackpackCommands {
    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        if (ServerBackpacks.hasTrinketLoaded) TrinketsBackpack.initialize(dispatcher);

        dispatcher.register(literal("backpacks")
                .requires(source -> Permissions.check(source, "serverbackpacks.command", 4))
                .executes(context -> {
                    context.getSource().sendFeedback(() -> Text.literal("Server Backpacks! by Rouesvm"), false);
                    return 1;
                }).then(literal("list").executes(context -> {
                    Set<UUID> instances = BackpackManager.instance.storedInstances.keySet();
                    context.getSource().sendFeedback(
                            () -> Text.translatable("command.serverbackpacks.list"), false);
                    for (UUID uuid : instances) {
                        context.getSource().sendFeedback(
                                () -> Text.literal(String.format("(%s)", uuid.toString())), false);
                    }
                    return 1;
                })).then(literal("open").then(argument("uuid", StringArgumentType.word()).executes(context -> {
                    String search = StringArgumentType.getString(context, "uuid");
                    if (!search.isEmpty()) {
                        if (search.length() != 36) {
                            throw new CommandSyntaxException(
                                    CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException(),
                                    Text.translatable("command.serverbackpacks.incorrect_uuid"));
                        }

                        UUID uuid = UUID.fromString(search);
                        BackpackInstance instance = BackpackManager.getInstance(uuid);
                        if (instance != null) {
                            new BackpackGui(context.getSource().getPlayer(), instance);
                        } else {
                            throw new CommandSyntaxException(
                                    CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException(),
                                    Text.translatable("command.serverbackpacks.incorrect_uuid"));
                        }
                    } else {
                        throw new CommandSyntaxException(
                                CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(),
                                Text.translatable("command.serverbackpacks.empty"));
                    }
                    return 1;
                }))).then(configCommand())
        );
    }

    public static LiteralArgumentBuilder<ServerCommandSource> configCommand() {
        return literal("config")
                .then(literal("reset").executes(context -> {
                    Configuration.manager.instance = new Configuration.Instance();
                    context.getSource().sendFeedback(
                            () -> Text.translatable("command.serverbackpacks.reset"), true);
                    return 1;
                })).then(literal("reload").executes(context -> {
                    Configuration.manager.load();
                    context.getSource().sendFeedback(
                            () -> Text.translatable("command.serverbackpacks.reload"), true);
                    return 1;
                }))
                .then(literal("save").executes(context -> {
                    Configuration.manager.save();
                    context.getSource().sendFeedback(
                            () -> Text.translatable("command.serverbackpacks.save"), true);
                    return 1;
                }));
    }
}