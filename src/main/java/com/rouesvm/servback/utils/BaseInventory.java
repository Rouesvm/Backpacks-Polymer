package com.rouesvm.servback.utils;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class BaseInventory implements Inventory, RecipeInputProvider {
    private final int size;
    public DefaultedList<ItemStack> heldStacks;

    @Nullable
    private List<InventoryChangedListener> listeners;

    public BaseInventory(int size) {
        this.size = size;
        this.heldStacks = DefaultedList.ofSize(size, ItemStack.EMPTY);
    }

    public BaseInventory(ItemStack... items) {
        this.size = items.length;
        this.heldStacks = DefaultedList.copyOf(ItemStack.EMPTY, items);
    }

    public void addListener(InventoryChangedListener listener) {
        if (this.listeners == null) {
            this.listeners = Lists.newArrayList();
        }

        this.listeners.add(listener);
    }

    public void removeListener(InventoryChangedListener listener) {
        if (this.listeners != null) {
            this.listeners.remove(listener);
        }

    }

    public ItemStack getStack(int slot) {
        return slot >= 0 && slot < this.heldStacks.size() ? this.heldStacks.get(slot) : ItemStack.EMPTY;
    }

    public List<ItemStack> clearToList() {
        List<ItemStack> list = this.heldStacks.stream().filter((stack) -> !stack.isEmpty()).collect(Collectors.toList());
        this.clear();
        return list;
    }

    public ItemStack removeStack(int slot, int amount) {
        ItemStack itemStack = Inventories.splitStack(this.heldStacks, slot, amount);
        if (!itemStack.isEmpty()) {
            this.markDirty();
        }

        return itemStack;
    }

    public ItemStack removeItem(Item item, int count) {
        ItemStack itemStack = new ItemStack(item, 0);

        for(int i = this.size - 1; i >= 0; --i) {
            ItemStack itemStack2 = this.getStack(i);
            if (itemStack2.getItem().equals(item)) {
                int j = count - itemStack.getCount();
                ItemStack itemStack3 = itemStack2.split(j);
                itemStack.increment(itemStack3.getCount());
                if (itemStack.getCount() == count) {
                    break;
                }
            }
        }

        if (!itemStack.isEmpty()) {
            this.markDirty();
        }

        return itemStack;
    }

    public ItemStack addStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            ItemStack itemStack = stack.copy();
            this.addToExistingSlot(itemStack);
            if (itemStack.isEmpty()) {
                return ItemStack.EMPTY;
            } else {
                this.addToNewSlot(itemStack);
                return itemStack.isEmpty() ? ItemStack.EMPTY : itemStack;
            }
        }
    }

    public boolean canInsert(ItemStack stack) {
        boolean bl = false;

        for(ItemStack itemStack : this.heldStacks) {
            if (itemStack.isEmpty() || ItemStack.areItemsAndComponentsEqual(itemStack, stack) && itemStack.getCount() < itemStack.getMaxCount()) {
                bl = true;
                break;
            }
        }

        return bl;
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
        return this.size;
    }

    public boolean isEmpty() {
        for(ItemStack itemStack : this.heldStacks) {
            if (!itemStack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public void markDirty() {
        if (this.listeners != null) {
            for(InventoryChangedListener inventoryChangedListener : this.listeners) {
                inventoryChangedListener.onInventoryChanged(this);
            }
        }

    }

    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    public void clear() {
        this.heldStacks.clear();
        this.markDirty();
    }



    @Override
    public void provideRecipeInputs(RecipeFinder finder) {
        for(ItemStack itemStack : this.heldStacks) {
            finder.addInput(itemStack);
        }
    }

    public String toString() {
        return (this.heldStacks.stream().filter((stack) -> !stack.isEmpty()).toList()).toString();
    }

    private void addToNewSlot(ItemStack stack) {
        for(int i = 0; i < this.size; ++i) {
            ItemStack itemStack = this.getStack(i);
            if (itemStack.isEmpty()) {
                this.setStack(i, stack.copyAndEmpty());
                return;
            }
        }

    }

    private void addToExistingSlot(ItemStack stack) {
        for(int i = 0; i < this.size; ++i) {
            ItemStack itemStack = this.getStack(i);
            if (ItemStack.areItemsAndComponentsEqual(itemStack, stack)) {
                this.transfer(stack, itemStack);
                if (stack.isEmpty()) {
                    return;
                }
            }
        }

    }

    private void transfer(ItemStack source, ItemStack target) {
        int i = this.getMaxCount(target);
        int j = Math.min(source.getCount(), i - target.getCount());
        if (j > 0) {
            target.increment(j);
            source.decrement(j);
            this.markDirty();
        }

    }

    public void readNbtList(NbtList list, RegistryWrapper.WrapperLookup registries) {
        this.clear();

        for(int i = 0; i < list.size(); ++i) {
            ItemStack.fromNbt(registries, list.getCompound(i)).ifPresent(this::addStack);
        }

    }

    public NbtList toNbtList(RegistryWrapper.WrapperLookup registries) {
        NbtList nbtList = new NbtList();

        for(int i = 0; i < this.size(); ++i) {
            ItemStack itemStack = this.getStack(i);
            if (!itemStack.isEmpty()) {
                nbtList.add(itemStack.toNbt(registries));
            }
        }

        return nbtList;
    }

    public DefaultedList<ItemStack> getHeldStacks() {
        return this.heldStacks;
    }
}
