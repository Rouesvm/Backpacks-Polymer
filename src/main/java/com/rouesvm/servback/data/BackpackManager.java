package com.rouesvm.servback.data;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.content.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.data.state.BackpackDataFixer;
import com.rouesvm.servback.data.state.BackpackDataSaver;
import com.rouesvm.servback.data.state.BackpackState;
import com.rouesvm.servback.data.state.GlobalBackpackState;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import com.rouesvm.servback.technical.cosmetic.CosmeticManager;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BackpackManager {
    public static BackpackManager instance;

    public BackpackInventory globalInventory = new BackpackInventory(9 * 3);
    public Map<UUID, BackpackInstance> storedInstances = new HashMap<>();

    private static boolean loaded = false;

    public static void setup(MinecraftServer server) {
        if (ServerBackpacks.hasTrinketLoaded) CosmeticManager.setup();
        instance = new BackpackManager();
        load(server);
    }

    public static void destroy(MinecraftServer server) {
        CosmeticManager.destroy();

        if (instance != null) {
            ServerBackpacks.LOGGER.info("Saving Server Backpacks's data!");

            save(server);
            instance = null;
        }
    }

    public Set<BackpackInstance> getBackpackInstances() {
        return new HashSet<>(this.storedInstances.values());
    }

    public static void save(MinecraftServer server) {
        BackpackDataSaver.setStoredInventories(instance.getBackpackInstances());
        BackpackDataSaver.save(server);

        GlobalBackpackState globalBackpackState = GlobalBackpackState.getServerState(server);
        globalBackpackState.globalInventory = instance.globalInventory;
    }

    public void load(Set<BackpackInstance> instances) {
        instances.forEach(backpackInstance -> storedInstances.put(backpackInstance.getUuid(), backpackInstance));
    }

    public static void load(MinecraftServer server) {
        BackpackDataFixer.onWorldLoading(server);

        BackpackState backpackState = BackpackState.getServerState(server);

        if (!loaded) {
            BackpackDataSaver.onServerStarting(server);

            Set<BackpackInstance> dataInstances = BackpackDataSaver.getBackpackInstances();
            if (dataInstances != null && !dataInstances.isEmpty()) {
                instance.load(dataInstances);
                loaded = true;
            }
        }

        if (!loaded && backpackState != null) {
            Set<BackpackInstance> stateInstances = backpackState.getBackpackInstances();
            if (stateInstances != null && !stateInstances.isEmpty()) {
                instance.load(stateInstances);

                BackpackDataSaver.setStoredInventories(stateInstances);
                backpackState.clearBackpackInstances();
                backpackState.markDirty();

                loaded = true;
            }
        }
    }

    public static void loadOnServerStarted(MinecraftServer server) {
        load(server);

        GlobalBackpackState globalBackpackState = GlobalBackpackState.getServerState(server);
        instance.globalInventory = globalBackpackState.globalInventory;
    }

    //

    public static void saveBackpack(BackpackInstance instance) {
        if (instance.getUuid() != null && instance.inventory() != null) {
            BackpackInstance accessedInstance = addBackpack(instance);
            if (accessedInstance != null) accessedInstance.saveToInventory(instance.inventory());
        }
    }

    public static void saveBackpack(UUID uuid, BackpackInventory inventory) {
        saveBackpack(new BackpackInstance(uuid, inventory));
    }

    public static BackpackInstance addBackpack(BackpackInstance backpackInstance) {
        if (backpackInstance.getUuid() != null && backpackInstance.inventory() != null) {
            instance.storedInstances.putIfAbsent(backpackInstance.getUuid(), backpackInstance);
        }

        return instance.storedInstances.get(backpackInstance.getUuid());
    }

    public static BackpackInstance addBackpack(UUID uuid, BackpackInventory inventory) {
        return addBackpack(new BackpackInstance(uuid, inventory));
    }

    //

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
        if (instance != null) {
            if (instance.hasBackpack(uuid)) {
                uuid = UUID.randomUUID();
            }
        }
        return uuid;
    }

    //

    public static BackpackInstance getInstance(UUID uuid) {
        if (instance.hasBackpack(uuid))
            return instance.storedInstances.get(uuid);
        else return null;
    }

    public static @Nullable BackpackInstance getInstance(UUID uuid, int slots) {
        if (instance == null) return null;
        if (instance.hasBackpack(uuid)) {
            BackpackInstance backpack = getInstance(uuid);
            BackpackInventory inventory = backpack.inventory;
            if (inventory.size() != slots) inventory.resize(slots);
            return backpack;
        }

        return addBackpack(uuid, new BackpackInventory(slots));
    }

    public static @Nullable BackpackInventory getInventory(UUID uuid, int slots) {
        BackpackInstance instance = getInstance(uuid, slots);
        if (instance != null)
            return instance.inventory();
        else return null;
    }

    public static @Nullable BackpackInventory getInventory(UUID uuid) {
        BackpackInstance instance = getInstance(uuid);
        if (instance != null)
            return instance.inventory();
        else return null;
    }

    //

    public static void resizeInventory(UUID uuid, BackpackInventory inventory, int newSize) {
        if (uuid != null && inventory != null) {
            BackpackInstance accessedInstance = getInstance(uuid);
            if (accessedInstance != null) accessedInstance.inventory.resize(newSize);
        }
    }

    public void setGlobalInventory(DefaultedList<ItemStack> stacks) {
        this.globalInventory.setInventoryDirectly(stacks);
    }

    public boolean hasBackpack(UUID uuid) {
        return uuid != null && this.storedInstances.containsKey(uuid);
    }
}
