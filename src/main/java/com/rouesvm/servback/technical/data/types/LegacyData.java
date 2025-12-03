package com.rouesvm.servback.technical.data.types;

import com.rouesvm.servback.technical.data.BackpackInstance;

import java.nio.file.Path;
import java.util.Set;

public interface LegacyData extends Data {
    @Override
    default Set<BackpackInstance> getBackpackInstances() {
        return Set.of();
    }

    @Override
    default void saveSingleToDisk(BackpackInstance instance, Path saveDir) {}
    @Override
    default void saveSingleToDisk(BackpackInstance instance) {}
    @Override
    default void saveAllToDisk() {}

    @Override
    default void replaceStoredInventory(BackpackInstance backpackInstance) {}
    @Override
    default void replaceStoredInventories(Set<BackpackInstance> backpackInstances) {}

    @Override
    default void createSingularBackup(BackpackInstance instance) {}
    @Override
    default void createBackup() {}
}
