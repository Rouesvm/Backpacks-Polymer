package com.rouesvm.servback.content.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.registry.BackpackPersistentDataAttachmentRegistry;
import com.rouesvm.servback.technical.config.Configuration;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.manager.BackpackManager;
import com.rouesvm.servback.technical.ui.BackpackGui;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
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
    private static final Permission REGULAR_PERMISSION = new Permission.HasCommandLevel(PermissionLevel.ALL);

    public static void init(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (ServerBackpacks.hasTrinketLoaded) TrinketsBackpack.initialize(dispatcher);

        dispatcher.register(literal("backpacks")
                .requires(source -> source.permissions().hasPermission(REGULAR_PERMISSION))
                .executes(context -> {
                    context.getSource().sendSuccess(() -> Component.literal("Server Backpacks! by Rouesvm"), false);
                    return 1;
                }).then(literal("backup")
                        .requires(source -> source.permissions().hasPermission(ADMIN_PERMISSION))
                        .executes(context -> {
                            context.getSource().sendSuccess(() -> Component.translatable("command.serverbackpacks.backup"), false);
                            BackpackManager.createBackupAndSave();
                            return 1;
                })).then(literal("list")
                        .requires(source -> source.permissions().hasPermission(ADMIN_PERMISSION))
                        .executes(context -> listBackpacks(context, 1))
                                .then(argument("page", IntegerArgumentType.integer(1))
                                        .executes(context -> listBackpacks(context,
                                                IntegerArgumentType.getInteger(context, "page")))
                                )
                ).then(literal("open")
                        .requires(source -> source.permissions().hasPermission(ADMIN_PERMISSION))
                        .then(argument("uuid", StringArgumentType.word())
                                .executes(context -> {
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
                                })).then(configCommand()))
                .then(toggleCommand()));
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

    private static LiteralArgumentBuilder<CommandSourceStack> toggleCommand() {
        return literal("set")
                .requires(source -> source.permissions().hasPermission(REGULAR_PERMISSION))
                .then(booleanAttachmentCommand(
                        "place_with_mainhand_full",
                        BackpackPersistentDataAttachmentRegistry.PLACE_WITH_HAND_FULL
                ))
                .then(booleanAttachmentCommand(
                        "place_with_offhand_full",
                        BackpackPersistentDataAttachmentRegistry.PLACE_WITH_OFFHAND_FULL
                ))
                .then(booleanAttachmentCommand(
                        "backpack_self_visibility",
                        BackpackPersistentDataAttachmentRegistry.BACKPACK_SELF_VISIBILITY
                ))
                .then(booleanAttachmentCommand(
                        "hide_backpack_when_looking_down",
                        BackpackPersistentDataAttachmentRegistry.HIDE_BACKPACK_WHEN_LOOKING_DOWN
                ));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> booleanAttachmentCommand(String name, AttachmentType<Boolean> attachment) {
        return literal(name)
                .executes(context -> getAttachment(context.getSource(), attachment))
                .then(argument("value", BoolArgumentType.bool())
                        .executes(context -> setAttachment(
                                context.getSource(),
                                attachment,
                                BoolArgumentType.getBool(context, "value")
                        )));
    }

    private static int getAttachment(CommandSourceStack source, AttachmentType<Boolean> attachment) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        boolean value = player.getAttachedOrElse(attachment, true);
        source.sendSuccess(() -> Component.translatable("command.serverbackpacks.get", String.valueOf(value)), false);

        return 1;
    }

    private static int setAttachment(CommandSourceStack source, AttachmentType<Boolean> attachment, boolean value) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        player.setAttached(attachment, value);
        source.sendSuccess(() -> Component.translatable("command.serverbackpacks.set", String.valueOf(value)), value);

        return 1;
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