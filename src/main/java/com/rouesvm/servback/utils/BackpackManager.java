package com.rouesvm.servback.utils;

import com.rouesvm.servback.Main;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.state.BackpackState;
import com.rouesvm.servback.ui.inventory.BackpackInventory;
import com.rouesvm.servback.utils.cosmetic.CosmeticManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.collection.DefaultedList;

import java.util.*;

public class BackpackManager {
    private static BackpackManager manager = null;

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
        }
        return UUID.fromString(uuidString);
    }

    private static UUID generateUniqueUUID() {
        UUID uuid = UUID.randomUUID();
        if (manager != null) {
            int attempts = 0;
            while (manager.hasBackpack(uuid) && attempts < 100) {
                uuid = UUID.randomUUID();
                attempts++;
            }
        }
        return uuid;
    }

    public static BackpackInstance getInstance(UUID uuid) {
        BackpackManager manager = getManager();
        if (manager.hasBackpack(uuid)) {
            return manager.storedInstances.get(uuid);
        }
        return null;
    }

    public static BackpackInstance getInstance(UUID uuid, int slots) {
        BackpackManager manager = getManager();
        if (manager.hasBackpack(uuid)) {
            BackpackInstance backpack = manager.storedInstances.get(uuid);
            BackpackInventory inventory = backpack.getInventory();
            if (inventory.size() != slots) {
                backpack.setInventory(resizeInventory(inventory, slots));
                manager.saveBackpack(backpack);
            }
            return backpack;
        }
        return new BackpackInstance(uuid, new BackpackInventory(slots));
    }

    public static BackpackInventory getInventory(UUID uuid, int slots) {
        return getInstance(uuid, slots).getInventory();
    }

    public static BackpackInventory resizeInventory(BackpackInventory inventory, int newSize) {
        BackpackInventory newInventory = new BackpackInventory(newSize);
        inventory.copyTo(newInventory);
        return newInventory;
    }

    public static void resizeInventory(UUID uuid, BackpackInventory inventory, int newSize) {
        manager.saveBackpack(uuid, resizeInventory(inventory, newSize));
    }

    public void saveBackpack(BackpackInstance instance) {
        if (instance.getUuid() != null && instance.getInventory() != null) {
            this.storedInstances.put(instance.getUuid(), instance);
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

    public void load(Set<BackpackInstance> instances) {
        instances.forEach(this::saveBackpack);
    }

    public void load(MinecraftServer server) {
        BackpackState backpackState = BackpackState.getServerState(server);
        this.load(backpackState.storedInventories);
        this.globalInventory = backpackState.globalInventory;
    }

    public Set<BackpackInstance> save() {
        return new HashSet<>(this.storedInstances.values());
    }

    public void save(MinecraftServer server) {
        BackpackState backpackState = BackpackState.getServerState(server);
        backpackState.storedInventories = this.save();
        backpackState.globalInventory = this.globalInventory;
    }

    public void setGlobalInventory(DefaultedList<ItemStack> stacks) {
        this.globalInventory.setInventoryDirectly(stacks);
    }

    public boolean hasBackpack(UUID uuid) {
        return uuid != null && this.storedInstances.containsKey(uuid);
    }
}
