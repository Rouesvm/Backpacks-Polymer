package com.rouesvm.servback.utils;

import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;

public class BackpackInventory {
    private DefaultedList<ItemStack> inventory;
    private int size;

    public BackpackInventory(int slots) {
        this.inventory = DefaultedList.ofSize(slots, ItemStack.EMPTY);
        this.size = slots;
    }

    public BackpackInventory() {
        this.inventory = DefaultedList.ofSize(9 * 6, ItemStack.EMPTY);
        this.size = this.inventory.size();
    }

    public int size() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setStack(int slot, ItemStack stack) {
        this.inventory.set(slot, stack);
    }

    public boolean insertItems(DefaultedList<ItemStack> itemStacks) {
        if (itemStacks != null && !itemStacks.isEmpty()) {
            SimpleInventory itemList = getSimpleInventory();
            itemStacks.forEach(itemList::addStack);

            this.inventory = itemList.getHeldStacks();
            return true;
        }

        return false;
    }

    public void copyTo(BackpackInventory inventory) {
        for(int i = 0; i < inventory.size(); ++i) {
            ItemStack itemStack = i < this.inventory.size() ? this.inventory.get(i) : ItemStack.EMPTY;
            inventory.setStack(i, itemStack.copy());
        }
    }

    public NbtCompound save(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound nbtCompound = new NbtCompound();
        return Inventories.writeNbt(nbtCompound, this.inventory, registryLookup);
    }

    public void load(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        Inventories.readNbt(nbtCompound, this.inventory, registryLookup);
        this.size = this.inventory.size();
    }

    public void setInventoryDirectly(DefaultedList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    public DefaultedList<ItemStack> getInventory() {
        return this.inventory;
    }

    public SimpleInventory getSimpleInventory() {
        return new SimpleInventory(this.inventory.toArray(ItemStack[]::new));
    }
}
