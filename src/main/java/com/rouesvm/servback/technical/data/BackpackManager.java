package com.rouesvm.servback.technical.data;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.content.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.technical.cosmetic.CosmeticManager;
import com.rouesvm.servback.technical.data.state.BackpackDataFixer;
import com.rouesvm.servback.technical.data.state.BackpackDataSaver;
import com.rouesvm.servback.technical.data.state.BackpackState;
import com.rouesvm.servback.technical.data.state.GlobalBackpackState;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
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
    // SAVING AND ADDING BACKPACKS
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
    // UUID
    //

    public static UUID getStackUUID(ItemStack stack) {
        UUID uuid = stack.get(BackpackDataComponentTypes.BACKPACK_UUID_TYPE);
        String uuidString = stack.get(BackpackDataComponentTypes.UUID_TYPE);

        if (uuidString != null) {
            uuid = UUID.fromString(uuidString);
            stack.set(BackpackDataComponentTypes.BACKPACK_UUID_TYPE, uuid);
            stack.remove(BackpackDataComponentTypes.UUID_TYPE);
        } else if (uuid == null) uuid = createNewUUID(stack);

        return uuid;
    }

    public static UUID createNewUUID(ItemStack stack) {
        UUID stackUUID = stack.get(BackpackDataComponentTypes.BACKPACK_UUID_TYPE);
        if (stackUUID == null) {
            UUID uuid = generateUniqueUUID();
            stack.set(BackpackDataComponentTypes.BACKPACK_UUID_TYPE, uuid);
            return uuid;
        } else return stackUUID;
    }

    // It's near impossible to generate an uuid that is the same, but I'm just going to regenerate just in case.
    public static UUID generateUniqueUUID() {
        UUID uuid = UUID.randomUUID();
        if (instance != null && instance.hasBackpack(uuid)) {
            uuid = UUID.randomUUID();
        }
        return uuid;
    }

    //
    // GET (INSTANCE, INVENTORY)
    //

    public static Optional<BackpackInstance> getInstance(UUID uuid) {
        if (instance.hasBackpack(uuid))
            return Optional.ofNullable(instance.storedInstances.get(uuid));
        else return Optional.empty();
    }

    public static Optional<BackpackInstance> getInstance(UUID uuid, int slots) {
        if (instance == null) return Optional.empty();
        if (instance.hasBackpack(uuid)) {
            resizeInventory(uuid, slots);
        } else addBackpack(uuid, new BackpackInventory(slots));

        return getInstance(uuid);
    }

    public static @Nullable BackpackInventory getInventory(UUID uuid) {
        Optional<BackpackInstance> instance = getInstance(uuid);
        return instance.map(BackpackInstance::inventory).orElse(null);
    }

    //
    // GENERAL
    //

    public static void setGlobalInventory(DefaultedList<ItemStack> stacks) {
        instance.globalInventory.setInventoryDirectly(stacks);
    }

    public static BackpackInventory getGlobalInventory() {
        return instance.globalInventory;
    }

    public static void resizeInventory(UUID uuid, int newSize) {
        if (uuid != null) getInstance(uuid).ifPresent(
                backpackInstance ->
                        backpackInstance.inventory.resize(newSize));
    }

    public boolean hasBackpack(UUID uuid) {
        return uuid != null && this.storedInstances.containsKey(uuid);
    }
}
