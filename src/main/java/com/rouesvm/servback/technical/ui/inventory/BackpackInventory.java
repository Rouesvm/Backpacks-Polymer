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
        super(stacks);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        super.setStack(slot, stack);
        if (entity != null) entity.markDirty();
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
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack itemStack = i < this.size() ? this.getStack(i) : ItemStack.EMPTY;
            inventory.setStack(i, itemStack.copy());
        }
    }

    public void resize(int newSize) {
        if (this.size() != newSize) {
            DefaultedList<ItemStack> stacks = DefaultedList.ofSize(newSize, ItemStack.EMPTY);

            for (int i = 0; i < stacks.size(); ++i) {
                ItemStack itemStack = i < this.size() ? this.getStack(i) : ItemStack.EMPTY;
                stacks.set(i, itemStack.copy());
            }

            setInventoryDirectly(stacks);
        }
    }

    public void setEntity(BlockEntity entity) {
        this.entity = entity;
    }
}
