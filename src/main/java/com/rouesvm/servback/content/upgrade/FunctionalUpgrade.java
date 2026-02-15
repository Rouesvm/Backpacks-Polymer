package com.rouesvm.servback.content.upgrade;

import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public interface FunctionalUpgrade {
    default boolean onUsed(Level world, ServerPlayer player, ItemStack stack) {
        return false;
    }

    default void tick(ServerPlayer player, ItemStack stack, ServerLevel world, Vec3 pos, BackpackInventory inventory) {}

    default void addTooltip(List<Component> tooltip, ItemStack stack, PacketContext context) {}
}
