package com.rouesvm.servback.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Uuids;
import net.minecraft.util.collection.DefaultedList;

import java.util.Objects;
import java.util.UUID;

public class BackpackInstance {
    public UUID uuid;
    public BackpackInventory backpackInventory;
    public long lastAccessed;

    public BackpackInstance(UUID uuid, BackpackInventory inventory) {
        this.uuid = uuid;
        this.backpackInventory = inventory;
    }

    public BackpackInstance() {
    }

    public void setLastAccessed() {
        this.lastAccessed = System.currentTimeMillis();
    }

    public NbtCompound save(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound contents = new NbtCompound();
        contents.put("uuid", Uuids.CODEC, this.uuid);

        contents.put("contents", this.backpackInventory.save(registryLookup));
        contents.putLong("lastAccessed", lastAccessed);
        return contents;
    }

    public static BackpackInstance load(NbtCompound compound, RegistryWrapper.WrapperLookup registryLookup) {
        BackpackInstance backpackInstance = new BackpackInstance();
        backpackInstance.uuid = compound.get("uuid", Uuids.CODEC).get();
        backpackInstance.backpackInventory = BackpackInventory.load(compound.getCompoundOrEmpty("contents"), registryLookup);
        backpackInstance.lastAccessed = compound.getLong("lastAccessed", 0);
        return backpackInstance;
    }

    public UUID getUuid() {
        return uuid;
    }

    public BackpackInventory getBackpackInventory() {
        return backpackInventory;
    }

    public DefaultedList<ItemStack> getHeldInventory() {
        return backpackInventory.getHeldStacks();
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
