package com.rouesvm.servback.content.upgrade;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;

public interface ClickableUpgrade {
    default boolean onClicked(ServerPlayerEntity player, ItemStack stack, Slot slot, ClickType clickType) {
        return false;
    }

    default boolean onClicked(ServerPlayerEntity player, ItemStack stack, Slot slot, eu.pb4.sgui.api.ClickType clickType) {
        ClickType mapped = clickType.isLeft ? ClickType.LEFT :
                clickType.isRight ? ClickType.RIGHT : ClickType.LEFT;
        return onClicked(player, stack, slot, mapped);
    }
}
