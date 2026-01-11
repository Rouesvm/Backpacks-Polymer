package com.rouesvm.servback.content.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.rouesvm.servback.content.item.BundleGuiItem;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

import static net.minecraft.commands.Commands.literal;

public class TrinketsBackpack {
    public static void initialize(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("open").executes(context -> {
            ServerPlayer player = context.getSource().getPlayer();
            Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);
            component.ifPresent(trinketComponent -> trinketComponent.forEach((slotReference, stack) -> {
                if (stack.getItem() instanceof BundleGuiItem item
                ) item.onOpenGui(player, stack);
            }));
            return 1;
        }));
    }
}
