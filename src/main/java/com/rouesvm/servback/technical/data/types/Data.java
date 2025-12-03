package com.rouesvm.servback.technical.data.types;

import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.manager.BackpackManager;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface Data {
    Set<UUID> uuids = new ObjectOpenHashSet<>();

    Set<UUID> getUUIDs();
    Set<BackpackInstance> getBackpackInstances();

    Optional<BackpackInstance> getOrLoadBackpack(UUID uuid);
    boolean loadData(boolean hasLoaded);

    BackpackManager.DATA_TYPE getType();

    void saveSingleToDisk(BackpackInstance instance, Path saveDir);
    void saveSingleToDisk(BackpackInstance instance);
    void saveAllToDisk();

    void replaceStoredInventory(BackpackInstance backpackInstance);
    void replaceStoredInventories(Set<BackpackInstance> backpackInstances);

    void createSingularBackup(BackpackInstance instance);
    void createBackup();
}
