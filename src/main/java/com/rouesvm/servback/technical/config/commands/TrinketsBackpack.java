package com.rouesvm.servback.technical.config.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.rouesvm.servback.content.item.ContainerItem;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.data.BackpackManager;
import com.rouesvm.servback.technical.data.BackpackUtils;
import com.rouesvm.servback.technical.ui.BackpackGui;
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
                    BackpackUtils.resizeIfIncorrectSize(player, stack, containerItem.slots);
                    Optional<BackpackInstance> instance = BackpackManager.getInstance(
                            BackpackManager.getStackUUID(stack),
                            containerItem.slots + BackpackUtils.getExtendedSlots(stack)
                    );
                    instance.ifPresent(backpackInstance -> new BackpackGui(player, backpackInstance));
                }
            }));
            return 1;
        }));
    }
}
