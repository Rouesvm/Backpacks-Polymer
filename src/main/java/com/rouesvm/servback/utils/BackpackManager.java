package com.rouesvm.servback.utils;

import com.rouesvm.servback.components.BackpacksDataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BackpackManager {
    public HashMap<UUID, BackpackInventory> storedInventories = new HashMap<>();

    public UUID getStackUUID(ItemStack stack) {
        String uuidString = stack.get(BackpacksDataComponentTypes.UUID_TYPE);
        if (uuidString == null)
            uuidString = String.valueOf(createNewUUID(stack));
        return UUID.fromString(uuidString);
    }

    public UUID createNewUUID(ItemStack stack) {
        String uuidString = stack.get(BackpacksDataComponentTypes.UUID_TYPE);
        if (uuidString == null) {
            UUID uuid = UUID.randomUUID();
            if (storedInventories.containsKey(uuid))
                uuid = UUID.randomUUID();

            uuidString = uuid.toString();
            stack.set(BackpacksDataComponentTypes.UUID_TYPE, uuidString);
            return uuid;
        }
        return UUID.fromString(uuidString);
    }

    public BackpackInventory getInventory(UUID uuid, int slots) {
        if (this.storedInventories.containsKey(uuid)) {
            BackpackInventory backpackInventory = this.storedInventories.get(uuid);
            backpackInventory.setSize(slots);

            saveBackpack(uuid, backpackInventory);
            if (backpackInventory.getInventory().isEmpty())
                return new BackpackInventory(slots);
            else return backpackInventory;
        } else return new BackpackInventory(slots);
    }

    public boolean saveBackpack(UUID uuid, BackpackInventory backpackInventory) {
        if (uuid != null && backpackInventory != null) {
            this.storedInventories.remove(uuid);
            this.storedInventories.put(uuid, backpackInventory);
            return true;
        }
        return false;
    }

    public boolean saveBackpack(BackpackInstance backpackInstance) {
        return saveBackpack(backpackInstance.uuid, backpackInstance.backpackInventory);
    }

    public static void loadNbt(Set<BackpackInstance> instances, NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.getList("backpackContents", NbtCompound.COMPOUND_TYPE).forEach(element ->
                instances.add(BackpackInstance.load((NbtCompound) element, registryLookup)));
    }

    public static void saveNbt(Set<BackpackInstance> instances, NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        if (!instances.isEmpty()) {
            NbtList nbtList = new NbtList();
            instances.forEach(instance -> nbtList.add(instance.save(registryLookup)));
            nbt.put("backpackContents", nbtList);
        }
    }

    public void load(Set<BackpackInstance> instances) {
        instances.forEach(backpackInstance -> this.storedInventories.put(backpackInstance.uuid, backpackInstance.backpackInventory));
    }

    public Set<BackpackInstance> save() {
        Set<BackpackInstance> backpackInstances = new HashSet<>();
        this.storedInventories.forEach((key, value) -> backpackInstances.add(new BackpackInstance(key, value)));
        return backpackInstances;
    }
}
