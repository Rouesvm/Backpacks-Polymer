package com.rouesvm.servback.content.upgrade;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public interface ClickableUpgrade {
    default boolean onClicked(ServerPlayer player, ItemStack stack, Slot slot, ClickAction clickType, boolean inContainer) {
        return false;
    }

    default boolean onClicked(ServerPlayer player, ItemStack stack, Slot slot, eu.pb4.sgui.api.ClickType clickType, boolean inContainer) {
        ClickAction mapped = clickType.isLeft ? ClickAction.PRIMARY :
                clickType.isRight ? ClickAction.SECONDARY : ClickAction.PRIMARY;
        return onClicked(player, stack, slot, mapped, inContainer);
    }
}
