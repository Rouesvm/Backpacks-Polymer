package com.rouesvm.servback.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BackpackManager {
    public HashMap<UUID, BackpackInventory> storedInventories = new HashMap<>();

    public BackpackInventory getInventory(UUID uuid, int slots) {
        return this.storedInventories.getOrDefault(uuid, new BackpackInventory(slots));
    }

    public BackpackInventory getInventory(UUID uuid) {
        return this.storedInventories.getOrDefault(uuid, new BackpackInventory(27));
    }

    public boolean saveBackpack(UUID uuid, BackpackInventory backpackInventory) {
        if (uuid != null && backpackInventory != null) {
            this.storedInventories.remove(uuid);
            this.storedInventories.put(uuid, backpackInventory);
            return true;
        }
        return false;
    }

    public boolean saveBackpack(BackpackInstance backpackInstance) {
        return saveBackpack(backpackInstance.uuid, backpackInstance.backpackInventory);
    }

    public void load(Set<BackpackInstance> instances) {
        instances.forEach(backpackInstance -> this.storedInventories.put(backpackInstance.uuid, backpackInstance.backpackInventory));
    }

    public Set<BackpackInstance> save() {
        Set<BackpackInstance> backpackInstances = new HashSet<>();
        this.storedInventories.forEach((key, value) -> backpackInstances.add(new BackpackInstance(key, value)));
        return backpackInstances;
    }
}
