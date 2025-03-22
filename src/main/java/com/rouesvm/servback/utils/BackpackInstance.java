package com.rouesvm.servback.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.Objects;
import java.util.UUID;

public class BackpackInstance {
    private final UUID uuid;
    private BackpackInventory inventory;
    public long lastAccessed;

    public BackpackInstance(UUID uuid, BackpackInventory inventory) {
        this.uuid = uuid;
        this.inventory = inventory;
    }

    public void setInventory(BackpackInventory inventory) {
        this.inventory = inventory;
    }

    public void setLastAccessed() {
        this.lastAccessed = System.currentTimeMillis();
    }

    public UUID getUuid() {
        return uuid;
    }

    public BackpackInventory getInventory() {
        return inventory;
    }

    public DefaultedList<ItemStack> getHeldInventory() {
        return inventory.getHeldStacks();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BackpackInstance that = (BackpackInstance) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
}
