package com.rouesvm.servback.slots;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class DisabledSlot extends Slot {
    private final ItemStack stack;

    public DisabledSlot(ItemStack root, Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.stack = root;
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return !this.getStack().equals(this.stack);
    }
}
