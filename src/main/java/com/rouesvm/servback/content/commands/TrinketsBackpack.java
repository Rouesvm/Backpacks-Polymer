package com.rouesvm.servback.content.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.rouesvm.servback.compat.trinkets.BackpackTrinket;
import com.rouesvm.servback.content.item.BundleGuiItem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import static net.minecraft.commands.Commands.literal;


public class TrinketsBackpack {
    public static void initialize(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("open").executes(context -> {
            ServerPlayer player = context.getSource().getPlayer();
            ItemStack stack = BackpackTrinket.getStackInBackSlot(player);
            if (!stack.isEmpty() && stack.getItem() instanceof BundleGuiItem item) {
                item.onOpenGui(player, stack);
            }
            return 1;
        }));
    }
}

