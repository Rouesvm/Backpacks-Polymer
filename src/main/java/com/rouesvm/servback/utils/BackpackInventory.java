package com.rouesvm.servback.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class BackpackInventory extends BaseInventory {
    public BackpackInventory(int slots) {
        super(slots);
    }

    public BackpackInventory(DefaultedList<ItemStack> stacks) {
        super(stacks.size());
        this.heldStacks = stacks;
    }

    public BackpackInventory(int slots, DefaultedList<ItemStack> stacks) {
        super(slots);
        this.heldStacks = stacks;
    }

    public boolean insertItems(DefaultedList<ItemStack> itemStacks) {
        if (itemStacks != null && !itemStacks.isEmpty()) {
            markDirty();
            itemStacks.forEach(this::addStack);
            return true;
        }

        return false;
    }

    public void copyTo(BackpackInventory inventory) {
        for(int i = 0; i < inventory.size(); ++i) {
            ItemStack itemStack = i < this.heldStacks.size() ? this.heldStacks.get(i) : ItemStack.EMPTY;
            inventory.setStack(i, itemStack.copy());
        }
    }

    public void setInventoryDirectly(DefaultedList<ItemStack> inventory) {
        this.heldStacks = inventory;
    }
}
