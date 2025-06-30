package com.rouesvm.servback.technical.data;

import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
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

    public void saveToInventory(BackpackInventory target) {
        target.copyTo(this.inventory);
    }

    public void setLastAccessed() {
        this.lastAccessed = System.currentTimeMillis();
    }

    public UUID getUuid() {
        return uuid;
    }

    public BackpackInventory inventory() {
        return inventory;
    }

    public DefaultedList<ItemStack> heldInventory() {
        return inventory.heldStacks();
    }

    public BackpackInstance copy() {
        BackpackInventory target = new BackpackInventory(inventory.size());
        inventory.copyTo(target);
        return new BackpackInstance(this.uuid, target);
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
