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
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class BackpackCommands {
    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, a, b) -> init(dispatcher));
    }

    private static final Permission ADMIN_PERMISSION = new Permission.HasCommandLevel(PermissionLevel.ADMINS);

    public static void init(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (ServerBackpacks.hasTrinketLoaded) TrinketsBackpack.initialize(dispatcher);

        dispatcher.register(literal("backpacks")
                .requires(source -> source.permissions().hasPermission(ADMIN_PERMISSION))
                .executes(context -> {
                    context.getSource().sendSuccess(() -> Component.literal("Server Backpacks! by Rouesvm"), false);
                    return 1;
                }).then(literal("backup").executes(context -> {
                    context.getSource().sendSuccess(() -> Component.translatable("command.serverbackpacks.backup"), false);
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
                                    Component.translatable("command.serverbackpacks.incorrect_uuid"));

                        UUID uuid = UUID.fromString(search);
                        Optional<BackpackInstance> instance = BackpackManager.getInstance(uuid);

                        if (instance.isPresent()) {
                            new BackpackGui(context.getSource().getPlayer(), instance.get());
                        } else throw new CommandSyntaxException(
                                    CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException(),
                                    Component.translatable("command.serverbackpacks.incorrect_uuid"));
                    } else throw new CommandSyntaxException(
                                CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(),
                                Component.translatable("command.serverbackpacks.empty"));
                    return 1;
                })).then(configCommand())
        ));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> configCommand() {
        return literal("config")
                .then(literal("reset").executes(context -> {
                    Configuration.manager.instance = new Configuration.Instance();
                    context.getSource().sendSuccess(() -> Component.translatable("command.serverbackpacks.reset"), true);
                    return 1;
                })).then(literal("reload").executes(context -> {
                    Configuration.manager.load();
                    context.getSource().sendSuccess(() -> Component.translatable("command.serverbackpacks.reload"), true);
                    return 1;
                }))
                .then(literal("save").executes(context -> {
                    Configuration.manager.save();
                    context.getSource().sendSuccess(() -> Component.translatable("command.serverbackpacks.save"), true);
                    return 1;
                }));
    }

    public static int listBackpacks(CommandContext<CommandSourceStack> context, int page) {
        List<UUID> instances = new ArrayList<>(BackpackManager.instance().getBackpackUUIDs());

        int pageSize = 10;
        int totalPages = (int) Math.ceil(instances.size() / (double) pageSize);

        if (page < 1 || page > totalPages) {
            context.getSource().sendFailure(Component.literal("Invalid page number. Valid pages: 1-" + totalPages));
            return 0;
        }

        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, instances.size());
        List<UUID> pageInstances = instances.subList(startIndex, endIndex);

        context.getSource().sendSuccess(() -> Component.literal(String.format("=== Backpacks Instances (Page %d/%d) ===", page, totalPages))
                .withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD), false);

        for (UUID uuid : pageInstances) {
            context.getSource().sendSuccess(() -> Component.literal("* " + uuid)
                    .withStyle(ChatFormatting.WHITE)
                    .withStyle(style -> style
                            .withClickEvent(new ClickEvent.SuggestCommand("/backpacks open " + uuid))
                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to open UUID")))
                    ), false);
        }

        MutableComponent footer = Component.literal("");

        if (page > 1) {
            footer.append(Component.literal("[< Previous] ")
                    .withStyle(ChatFormatting.YELLOW)
                    .withStyle(style -> style.withClickEvent(new ClickEvent.RunCommand("/backpacks list " + (page - 1)))));
        }

        footer.append(Component.literal(String.format("Page %d/%d ", page, totalPages)).withStyle(ChatFormatting.GRAY));

        if (page < totalPages) {
            footer.append(Component.literal("[Next >]")
                    .withStyle(ChatFormatting.YELLOW)
                    .withStyle(style -> style.withClickEvent(new ClickEvent.RunCommand("/backpacks list " + (page + 1)))));
        }

        context.getSource().sendSuccess(() -> footer, false);

        return 1;
    }

}