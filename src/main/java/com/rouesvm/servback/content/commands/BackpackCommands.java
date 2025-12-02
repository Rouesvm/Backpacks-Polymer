package com.rouesvm.servback.content.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.technical.config.Configuration;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.manager.BackpackManager;
import com.rouesvm.servback.technical.ui.BackpackGui;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BackpackCommands {
    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, a, b) -> init(dispatcher));
    }

    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        if (ServerBackpacks.hasTrinketLoaded) TrinketsBackpack.initialize(dispatcher);

        dispatcher.register(literal("backpacks")
                .requires(source -> Permissions.check(source, "serverbackpacks.command", 4))
                .executes(context -> {
                    context.getSource().sendFeedback(() -> Text.literal("Server Backpacks! by Rouesvm"), false);
                    return 1;
                }).then(literal("backup").executes(context -> {
                    context.getSource().sendFeedback(() -> Text.translatable("command.serverbackpacks.backup"), false);
                    BackpackManager.createBackupAndSave();
                    return 1;
                })).then(literal("list").executes(context -> listBackpacks(context, 1))
                                .then(argument("page", IntegerArgumentType.integer(1))
                                        .executes(context -> listBackpacks(context,
                                                IntegerArgumentType.getInteger(context, "page")))
                                )
                ).then(literal("open").then(argument("uuid", StringArgumentType.word()).executes(context -> {
                    String search = StringArgumentType.getString(context, "uuid");
                    if (!search.isEmpty()) {
                        if (search.length() != 36) throw new CommandSyntaxException(
                                    CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException(),
                                    Text.translatable("command.serverbackpacks.incorrect_uuid"));

                        UUID uuid = UUID.fromString(search);
                        Optional<BackpackInstance> instance = BackpackManager.getInstance(uuid);

                        if (instance.isPresent()) {
                            new BackpackGui(context.getSource().getPlayer(), instance.get());
                        } else throw new CommandSyntaxException(
                                    CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException(),
                                    Text.translatable("command.serverbackpacks.incorrect_uuid"));
                    } else throw new CommandSyntaxException(
                                CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(),
                                Text.translatable("command.serverbackpacks.empty"));
                    return 1;
                })).then(configCommand())
        ));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> configCommand() {
        return literal("config")
                .then(literal("reset").executes(context -> {
                    Configuration.manager.instance = new Configuration.Instance();
                    context.getSource().sendFeedback(() -> Text.translatable("command.serverbackpacks.reset"), true);
                    return 1;
                })).then(literal("reload").executes(context -> {
                    Configuration.manager.load();
                    context.getSource().sendFeedback(() -> Text.translatable("command.serverbackpacks.reload"), true);
                    return 1;
                }))
                .then(literal("save").executes(context -> {
                    Configuration.manager.save();
                    context.getSource().sendFeedback(() -> Text.translatable("command.serverbackpacks.save"), true);
                    return 1;
                }));
    }

    public static int listBackpacks(CommandContext<ServerCommandSource> context, int page) {
        List<UUID> instances = new ArrayList<>(BackpackManager.instance().discoveredBackpackUUIDs());

        int pageSize = 10;
        int totalPages = (int) Math.ceil(instances.size() / (double) pageSize);

        if (page < 1 || page > totalPages) {
            context.getSource().sendError(Text.literal("Invalid page number. Valid pages: 1-" + totalPages));
            return 0;
        }

        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, instances.size());
        List<UUID> pageInstances = instances.subList(startIndex, endIndex);

        sendToPlayer(context, Text.literal(String.format("-== Backpacks Instances (Page %d/%d) ==-", page, totalPages))
                .formatted(Formatting.WHITE, Formatting.BOLD));

        for (UUID uuid : pageInstances) {
            sendToPlayer(context, Text.literal("* " + uuid)
                    .formatted(Formatting.AQUA)
                    .styled(style -> style
                            .withClickEvent(new ClickEvent.SuggestCommand("/backpacks open " + uuid))
                            .withHoverEvent(new HoverEvent.ShowText(Text.literal("Click to open UUID")))
                    ));
        }

        MutableText footer = Text.literal("");

        if (page > 1) {
            footer.append(Text.literal("[< Previous] ")
                    .formatted(Formatting.YELLOW)
                    .styled(style -> style.withClickEvent(new ClickEvent.RunCommand("/backpacks list " + (page - 1)))));
        }

        footer.append(Text.literal(String.format("Page %d/%d ", page, totalPages)).formatted(Formatting.GRAY));

        if (page < totalPages) {
            footer.append(Text.literal("[Next >]")
                    .formatted(Formatting.YELLOW)
                    .styled(style -> style.withClickEvent(new ClickEvent.RunCommand("/backpacks list " + (page + 1)))));
        }

        sendToPlayer(context, footer);

        return 1;
    }

    private static void sendToPlayer(CommandContext<ServerCommandSource> context, Text text) {
        if (context.getSource().getPlayer() != null) {
            context.getSource().getPlayer().sendMessage(text, false);
        } else {
            context.getSource().sendFeedback(() -> text, false);
        }
    }
}