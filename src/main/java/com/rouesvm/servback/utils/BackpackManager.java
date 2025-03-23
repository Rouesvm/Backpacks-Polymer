package com.rouesvm.servback.utils;

import com.rouesvm.servback.registry.BackpacksDataComponentTypes;
import com.rouesvm.servback.state.BackpackDataFixer;
import com.rouesvm.servback.state.BackpackState;
import com.rouesvm.servback.state.GlobalBackpackState;
import com.rouesvm.servback.ui.inventory.BackpackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.collection.DefaultedList;

import java.util.*;

public class BackpackManager {
    private static BackpackManager manager = null;
    
    public BackpackInventory globalInventory = new BackpackInventory(9 * 3);
    public Map<UUID, BackpackInstance> storedInstances = new HashMap<>();

    private BackpackManager() {}

    public static BackpackManager getManager() {
        return manager;
    }

    public static void setup(MinecraftServer server) {
        manager = new BackpackManager();
        manager.load(server);
        BackpackDataFixer.onWorldLoading(server);
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
            UUID uuid = generateUniqueUUID();
            stack.set(BackpacksDataComponentTypes.UUID_TYPE, uuid.toString());
            return uuid;
        }
        return UUID.fromString(uuidString);
    }

    private static UUID generateUniqueUUID() {
        UUID uuid = UUID.randomUUID();
        if (manager != null) {
            while (manager.storedInstances.containsKey(uuid)) {
                uuid = UUID.randomUUID();
            }
        }
        return uuid;
    }

    public static BackpackInstance getInstance(UUID uuid, int slots) {
        BackpackManager manager = getManager();
        if (manager.storedInstances.containsKey(uuid)) {
            BackpackInstance backpack = manager.storedInstances.get(uuid);
            BackpackInventory inventory = backpack.getInventory();
            if (inventory.size() != slots) {
                inventory = resizeInventory(inventory, slots);
                backpack.setInventory(inventory);
                manager.saveBackpack(backpack);
            }
            return backpack;
        }
        return new BackpackInstance(uuid, new BackpackInventory(slots));
    }

    public static BackpackInventory getInventory(UUID uuid, int slots) {
        BackpackInstance backpack = getInstance(uuid, slots);
        return backpack.getInventory();
    }

    public static BackpackInventory resizeInventory(BackpackInventory inventory, int newSize) {
        BackpackInventory newInventory = new BackpackInventory(newSize);
        inventory.copyTo(newInventory);
        return newInventory;
    }

    public static void resizeInventory(UUID uuid, BackpackInventory inventory, int newSize) {
        BackpackInventory newInventory = new BackpackInventory(newSize);
        inventory.copyTo(newInventory);
        manager.saveBackpack(uuid, inventory);
    }

    public void saveBackpack(BackpackInstance instance) {
        if (instance.getUuid() != null && instance.getInventory() != null) {
            this.storedInstances.put(instance.getUuid(), instance);
        }
    }

    public void saveBackpack(UUID uuid, BackpackInventory backpackInventory) {
        saveBackpack(new BackpackInstance(uuid, backpackInventory));
    }

    public void load(Set<BackpackInstance> instances) {
        instances.forEach(this::saveBackpack);
    }

    public void load(MinecraftServer server) {
        BackpackState backpackState = BackpackState.getServerState(server);
        this.load(backpackState.getBackpackInstances());

        GlobalBackpackState globalBackpackState = GlobalBackpackState.getServerState(server);
        this.globalInventory = globalBackpackState.globalInventory;
    }

    public Set<BackpackInstance> save() {
        Set<BackpackInstance> backpackInstances = new HashSet<>();
        this.storedInstances.forEach((key, value) -> backpackInstances.add(value));
        return backpackInstances;
    }

    public void save(MinecraftServer server) {
        BackpackState backpackState = BackpackState.getServerState(server);
        backpackState.setStoredInventories(this.save());

        GlobalBackpackState globalBackpackState = GlobalBackpackState.getServerState(server);
        globalBackpackState.globalInventory = this.globalInventory;
    }

    public void setGlobalInventory(DefaultedList<ItemStack> stacks) {
        this.globalInventory.setInventoryDirectly(stacks);
    }
}
