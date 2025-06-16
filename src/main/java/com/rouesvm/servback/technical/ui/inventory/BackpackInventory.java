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

    public boolean insertItems(DefaultedList<ItemStack> target) {
        if (target != null && !target.isEmpty()) {
            markDirty();
            target.forEach(this::addStack);
            return true;
        }

        return false;
    }

    public void copyTo(BackpackInventory target) {
        for (int i = 0; i < target.size(); ++i) {
            ItemStack itemStack = i < this.size() ? this.getStack(i) : ItemStack.EMPTY;
            target.setStack(i, itemStack.copy());
        }
    }

    public void resize(int newSize) {
        if (this.size() != newSize) {
            DefaultedList<ItemStack> copy = DefaultedList.ofSize(newSize, ItemStack.EMPTY);
            int limit = Math.min(this.size(), newSize);

            for (int i = 0; i < limit; ++i) {
                copy.set(i, this.getStack(i).copy());
            }

            setInventoryDirectly(copy);
        }
    }

    public void setEntity(BlockEntity entity) {
        this.entity = entity;
    }
}
