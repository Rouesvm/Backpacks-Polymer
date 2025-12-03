package com.rouesvm.servback.technical.data.types;

import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.data.DATA_TYPE;
import com.rouesvm.servback.technical.manager.Manager;

import java.nio.file.Path;
import java.util.*;

public class FallbackData implements Data {
    private final Map<UUID, BackpackInstance> loadedBackpacks = new HashMap<>();

    private final Manager manager;

    public FallbackData(Manager manager) {
        this.manager = manager;
    }

    public Manager manager() {
        return manager;
    }

    public void addBackpackInstances(Set<BackpackInstance> instances) {
        instances.forEach(backpackInstance -> loadedBackpacks.put(backpackInstance.uuid(), backpackInstance));
    }

    @Override
    public Set<UUID> getUUIDs() {
        return new HashSet<>(loadedBackpacks.keySet());
    }

    @Override
    public Set<BackpackInstance> getBackpackInstances() {
        return Set.of();
    }

    @Override
    public Optional<BackpackInstance> getOrLoadBackpack(UUID uuid) {
        BackpackInstance cached = loadedBackpacks.get(uuid);
        if (cached != null) {
            return Optional.of(cached);
        } else return Optional.empty();
    }

    @Override
    public boolean initializeData(boolean hasLoaded) {
        return false;
    }

    @Override
    public DATA_TYPE getType() {
        return DATA_TYPE.NONE;
    }

    @Override
    public void saveSingleToDisk(BackpackInstance instance, Path saveDir) {}
    @Override
    public void saveSingleToDisk(BackpackInstance instance) {}
    @Override
    public void saveAllToDisk() {}

    @Override
    public void replaceStoredInventory(BackpackInstance backpackInstance) {}
    @Override
    public void replaceStoredInventories(Set<BackpackInstance> backpackInstances) {}

    @Override
    public void createSingularBackup(BackpackInstance instance) {}
    @Override
    public void createBackup() {}
}
