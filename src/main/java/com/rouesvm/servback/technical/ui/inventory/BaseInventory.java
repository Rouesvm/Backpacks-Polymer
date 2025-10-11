package com.rouesvm.servback.technical.ui.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.util.collection.DefaultedList;

public class BaseInventory implements Inventory, RecipeInputProvider {
    private DefaultedList<ItemStack> heldStacks;

    public BaseInventory(int size) {
        this.heldStacks = DefaultedList.ofSize(size, ItemStack.EMPTY);
    }

    public BaseInventory(DefaultedList<ItemStack> stacks) {
        this.heldStacks = stacks;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return canInsert(stack);
    }

    public ItemStack getStack(int slot) {
        return slot >= 0 && slot < this.heldStacks.size() ? this.heldStacks.get(slot) : ItemStack.EMPTY;
    }

    public ItemStack removeStack(int slot, int amount) {
        ItemStack itemStack = Inventories.splitStack(this.heldStacks, slot, amount);
        if (!itemStack.isEmpty()) {
            this.markDirty();
        }

        return itemStack;
    }

    public ItemStack addStack(ItemStack stack) {
        return BaseInventory.addStack(stack, this);
    }

    public boolean canInsert(ItemStack stack) {
        return BaseInventory.canInsert(stack, this);
    }

    public ItemStack removeStack(int slot) {
        ItemStack itemStack = this.heldStacks.get(slot);
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.heldStacks.set(slot, ItemStack.EMPTY);
            return itemStack;
        }
    }

    public void setStack(int slot, ItemStack stack) {
        this.heldStacks.set(slot, stack);
        stack.capCount(this.getMaxCount(stack));
        this.markDirty();
    }

    public int size() {
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

    public void markDirty() {}

    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    public void clear() {
        this.heldStacks.clear();
        this.markDirty();
    }

    @Override
    public void provideRecipeInputs(RecipeMatcher finder) {
        for(ItemStack itemStack : this.heldStacks()) {
            finder.addInput(itemStack);
        }
    }

    public String toString() {
        return (this.heldStacks.stream().filter((stack) -> !stack.isEmpty()).toList()).toString();
    }

    public DefaultedList<ItemStack> heldStacks() {
        return this.heldStacks;
    }

    public void setInventoryDirectly(DefaultedList<ItemStack> inventory) {
        this.heldStacks = inventory;
    }

    public static ItemStack addStack(ItemStack stack, Inventory inventory) {
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

    private static void addToNewSlot(ItemStack stack, Inventory inventory) {
        for(int i = 0; i < inventory.size(); ++i) {
            ItemStack itemStack = inventory.getStack(i);
            if (itemStack.isEmpty()) {
                inventory.setStack(i, stack.copyAndEmpty());
                return;
            }
        }

    }

    private static void addToExistingSlot(ItemStack stack, Inventory inventory) {
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack itemStack = inventory.getStack(i);
            if (ItemStack.areItemsAndComponentsEqual(itemStack, stack)) {
                BaseInventory.transfer(stack, itemStack, inventory);
                if (stack.isEmpty()) {
                    return;
                }
            }
        }
    }

    @Override
    public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
        return canInsert(stack);
    }

    public static boolean isFull(BaseInventory inventory) {
        for (ItemStack stack : inventory.heldStacks()) {
            if (stack.isEmpty() || stack.getCount() < stack.getMaxCount()) {
                return false;
            }
        }
        return true;
    }

    public static boolean canInsert(ItemStack stack, BaseInventory inventory) {
        boolean bl = false;

        if (!stack.getItem().canBeNested()) return false;

        for(ItemStack itemStack : inventory.heldStacks()) {
            if (itemStack.isEmpty()  || ItemStack.areItemsAndComponentsEqual(itemStack, stack) && itemStack.getCount() < itemStack.getMaxCount()) {
                bl = true;
                break;
            }
        }

        return bl;
    }

    private static void transfer(ItemStack source, ItemStack target, Inventory inventory) {
        int i = inventory.getMaxCount(target);
        int j = Math.min(source.getCount(), i - target.getCount());
        if (j > 0) {
            target.increment(j);
            source.decrement(j);
            inventory.markDirty();
        }
    }
}
