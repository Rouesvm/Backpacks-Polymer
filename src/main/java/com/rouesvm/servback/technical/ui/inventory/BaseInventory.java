package com.rouesvm.servback.technical.ui.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class BaseInventory implements Container, StackedContentsCompatible {
    private NonNullList<ItemStack> heldStacks;

    public BaseInventory(int size) {
        this.heldStacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public BaseInventory(NonNullList<ItemStack> stacks) {
        this.heldStacks = stacks;
    }

    @Override
    public boolean canPlaceItem(int slot, @NonNull ItemStack stack) {
        return canInsert(stack);
    }

    public @NonNull ItemStack getItem(int slot) {
        return slot >= 0 && slot < this.heldStacks.size() ? this.heldStacks.get(slot) : ItemStack.EMPTY;
    }

    public @NonNull ItemStack removeItem(int slot, int amount) {
        ItemStack itemStack = ContainerHelper.removeItem(this.heldStacks, slot, amount);
        if (!itemStack.isEmpty()) {
            this.setChanged();
        }

        return itemStack;
    }

    public ItemStack addStack(ItemStack stack) {
        return BaseInventory.addStack(stack, this);
    }

    public boolean canInsert(ItemStack stack) {
        return BaseInventory.canInsert(stack, this);
    }

    public @NonNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack itemStack = this.heldStacks.get(slot);
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.heldStacks.set(slot, ItemStack.EMPTY);
            return itemStack;
        }
    }

    public void setItem(int slot, @NonNull ItemStack stack) {
        this.heldStacks.set(slot, stack);
        stack.limitSize(this.getMaxStackSize(stack));
        this.setChanged();
    }

    public int getContainerSize() {
        return this.heldStacks.size();
    }

    public boolean isEmpty() {
        for(ItemStack itemStack : this.heldStacks) {
            if (!itemStack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public void setChanged() {}

    public boolean stillValid(@NonNull Player player) {
        return true;
    }

    public void clearContent() {
        this.heldStacks.clear();
        this.setChanged();
    }

    @Override
    public void fillStackedContents(@NonNull StackedItemContents finder) {
        for(ItemStack itemStack : this.heldStacks) {
            finder.accountStack(itemStack);
        }
    }

    public String toString() {
        return (this.heldStacks.stream().filter((stack) -> !stack.isEmpty()).toList()).toString();
    }

    public NonNullList<ItemStack> heldStacks() {
        return this.heldStacks;
    }

    public void setInventoryDirectly(NonNullList<ItemStack> inventory) {
        this.heldStacks = inventory;
    }

    public static ItemStack addStack(ItemStack stack, Container inventory) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        } else if (!canInsert(stack, inventory)) {
                return ItemStack.EMPTY;
        } else {
            ItemStack itemStack = stack.copy();
            BaseInventory.addToExistingSlot(itemStack, inventory);
            if (itemStack.isEmpty()) {
                return ItemStack.EMPTY;
            } else {
                BaseInventory.addToNewSlot(itemStack, inventory);
                return itemStack.isEmpty() ? ItemStack.EMPTY : itemStack;
            }
        }
    }

    private static void addToNewSlot(ItemStack stack, Container inventory) {
        for(int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack.isEmpty()) {
                inventory.setItem(i, stack.copyAndClear());
                return;
            }
        }

    }

    private static void addToExistingSlot(ItemStack stack, Container inventory) {
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack itemStack = inventory.getItem(i);
            if (ItemStack.isSameItemSameComponents(itemStack, stack)) {
                BaseInventory.transfer(stack, itemStack, inventory);
                if (stack.isEmpty()) {
                    return;
                }
            }
        }
    }

    @Override
    public boolean canTakeItem(@NonNull Container hopperInventory, int slot, @NonNull ItemStack stack) {
        return canInsert(stack);
    }

    public static boolean isFull(Container inventory) {
        for (ItemStack stack : inventory) {
            if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) {
                return false;
            }
        }
        return true;
    }

    public static boolean canInsert(ItemStack stack, Container inventory) {
        boolean bl = false;

        if (!stack.getItem().canFitInsideContainerItems()) return false;

        for(ItemStack itemStack : inventory) {
            if (itemStack.isEmpty()  || ItemStack.isSameItemSameComponents(itemStack, stack) && itemStack.getCount() < itemStack.getMaxStackSize()) {
                bl = true;
                break;
            }
        }

        return bl;
    }

    private static void transfer(ItemStack source, ItemStack target, Container inventory) {
        int i = inventory.getMaxStackSize(target);
        int j = Math.min(source.getCount(), i - target.getCount());
        if (j > 0) {
            target.grow(j);
            source.shrink(j);
            inventory.setChanged();
        }
    }
}
