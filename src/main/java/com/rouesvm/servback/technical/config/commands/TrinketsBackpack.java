package com.rouesvm.servback.technical.config.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.rouesvm.servback.content.item.ContainerItem;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

import static net.minecraft.server.command.CommandManager.literal;

public class TrinketsBackpack {
    public static void initialize(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("open").executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayer();
            Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);
            component.ifPresent(trinketComponent -> trinketComponent.forEach((slotReference, stack) -> {
                if (stack.getItem() instanceof ContainerItem containerItem) {
                    containerItem.onOpenGui(player, stack);
                }
            }));
            return 1;
        }));
    }
}
