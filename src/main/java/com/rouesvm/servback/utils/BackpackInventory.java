package com.rouesvm.servback.utils;

import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;

public class BackpackInventory extends SimpleInventory {

    public BackpackInventory(int slots) {
        super(slots);
    }

    public BackpackInventory(DefaultedList<ItemStack> stacks) {
        super(stacks.size());

        this.heldStacks.clear();
        this.heldStacks.addAll(stacks);
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

    public NbtCompound save(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound nbtCompound = new NbtCompound();
        return Inventories.writeNbt(nbtCompound, this.heldStacks, registryLookup);
    }

    public static BackpackInventory load(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(9 * 6, ItemStack.EMPTY);
        Inventories.readNbt(nbtCompound, itemStacks, registryLookup);

        return new BackpackInventory(itemStacks);
    }

    public void setInventoryDirectly(DefaultedList<ItemStack> inventory) {
        this.heldStacks.clear();
        this.heldStacks.addAll(inventory);
    }
}
