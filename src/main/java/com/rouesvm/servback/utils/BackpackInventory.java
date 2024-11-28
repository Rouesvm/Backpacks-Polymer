package com.rouesvm.servback.utils;

import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;

public class BackpackInventory {
    private DefaultedList<ItemStack> inventory;

    public BackpackInventory(int slots) {
        this.inventory = DefaultedList.ofSize(slots, ItemStack.EMPTY);
    }

    public boolean insertItems(DefaultedList<ItemStack> itemStacks) {
        if (itemStacks != null) {
            SimpleInventory itemList = getSimpleInventory();
            itemStacks.forEach(itemList::addStack);

            this.inventory = itemList.getHeldStacks();
            return true;
        }

        return false;
    }

    public NbtCompound save(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound nbtCompound = new NbtCompound();
        return Inventories.writeNbt(nbtCompound, this.inventory, registryLookup);
    }

    public void load(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        Inventories.readNbt(nbtCompound, this.inventory, registryLookup);
    }

    public int slots() {
        return this.inventory.size();
    }

    public void setInventory(DefaultedList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    public DefaultedList<ItemStack> getInventory() {
        return this.inventory;
    }

    public SimpleInventory getSimpleInventory() {
        return new SimpleInventory(this.inventory.toArray(ItemStack[]::new));
    }
}
