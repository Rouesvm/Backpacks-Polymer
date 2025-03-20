package com.rouesvm.servback.utils;

import com.rouesvm.servback.components.BackpacksDataComponentTypes;
import com.rouesvm.servback.state.BackpackState;
import com.rouesvm.servback.state.GlobalBackpackState;
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
            BackpackInventory backpackInventory = backpack.backpackInventory;
            if (backpackInventory.size() != slots) {
                BackpackInventory newInventory = new BackpackInventory(slots);
                newInventory.setInventoryDirectly(backpackInventory.getHeldStacks());

                backpack.backpackInventory = newInventory;
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
            BackpackInventory backpackInventory = backpack.backpackInventory;
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
        if (instance.uuid != null && instance.backpackInventory != null) {
            manager.storedInstances.put(instance.uuid, instance);
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

    public void load(MinecraftServer server) {
        BackpackState backpackState = BackpackState.getServerState(server);
        GlobalBackpackState globalBackpackState = GlobalBackpackState.getServerState(server);
        this.globalInventory = globalBackpackState.globalInventory;
        this.load(backpackState.storedInventories);
    }

    public Set<BackpackInstance> save() {
        Set<BackpackInstance> backpackInstances = new HashSet<>();
        this.storedInstances.forEach((key, value) -> backpackInstances.add(value));
        return backpackInstances;
    }

    public void save(MinecraftServer server) {
        BackpackState backpackState = BackpackState.getServerState(server);
        GlobalBackpackState globalBackpackState = GlobalBackpackState.getServerState(server);
        globalBackpackState.globalInventory = this.globalInventory;
        backpackState.storedInventories = this.save();
    }

    public void setGlobalInventory(DefaultedList<ItemStack> stacks) {
        this.globalInventory.setInventoryDirectly(stacks);
    }
}
