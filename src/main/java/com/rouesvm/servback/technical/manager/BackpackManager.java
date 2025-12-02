package com.rouesvm.servback.technical.manager;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.technical.cosmetic.CosmeticManager;
import com.rouesvm.servback.technical.data.BackpackData;
import com.rouesvm.servback.technical.data.BackpackDataBackups;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.data.alternative.BackpackListData;
import com.rouesvm.servback.technical.data.alternative.BackpackState;
import com.rouesvm.servback.technical.data.alternative.BackpackStateUpper;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BackpackManager {
    private static BackpackManager instance;

    private final Map<UUID, BackpackInstance> storedInstances = new Object2ObjectOpenHashMap<>();
    private final Set<UUID> discoveredBackpackUUIDs = new ObjectOpenHashSet<>();

    private boolean loaded = false;
    private DATA_TYPE data_type = DATA_TYPE.NONE;

    private MinecraftServer server;

    public static void initialize(MinecraftServer server) {
        if (ServerBackpacks.hasTrinketLoaded) CosmeticManager.initialize();
        instance = new BackpackManager();
        instance.server = server;

        load(server);

        ServerBackpacks.LOGGER.info("Loading Server Backpack's data on server starting...");

        BackpackDataBackups.createBackupDirs(server);
    }

    public static void destroy(MinecraftServer ignoredServer) {
        CosmeticManager.destroy();

        if (instance != null) {
            ServerBackpacks.LOGGER.info("Saving Server Backpacks's data!");
            createBackupAndSave();
            instance = null;
        }
    }

    public void loadIntoStoredInstances(Set<BackpackInstance> instances) {
        instances.forEach(backpackInstance -> storedInstances.put(backpackInstance.uuid(), backpackInstance));
    }

    public void loadDiscoveredBackpackUUIDs(Set<UUID> uuids) {
        discoveredBackpackUUIDs.addAll(uuids);
    }

    public static void createBackupAndSave() {
        saveData();
        BackpackDataBackups.createBackup(server());
    }

    public static void createSingularBackupAndSave(BackpackInstance instance) {
        saveData(instance);
        BackpackDataBackups.createSingularBackup(server(), instance);
    }

    public static void loadFallback(MinecraftServer server, boolean loadState) {
        if (BackpackListData.loadData(server, instance.loaded)) {
            instance.loaded = true;
            instance.data_type = DATA_TYPE.LIST_FILE_DATA;
        }

        if (loadState) {
            if (BackpackState.loadData(server, instance.loaded)) {
                instance.loaded = true;
                instance.data_type = DATA_TYPE.MINECRAFT_STATE;
            }
        }

        if (BackpackStateUpper.loadData(server, instance.loaded)) {
            instance.loaded = true;
            instance.data_type = DATA_TYPE.OLD_MINECRAFT_STATE;
        }
    }

    public static void loadFileData(MinecraftServer server) {
        if (BackpackData.loadData(server, instance.loaded)) {
            instance.loaded = true;
            instance.data_type = BackpackManager.DATA_TYPE.FILE_DATA;
        }
    }

    public static void load(MinecraftServer server) {
        loadFileData(server);
        if (!instance.loaded) {
            loadFallback(server, false);
        }

        if (!instance.loaded)
            ServerBackpacks.LOGGER.error("Failed to load Server Backpack's data.");
        else ServerBackpacks.LOGGER.info("Loaded {} format as Server Backpack's data.", instance.data_type.toString());
    }

    public static void loadOnServerStarted(MinecraftServer server) {
        if (!instance.loaded) {
            ServerBackpacks.LOGGER.info("Running Server Backpack's data old format convertor...");
            loadFallback(server, true);
        }
    }

    public static void saveData() {
        BackpackData.replaceStoredInventories(instance.toBackpackInstances());
        BackpackData.save(server());
    }

    public static void saveData(BackpackInstance instance) {
        if (!server().isStopping() && !server().isSaving()) {
            BackpackData.replaceStoredInventory(instance);
            BackpackData.saveSingle(server(), instance);
        }
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
        if (instance.uuid() == null || instance.inventory() == null) {
            return Optional.empty();
        }

        UUID uuid = instance.uuid();
        BackpackManager.instance.discoveredBackpackUUIDs.add(uuid);
        BackpackManager.instance.storedInstances.putIfAbsent(uuid, instance);

        return getInstance(uuid);
    }

    public static void addBackpack(UUID uuid, BackpackInventory inventory) {
        addBackpack(new BackpackInstance(uuid, inventory));
    }

    //
    // GET (INSTANCE, INVENTORY)
    //

    public static Optional<BackpackInstance> getInstance(UUID uuid) {
        BackpackInstance backpackInstance = instance.storedInstances.get(uuid);
        if (backpackInstance != null) {
            return Optional.of(backpackInstance);
        }

        if (!hasUUID(uuid)) {
            return Optional.empty();
        }

        Optional<BackpackInstance> loaded = BackpackData.getOrLoadBackpack(uuid, server());
        if (loaded.isPresent()) {
            instance.storedInstances.put(uuid, loaded.get());
            return loaded;
        }

        return Optional.empty();
    }

    public static Optional<BackpackInstance> getInstance(UUID uuid, int slots) {
        if (instance == null) return Optional.empty();

        if (hasBackpack(uuid))
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

    public Set<BackpackInstance> toBackpackInstances() {
        return new HashSet<>(this.storedInstances.values());
    }

    public Set<UUID> discoveredBackpackUUIDs() {
        return new HashSet<>(discoveredBackpackUUIDs);
    }

    public static boolean hasUUID(UUID uuid) {
        return uuid != null && instance.discoveredBackpackUUIDs.contains(uuid);
    }

    public static boolean hasBackpack(UUID uuid) {
        Optional<BackpackInstance> backpackInstance = getInstance(uuid);
        return uuid != null && backpackInstance.isPresent();
    }

    public enum DATA_TYPE {
        NONE("No data loaded"),
        MINECRAFT_STATE("Minecraft state"),
        OLD_MINECRAFT_STATE("Old Minecraft state"),
        FILE_DATA("File-based data"),
        LIST_FILE_DATA("List file-based data");

        private final String description;

        DATA_TYPE(String desc) { this.description = desc; }

        @Override
        public String toString() { return description; }
    }
}
