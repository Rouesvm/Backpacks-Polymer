package com.rouesvm.servback.technical.manager;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.technical.cosmetic.CosmeticManager;
import com.rouesvm.servback.technical.data.BackpackData;
import com.rouesvm.servback.technical.data.BackpackDataFixer;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.data.state.BackpackState;
import com.rouesvm.servback.technical.data.state.GlobalBackpackState;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BackpackManager {
    private static BackpackManager instance;

    private final BackpackInventory globalInventory = new BackpackInventory(9 * 3);
    private final Map<UUID, BackpackInstance> storedInstances = new HashMap<>();

    private boolean loaded = false;
    private DATA_TYPE data_type = null;

    private MinecraftServer server;

    public static void setup(MinecraftServer server) {
        if (ServerBackpacks.hasTrinketLoaded) CosmeticManager.setup();
        instance = new BackpackManager();
        instance.server = server;

        load(server);

        ServerBackpacks.LOGGER.info("Loading Server Backpack's data on server starting...");
        BackpackData.createBackupDir(server);
    }

    public static void destroy(MinecraftServer ignoredServer) {
        CosmeticManager.destroy();

        if (instance != null) {
            ServerBackpacks.LOGGER.info("Saving Server Backpacks's data!");

            saveData();
            createBackup();

            instance = null;
        }
    }

    public void loadIntoStoredInstances(Set<BackpackInstance> instances) {
        instances.forEach(backpackInstance -> storedInstances.put(backpackInstance.uuid(), backpackInstance));
    }

    public static void createBackup() {
        saveData();
        BackpackData.createBackup(server());
    }

    public static void loadFallback(MinecraftServer server, boolean loadState) {
        if (loadState) {
            BackpackState state = BackpackState.getServerState(server);

            if (!instance.loaded && state != null) {
                instance.data_type = DATA_TYPE.MINECRAFT_STATE;
                instance.loaded = BackpackState.loadData(state);
            }
        }

        if (!instance.loaded) {
            instance.loaded = BackpackDataFixer.isDataPresent(server);
            if (instance.loaded) instance.data_type = DATA_TYPE.OLD_MINECRAFT_STATE;
        }

        if (!instance.loaded)
            ServerBackpacks.LOGGER.error("Failed to load Server Backpack's data.");
        else ServerBackpacks.LOGGER.info("Loaded {} format as Server Backpack's data.", instance.data_type.toString());
    }

    public static void loadFileData(MinecraftServer server) {
        if (!instance.loaded) {
            instance.loaded = BackpackData.loadData(server);

            Set<BackpackInstance> dataInstances = BackpackData.getBackpackInstances();
            if (dataInstances != null && !dataInstances.isEmpty()) {
                instance.data_type = DATA_TYPE.FILE_DATA;
                instance.loadIntoStoredInstances(dataInstances);
            }
        }
    }

    public static void load(MinecraftServer server) {
        loadFileData(server);
        loadFallback(server, false);
    }

    public static void saveData() {
        BackpackData.setStoredInventories(instance.backpackInstances());
        BackpackData.save(server());

        GlobalBackpackState globalBackpackState = GlobalBackpackState.getServerState(server());
        globalBackpackState.globalInventory = instance.globalInventory;
    }

    public static void loadOnServerStarted(MinecraftServer server) {
        if (!instance.loaded) {
            ServerBackpacks.LOGGER.info("Running Server Backpack's data old format convertor...");
            loadFallback(server, true);
        }

        GlobalBackpackState globalBackpackState = GlobalBackpackState.getServerState(server);
        instance.globalInventory.setInventoryDirectly(globalBackpackState.globalInventory.heldStacks());
    }

    //
    // SAVING AND ADDING BACKPACKS
    //

    public static void saveToBackpackInventory(BackpackInstance instance) {
        if (instance == null) return;
        if (instance.uuid() != null && instance.inventory() != null) {
            instance.setLastAccessed();
            Optional<BackpackInstance> saved = addBackpack(instance);
            saved.ifPresent(savedInstance -> savedInstance.copyToInventory(instance.inventory()));
        }
    }

    public static Optional<BackpackInstance> addBackpack(BackpackInstance instance) {
        UUID uuid = instance.uuid();
        if (uuid == null || instance.inventory() == null) return Optional.empty();
        BackpackManager.instance.storedInstances().putIfAbsent(uuid, instance);
        return getInstance(uuid);
    }

    public static void addBackpack(UUID uuid, BackpackInventory inventory) {
        addBackpack(new BackpackInstance(uuid, inventory));
    }

    //
    // GET (INSTANCE, INVENTORY)
    //

    public static Optional<BackpackInstance> getInstance(UUID uuid) {
        BackpackInstance backpackInstance = instance.storedInstances().get(uuid);
        return Optional.ofNullable(backpackInstance);
    }

    public static Optional<BackpackInstance> getInstance(UUID uuid, int slots) {
        if (instance == null) return Optional.empty();

        if (instance.hasBackpack(uuid))
            BackpackInventory.resizeInventory(uuid, slots);
        else addBackpack(uuid, new BackpackInventory(slots));

        return getInstance(uuid);
    }

    public static @Nullable BackpackInventory getInventory(UUID uuid) {
        return getInstance(uuid)
                .map(BackpackInstance::inventory)
                .orElse(null);
    }

    //

    public static BackpackManager instance() {
        return instance;
    }

    //
    // GENERAL
    //

    public static MinecraftServer server() {
        return instance.server;
    }

    public Set<BackpackInstance> backpackInstances() {
        return new HashSet<>(this.storedInstances.values());
    }

    public Map<UUID, BackpackInstance> storedInstances() {
        return storedInstances;
    }

    public static void setGlobalInventory(DefaultedList<ItemStack> stacks) {
        instance.globalInventory.setInventoryDirectly(stacks);
    }

    public static BackpackInventory globalInventory() {
        return instance.globalInventory;
    }

    public boolean hasBackpack(UUID uuid) {
        Optional<BackpackInstance> backpackInstance = getInstance(uuid);
        return uuid != null && backpackInstance.isPresent();
    }

    private enum DATA_TYPE {
        FILE_DATA,
        MINECRAFT_STATE,
        OLD_MINECRAFT_STATE
    }
}
