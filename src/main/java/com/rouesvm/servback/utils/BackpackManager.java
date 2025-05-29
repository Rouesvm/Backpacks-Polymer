package com.rouesvm.servback.utils;

import com.rouesvm.servback.Main;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.state.BackpackDataFixer;
import com.rouesvm.servback.state.BackpackDataSaver;
import com.rouesvm.servback.state.BackpackState;
import com.rouesvm.servback.state.GlobalBackpackState;
import com.rouesvm.servback.ui.inventory.BackpackInventory;
import com.rouesvm.servback.utils.cosmetic.CosmeticManager;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BackpackManager {
    private static BackpackManager manager = null;

    public static boolean loaded = false;

    public BackpackInventory globalInventory;
    public Map<UUID, BackpackInstance> storedInstances = new HashMap<>();

    private BackpackManager() {
        this.globalInventory = new BackpackInventory(9 * 3);
    }

    public static BackpackManager getManager() {
        return manager;
    }

    public static void setup(MinecraftServer server) {
        if (Main.hasTrinketLoaded) CosmeticManager.setup();

        manager = new BackpackManager();
        manager.load(server);
    }

    public static void destroy(MinecraftServer server) {
        CosmeticManager.destroy();

        if (manager != null) {
            manager.save(server);
            manager = null;
        }
    }

    public void load(Set<BackpackInstance> instances) {
        instances.forEach(this::saveBackpack);
    }

    public void load(MinecraftServer server) {
        BackpackDataFixer.onWorldLoading(server);

        BackpackState backpackState = BackpackState.getServerState(server);

        if (!loaded) {
            BackpackDataSaver.onServerStarting(server);

            Set<BackpackInstance> dataInstances = BackpackDataSaver.getBackpackInstances();
            if (dataInstances != null && !dataInstances.isEmpty()) {
                this.load(dataInstances);
                loaded = true;
            }
        }

        if (!loaded && backpackState != null) {
            Set<BackpackInstance> stateInstances = backpackState.getBackpackInstances();
            if (stateInstances != null && !stateInstances.isEmpty()) {
                this.load(stateInstances);

                BackpackDataSaver.setStoredInventories(stateInstances);
                backpackState.clearBackpackInstances();
                backpackState.markDirty();

                loaded = true;
            }
        }
    }

    public void loadOnServerStarted(MinecraftServer server) {
        GlobalBackpackState globalBackpackState = GlobalBackpackState.getServerState(server);
        this.globalInventory = globalBackpackState.globalInventory;
    }

    public Set<BackpackInstance> save() {
        return new HashSet<>(this.storedInstances.values());
    }

    public void save(MinecraftServer server) {
        BackpackDataSaver.setStoredInventories(this.save());
        BackpackDataSaver.save(server);

        GlobalBackpackState globalBackpackState = GlobalBackpackState.getServerState(server);
        globalBackpackState.globalInventory = this.globalInventory;
    }

    public static UUID getStackUUID(ItemStack stack) {
        String uuidString = stack.get(BackpackDataComponentTypes.UUID_TYPE);
        if (uuidString == null)
            uuidString = String.valueOf(createNewUUID(stack));
        return UUID.fromString(uuidString);
    }

    public static UUID createNewUUID(ItemStack stack) {
        String uuidString = stack.get(BackpackDataComponentTypes.UUID_TYPE);
        if (uuidString == null) {
            UUID uuid = generateUniqueUUID();
            stack.set(BackpackDataComponentTypes.UUID_TYPE, uuid.toString());
            return uuid;
        } else return UUID.fromString(uuidString);
    }

    public static UUID generateUniqueUUID() {
        UUID uuid = UUID.randomUUID();
        if (manager != null) {
            int attempts = 0;
            if (manager.hasBackpack(uuid)) {
                uuid = UUID.randomUUID();
            }
        }
        return uuid;
    }

    public static BackpackInstance getInstance(UUID uuid) {
        BackpackManager manager = getManager();
        if (manager.hasBackpack(uuid))
            return manager.storedInstances.get(uuid);
        else return null;
    }

    public static @Nullable BackpackInstance getInstance(UUID uuid, int slots) {
        BackpackManager manager = getManager();
        if (manager == null) return null;
        if (manager.hasBackpack(uuid)) {
            BackpackInstance backpack = manager.storedInstances.get(uuid);
            BackpackInventory inventory = backpack.getInventory();
            if (inventory.size() != slots) {
                backpack.setInventory(resizeInventory(inventory, slots));
                manager.saveNewInventoryBackpack(uuid, inventory);
            }
            return backpack;
        } else return new BackpackInstance(uuid, new BackpackInventory(slots));
    }

    public static @Nullable BackpackInventory getInventory(UUID uuid, int slots) {
        BackpackInstance instance = getInstance(uuid, slots);
        if (instance != null)
            return instance.getInventory();
        else return null;
    }

    public static @Nullable BackpackInventory getInventory(UUID uuid) {
        BackpackInstance instance = getInstance(uuid);
        if (instance != null)
            return instance.getInventory();
        else return null;
    }

    public static BackpackInventory resizeInventory(BackpackInventory inventory, int newSize) {
        BackpackInventory newInventory = new BackpackInventory(newSize);
        inventory.copyTo(newInventory);
        return newInventory;
    }

    public static void resizeInventory(UUID uuid, BackpackInventory inventory, int newSize) {
        if (uuid != null && inventory != null) {
            BackpackInstance accessedInstance = getInstance(uuid, newSize);
            if (accessedInstance != null) manager.saveNewInventoryBackpack(uuid, resizeInventory(inventory, newSize));
        }
    }

    public void saveNewInventoryBackpack(UUID uuid, BackpackInventory inventory) {
        if (uuid != null && inventory != null) {
            BackpackInstance accessedInstance = getInstance(uuid);
            if (accessedInstance != null) accessedInstance.setInventory(inventory);
        }
    }

    public void saveBackpack(BackpackInstance instance) {
        if (instance != null) saveBackpack(instance.getUuid(), instance.getInventory());
    }

    public void saveBackpack(UUID uuid, BackpackInventory inventory) {
        if (uuid != null && inventory != null) {
            BackpackInstance accessedInstance = this.storedInstances.putIfAbsent(uuid, new BackpackInstance(uuid, inventory));
            if (accessedInstance != null) accessedInstance.saveToInventory(inventory);
        }
    }

    public void setGlobalInventory(DefaultedList<ItemStack> stacks) {
        this.globalInventory.setInventoryDirectly(stacks);
    }

    public boolean hasBackpack(UUID uuid) {
        return uuid != null && this.storedInstances.containsKey(uuid);
    }
}
