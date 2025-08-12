package com.rouesvm.servback.technical.data;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.technical.config.Configuration;
import com.rouesvm.servback.technical.cosmetic.CosmeticManager;
import com.rouesvm.servback.technical.data.sql.BackpackSQL;
import com.rouesvm.servback.technical.data.state.BackpackState;
import com.rouesvm.servback.technical.data.state.GlobalBackpackState;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BackpackManager {
    private static BackpackSQL sqlStorage = null;
    private static BackpackManager instance;

    public final BackpackInventory globalInventory = new BackpackInventory(9 * 3);
    public final Map<UUID, BackpackInstance> storedInstances = new HashMap<>();

    private boolean loaded = false;
    private DATA_TYPE data_type = null;

    private MinecraftServer server;

    public static void setup(MinecraftServer server) {
        if (ServerBackpacks.hasTrinketLoaded) CosmeticManager.setup();
        instance = new BackpackManager();
        instance.server = server;

        if (Configuration.instance().enable_sql_data) {
            sqlStorage = new BackpackSQL(server);
            if (!sqlStorage.createConnection()) {
                ServerBackpacks.LOGGER.error("Failed to initialize SQL connection; falling back to default storage.");
                sqlStorage = null;
            } else sqlStorage.createTableIfNotExists();
        }

        load(server);

        ServerBackpacks.LOGGER.info("Loading Server Backpack's data on server starting...");
        BackpackData.createBackupDir(server);
    }

    public static void destroy(MinecraftServer server) {
        CosmeticManager.destroy();

        if (instance != null) {
            ServerBackpacks.LOGGER.info("Saving Server Backpacks's data!");

            saveData();
            createBackup();

            if (sqlStorage != null) {
                sqlStorage.close();
                sqlStorage = null;
            }

            instance = null;
        }
    }

    public Set<BackpackInstance> getBackpackInstances() {
        return new HashSet<>(this.storedInstances.values());
    }

    public static void createBackup() {
        saveData();
        BackpackData.createBackup(getServer());
    }

    public void loadIntoStoredInstances(Set<BackpackInstance> instances) {
        instances.forEach(backpackInstance -> storedInstances.put(backpackInstance.getUuid(), backpackInstance));
    }

    public static void load(MinecraftServer server) {
        if (Configuration.instance().enable_sql_data && sqlStorage != null) {
            try {
                Set<BackpackInstance> sqlData = sqlStorage.loadInventories();
                if (sqlData != null && !sqlData.isEmpty()) {
                    instance().data_type = DATA_TYPE.SQL;

                    instance.loadIntoStoredInstances(sqlData);
                    instance.loaded = true;
                }
            } catch (Exception e) {
                ServerBackpacks.LOGGER.error("Failed to load backpacks from SQL, falling back.", e);
            }
        }

        BackpackState state = BackpackState.getServerState(server);

        if (!instance.loaded) {
            instance.loaded = BackpackData.loadData(server);

            Set<BackpackInstance> dataInstances = BackpackData.getBackpackInstances();
            if (dataInstances != null && !dataInstances.isEmpty()) {
                instance.data_type = DATA_TYPE.FILE_DATA;
                instance.loadIntoStoredInstances(dataInstances);
            }
        }

        if (!instance.loaded && state != null) {
            instance.data_type = DATA_TYPE.MINECRAFT_STATE;
            instance.loaded = BackpackState.loadData(state);
        }

        if (!instance.loaded) {
            instance.loaded = BackpackDataFixer.isDataPresent(server);
            if (instance.loaded) instance.data_type = DATA_TYPE.OLD_MINECRAFT_STATE;
        }

        if (!instance.loaded)
            ServerBackpacks.LOGGER.error("Failed to load Server Backpack's data.");
        else ServerBackpacks.LOGGER.info("Loaded {} format as Server Backpack's data.", instance.data_type.toString());
    }

    public static void saveData() {
        if (Configuration.instance().enable_sql_data && sqlStorage != null) {
            try {
                if (!sqlStorage.saveInventories()) ServerBackpacks.LOGGER.warn("Failed to save backpacks to SQL database.");
            } catch (Exception e) {
                ServerBackpacks.LOGGER.error("Error saving backpacks to SQL database.", e);
            }
            return;
        }

        BackpackData.setStoredInventories(instance.getBackpackInstances());
        BackpackData.save(getServer());

        GlobalBackpackState globalBackpackState = GlobalBackpackState.getServerState(getServer());
        globalBackpackState.globalInventory = instance.globalInventory;
    }

    public static void loadOnServerStarted(MinecraftServer server) {
        if (!instance.loaded) {
            ServerBackpacks.LOGGER.info("Running Server Backpack's data old format convertor...");
            load(server);
        }

        GlobalBackpackState globalBackpackState = GlobalBackpackState.getServerState(server);
        instance.globalInventory.setInventoryDirectly(globalBackpackState.globalInventory.heldStacks());
    }

    //
    // SAVING AND ADDING BACKPACKS
    //

    public static void saveToBackpackInventory(BackpackInstance instance) {
        if (instance == null) return;
        if (instance.getUuid() != null && instance.inventory() != null) {
            addBackpack(instance).ifPresent(saved ->
                    saved.saveToInventory(instance.inventory()));
        }
    }

    public static Optional<BackpackInstance> addBackpack(BackpackInstance instance) {
        UUID uuid = instance.getUuid();
        if (uuid != null && instance.inventory() != null) {
            BackpackManager.instance.storedInstances.putIfAbsent(uuid, instance);
        }
        return getInstance(uuid);
    }

    public static void addBackpack(UUID uuid, BackpackInventory inventory) {
        addBackpack(new BackpackInstance(uuid, inventory));
    }

    //
    // UUID
    //

    public static @Nullable UUID getStackUUID(ItemStack stack) {
        UUID uuid = stack.get(BackpackDataComponentTypes.BACKPACK_UUID);

        if (uuid == null) {
            String legacy = stack.get(BackpackDataComponentTypes.STRING_UUID);
            if (legacy != null) {
                uuid = UUID.fromString(legacy);
                stack.set(BackpackDataComponentTypes.BACKPACK_UUID, uuid);
                stack.remove(BackpackDataComponentTypes.STRING_UUID);
            }
        }

        return uuid;
    }

    public static UUID createNewUUID(ItemStack stack) {
        UUID uuid = getStackUUID(stack);
        if (uuid == null) {
            uuid = generateUniqueUUID();
            stack.set(BackpackDataComponentTypes.BACKPACK_UUID, uuid);
        }
        return uuid;
    }

    // The cake is a lie.
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
        return instance.hasBackpack(uuid)
                ? Optional.ofNullable(instance.storedInstances.get(uuid))
                : Optional.empty();
    }

    public static Optional<BackpackInstance> getInstance(UUID uuid, int slots) {
        if (instance == null) return Optional.empty();

        if (instance.hasBackpack(uuid))
            resizeInventory(uuid, slots);
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

    public static MinecraftServer getServer() {
        return instance.server;
    }

    public static void setGlobalInventory(DefaultedList<ItemStack> stacks) {
        instance.globalInventory.setInventoryDirectly(stacks);
    }

    public static BackpackInventory getGlobalInventory() {
        return instance.globalInventory;
    }

    public static void resizeInventory(UUID uuid, int newSize) {
        if (uuid != null) getInstance(uuid).ifPresent(
                backpackInstance ->
                        backpackInstance.inventory().resize(newSize));
    }

    public boolean hasBackpack(UUID uuid) {
        return uuid != null && this.storedInstances.containsKey(uuid);
    }

    private enum DATA_TYPE {
        FILE_DATA,
        MINECRAFT_STATE,
        OLD_MINECRAFT_STATE,
        SQL
    }
}
