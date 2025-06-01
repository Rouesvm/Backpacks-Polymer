package com.rouesvm.servback.technical.ui.inventory;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class BackpackInventory extends BaseInventory {
    private BlockEntity entity;

    public BackpackInventory(int slots) {
        super(slots);
    }

    public BackpackInventory(DefaultedList<ItemStack> stacks) {
        super(stacks.size());
        this.heldStacks = stacks;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        super.setStack(slot, stack);
        if (entity != null) entity.markDirty();
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

    public void resize(int newSize) {
        DefaultedList<ItemStack> stacks = DefaultedList.ofSize(newSize, ItemStack.EMPTY);

        for(int i = 0; i < stacks.size(); ++i) {
            ItemStack itemStack = i < this.heldStacks.size() ? this.heldStacks.get(i) : ItemStack.EMPTY;
            stacks.set(i, itemStack.copy());
        }

        this.heldStacks = stacks;
    }

    public void setEntity(BlockEntity entity) {
        this.entity = entity;
    }
}
