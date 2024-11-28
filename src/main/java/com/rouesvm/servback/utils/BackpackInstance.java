package com.rouesvm.servback.utils;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;

import java.util.UUID;

public class BackpackInstance {
    public UUID uuid;
    public BackpackInventory backpackInventory;

    public BackpackInstance(UUID uuid, BackpackInventory inventory) {
        this.uuid = uuid;
        this.backpackInventory = inventory;
    }

    public NbtCompound save(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound contents = new NbtCompound();
        contents.putUuid("uuid", this.uuid);
        contents.put("contents", this.backpackInventory.save(registryLookup));
        return contents;
    }

    public static BackpackInstance load(NbtCompound compound, RegistryWrapper.WrapperLookup registryLookup) {
        UUID uuid = compound.getUuid("uuid");
        BackpackInventory inventory = new BackpackInventory();
        inventory.load(compound.getCompound("contents"), registryLookup);
        return new BackpackInstance(uuid, inventory);
    }
}
