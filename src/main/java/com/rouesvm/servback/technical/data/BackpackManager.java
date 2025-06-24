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

    public final BackpackInventory globalInventory = new BackpackInventory(9 * 3);
    public final Map<UUID, BackpackInstance> storedInstances = new HashMap<>();

    private boolean loaded = false;

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
        BackpackState state = BackpackState.getServerState(server);

        if (!instance.loaded) {
            BackpackDataSaver.onServerStarting(server);

            Set<BackpackInstance> dataInstances = BackpackDataSaver.getBackpackInstances();
            if (dataInstances != null && !dataInstances.isEmpty()) {
                instance.load(dataInstances);
                instance.loaded = true;
            }
        }

        if (!instance.loaded && state != null) {
            Set<BackpackInstance> stateInstances = state.getBackpackInstances();
            if (stateInstances != null && !stateInstances.isEmpty()) {
                instance.load(stateInstances);

                BackpackDataSaver.setStoredInventories(stateInstances);
                state.clearBackpackInstances();
                state.markDirty();

                instance.loaded = true;
            }
        }
    }

    public static void loadOnServerStarted(MinecraftServer server) {
        load(server);

        GlobalBackpackState globalBackpackState = GlobalBackpackState.getServerState(server);
        instance.globalInventory.setInventoryDirectly(globalBackpackState.globalInventory.heldStacks());
    }

    //
    // SAVING AND ADDING BACKPACKS
    //

    public static void saveBackpack(BackpackInstance instance) {
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
        UUID uuid = stack.get(BackpackDataComponentTypes.BACKPACK_UUID_TYPE);

        if (uuid == null) {
            String legacy = stack.get(BackpackDataComponentTypes.UUID_TYPE);
            if (legacy != null) {
                uuid = UUID.fromString(legacy);
                stack.set(BackpackDataComponentTypes.BACKPACK_UUID_TYPE, uuid);
                stack.remove(BackpackDataComponentTypes.UUID_TYPE);
            }
        }

        return uuid;
    }

    public static UUID createNewUUID(ItemStack stack) {
        UUID uuid = getStackUUID(stack);
        if (uuid == null) {
            uuid = generateUniqueUUID();
            stack.set(BackpackDataComponentTypes.BACKPACK_UUID_TYPE, uuid);
        }
        return uuid;
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
                        backpackInstance.inventory().resize(newSize));
    }

    public boolean hasBackpack(UUID uuid) {
        return uuid != null && this.storedInstances.containsKey(uuid);
    }
}
