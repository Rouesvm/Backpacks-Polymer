package com.rouesvm.servback.utils;

import com.rouesvm.servback.components.BackpacksDataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;

import java.util.*;

public class BackpackManager {
    public BackpackInventory globalInventory = new BackpackInventory(9 * 3);
    public Map<UUID, BackpackInstance> storedInstances = new HashMap<>();

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
            if (storedInstances.containsKey(uuid))
                uuid = UUID.randomUUID();

            uuidString = uuid.toString();
            stack.set(BackpacksDataComponentTypes.UUID_TYPE, uuidString);
            return uuid;
        }
        return UUID.fromString(uuidString);
    }

    public BackpackInstance getInstance(UUID uuid, int slots) {
        if (this.storedInstances.containsKey(uuid)) {
            BackpackInstance backpack = this.storedInstances.get(uuid);
            BackpackInventory backpackInventory = backpack.backpackInventory;
            if (backpackInventory.size() != slots) {
                BackpackInventory newInventory = new BackpackInventory(slots);
                newInventory.setInventoryDirectly(backpackInventory.getHeldStacks());

                backpack.backpackInventory = newInventory;
                saveBackpack(backpack);
            }

            if (!backpackInventory.getHeldStacks().isEmpty())
                return backpack;
        }
        return new BackpackInstance(uuid, new BackpackInventory(slots));
    }

    public BackpackInventory getInventory(UUID uuid, int slots) {
        if (this.storedInstances.containsKey(uuid)) {
            BackpackInstance backpack = this.storedInstances.get(uuid);
            BackpackInventory backpackInventory = backpack.backpackInventory;
            if (backpackInventory.size() != slots) {
                BackpackInventory newInventory = new BackpackInventory(slots);
                newInventory.setInventoryDirectly(backpackInventory.getHeldStacks());

                backpackInventory = newInventory;
                saveBackpack(uuid, backpackInventory);
            }

            if (!backpackInventory.getHeldStacks().isEmpty())
                return backpackInventory;
        }

        return new BackpackInventory(slots);
    }

    public void saveBackpack(BackpackInstance instance) {
        if (instance.uuid != null && instance.backpackInventory != null) {
            this.storedInstances.put(instance.uuid, instance);
        }
    }

    public void saveBackpack(UUID uuid, BackpackInventory backpackInventory) {
        saveBackpack(new BackpackInstance(uuid, backpackInventory));
    }

    public static void loadNbt(Set<BackpackInstance> instances, NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.getListOrEmpty("backpackContents").forEach(element ->
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
        instances.forEach(this::saveBackpack);
    }

    public Set<BackpackInstance> save() {
        Set<BackpackInstance> backpackInstances = new HashSet<>();
        this.storedInstances.forEach((key, value) -> backpackInstances.add(value));
        return backpackInstances;
    }

    public void setGlobalInventory(DefaultedList<ItemStack> stacks) {
        this.globalInventory.setInventoryDirectly(stacks);
    }
}
