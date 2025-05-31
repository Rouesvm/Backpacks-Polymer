package com.rouesvm.servback.config.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.rouesvm.servback.item.ContainerItem;
import com.rouesvm.servback.ui.BackpackGui;
import com.rouesvm.servback.utils.BackpackManager;
import com.rouesvm.servback.utils.BackpackUtils;
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
                    BackpackUtils.checkEnchantments(stack, player, containerItem.slots);
                    new BackpackGui(player, BackpackManager.getInstance(BackpackManager.getStackUUID(stack), containerItem.slots + BackpackUtils.getExtendedSlots(stack)));
                }
            }));
            return 1;
        }));
    }
}
