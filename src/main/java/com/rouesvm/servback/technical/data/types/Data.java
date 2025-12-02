package com.rouesvm.servback.technical.data.types;

import com.rouesvm.servback.technical.data.BackpackInstance;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface Data {
    Set<UUID> getUUIDs();
    Set<BackpackInstance> getBackpackInstances();

    Optional<BackpackInstance> getOrLoadBackpack(UUID uuid);
    boolean loadData(boolean hasLoaded);

    void shutdownThread();

    void saveSingleToDisk(BackpackInstance instance, Path saveDir);
    void saveSingleToDisk(BackpackInstance instance);
    void saveAllToDisk();

    void replaceStoredInventory(BackpackInstance backpackInstance);
    void replaceStoredInventories(Set<BackpackInstance> backpackInstances);

    void createSingularBackup(BackpackInstance instance);
    void createBackup();
}
