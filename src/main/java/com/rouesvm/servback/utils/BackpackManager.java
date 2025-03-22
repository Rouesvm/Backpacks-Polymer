package com.rouesvm.servback.utils;

import com.rouesvm.servback.components.BackpacksDataComponentTypes;
import com.rouesvm.servback.state.StateSaverAndLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.collection.DefaultedList;

import java.util.*;

public class BackpackManager {
    private static BackpackManager manager = null;

    public BackpackInventory globalInventory = new BackpackInventory(9 * 3);
    public Map<UUID, BackpackInstance> storedInstances = new HashMap<>();

    public static BackpackManager getManager() {
        return manager;
    }

    public static void setup(MinecraftServer server) {
        manager = new BackpackManager();
        manager.load(server);
    }

    public static void destroy(MinecraftServer server) {
        manager.save(server);
        manager = null;
    }

    public static UUID getStackUUID(ItemStack stack) {
        String uuidString = stack.get(BackpacksDataComponentTypes.UUID_TYPE);
        if (uuidString == null)
            uuidString = String.valueOf(createNewUUID(stack));
        return UUID.fromString(uuidString);
    }

    public static UUID createNewUUID(ItemStack stack) {
        String uuidString = stack.get(BackpacksDataComponentTypes.UUID_TYPE);
        if (uuidString == null) {
            UUID uuid = UUID.randomUUID();
            if (manager.storedInstances.containsKey(uuid))
                uuid = UUID.randomUUID();

            uuidString = uuid.toString();
            stack.set(BackpacksDataComponentTypes.UUID_TYPE, uuidString);
            return uuid;
        }
        return UUID.fromString(uuidString);
    }

    public static BackpackInstance getInstance(UUID uuid, int slots) {
        if (manager.storedInstances.containsKey(uuid)) {
            BackpackInstance backpack = manager.storedInstances.get(uuid);
            BackpackInventory backpackInventory = backpack.inventory;
            if (backpackInventory.size() != slots) {
                BackpackInventory newInventory = new BackpackInventory(slots);
                newInventory.setInventoryDirectly(backpackInventory.getHeldStacks());

                backpack.inventory = newInventory;
                manager.saveBackpack(backpack);
            }

            if (!backpackInventory.getHeldStacks().isEmpty())
                return backpack;
        }
        return new BackpackInstance(uuid, new BackpackInventory(slots));
    }

    public static BackpackInventory getInventory(UUID uuid, int slots) {
        if (manager.storedInstances.containsKey(uuid)) {
            BackpackInstance backpack = manager.storedInstances.get(uuid);
            BackpackInventory backpackInventory = backpack.inventory;
            if (backpackInventory.size() != slots) {
                BackpackInventory newInventory = new BackpackInventory(slots);
                newInventory.setInventoryDirectly(backpackInventory.getHeldStacks());

                backpackInventory = newInventory;
                manager.saveBackpack(uuid, backpackInventory);
            }

            if (!backpackInventory.getHeldStacks().isEmpty())
                return backpackInventory;
        }

        return new BackpackInventory(slots);
    }

    public void saveBackpack(BackpackInstance instance) {
        if (instance.uuid != null && instance.inventory != null) {
            this.storedInstances.put(instance.uuid, instance);
        }
    }

    public void saveBackpack(UUID uuid, BackpackInventory backpackInventory) {
        saveBackpack(new BackpackInstance(uuid, backpackInventory));
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

    public void load(MinecraftServer server) {
        StateSaverAndLoader serverState = StateSaverAndLoader.getServerState(server);
        this.globalInventory = serverState.globalInventory;
        serverState.storedInventories.forEach(this::saveBackpack);
    }

    public Set<BackpackInstance> save() {
        Set<BackpackInstance> backpackInstances = new HashSet<>();
        this.storedInstances.forEach((key, value) -> backpackInstances.add(value));
        return backpackInstances;
    }

    public void save(MinecraftServer server) {
        StateSaverAndLoader serverState = StateSaverAndLoader.getServerState(server);
        serverState.globalInventory = this.globalInventory;
        serverState.storedInventories = this.save();
    }

    public static void setGlobalInventory(DefaultedList<ItemStack> stacks) {
        manager.globalInventory.setInventoryDirectly(stacks);
    }

    public static BackpackInventory getGlobalInventory() {
        return manager.globalInventory;
    }
}
