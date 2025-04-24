package com.rouesvm.servback.utils;

import com.rouesvm.servback.ui.inventory.BackpackInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;

import java.util.Objects;
import java.util.UUID;

public class BackpackInstance {
    private UUID uuid;
    private BackpackInventory inventory;

    public long lastAccessed;

    public BackpackInstance(UUID uuid, BackpackInventory inventory) {
        this.uuid = uuid;
        this.inventory = inventory;
    }

    public BackpackInstance() {}

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

    public NbtCompound save(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound contents = new NbtCompound();
        contents.putUuid("uuid", this.uuid);
        contents.put("contents", this.inventory.save(registryLookup));
        contents.putLong("lastAccessed", lastAccessed);
        return contents;
    }

    public static BackpackInstance load(NbtCompound compound, RegistryWrapper.WrapperLookup registryLookup) {
        BackpackInstance backpackInstance = new BackpackInstance();
        backpackInstance.uuid = compound.getUuid("uuid");
        backpackInstance.inventory = BackpackInventory.load(compound.getCompound("contents"), registryLookup);
        backpackInstance.lastAccessed = compound.getLong("lastAccessed");
        return backpackInstance;
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
