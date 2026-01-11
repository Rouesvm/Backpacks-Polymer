package com.rouesvm.servback.technical.ui.inventory;

import com.rouesvm.servback.technical.manager.BackpackManager;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.UUID;

public class BackpackInventory extends BaseInventory {
    private BlockEntity entity;

    public BackpackInventory(int slots) {
        super(slots);
    }

    public BackpackInventory(NonNullList<ItemStack> stacks) {
        super(stacks);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        if (entity != null) entity.setChanged();
    }

    public boolean insertItems(NonNullList<ItemStack> target) {
        if (target != null && !target.isEmpty()) {
            setChanged();
            target.forEach(this::addStack);
            return true;
        }

        return false;
    }

    public void copyTo(BackpackInventory target) {
        for (int i = 0; i < target.getContainerSize(); ++i) {
            ItemStack itemStack = i < this.getContainerSize() ? this.getItem(i) : ItemStack.EMPTY;
            target.setItem(i, itemStack.copy());
        }
    }

    public void resize(int newSize) {
        if (this.getContainerSize() != newSize) {
            NonNullList<ItemStack> copy = NonNullList.withSize(newSize, ItemStack.EMPTY);
            int limit = Math.min(this.getContainerSize(), newSize);

            for (int i = 0; i < limit; ++i) {
                copy.set(i, this.getItem(i).copy());
            }

            setInventoryDirectly(copy);
        }
    }

    public void setEntity(BlockEntity entity) {
        this.entity = entity;
    }

    public static void resizeInventory(UUID uuid, int newSize) {
        if (uuid != null) BackpackManager.getInstance(uuid).ifPresent(
                backpackInstance ->
                        backpackInstance.inventory().resize(newSize));
    }
}
