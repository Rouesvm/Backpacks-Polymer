package com.rouesvm.servback.technical.data.types.state;

import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.data.DATA_TYPE;
import com.rouesvm.servback.technical.data.types.LegacyData;
import com.rouesvm.servback.technical.manager.Manager;

import java.util.*;

public class BackpackPersistentStateData implements LegacyData {
    private final Map<UUID, BackpackInstance> loadedBackpacks = new HashMap<>();

    private final Manager manager;

    public BackpackPersistentStateData(Manager manager) {
        this.manager = manager;
    }

    @Override
    public Set<UUID> getUUIDs() {
        return new HashSet<>(uuids);
    }

    @Override
    public boolean loadData(boolean hasLoaded) {
        BackpackPersistentState state = BackpackPersistentState.getServerState(manager.server());

        if (!hasLoaded && state != null) {
            Set<BackpackInstance> stateInstances = state.getBackpackInstances();
            if (stateInstances != null && !stateInstances.isEmpty()) {
                stateInstances.forEach(instance -> loadedBackpacks.put(instance.uuid(), instance));
                state.clearBackpackInstances();
                state.markDirty();
                return true;
            }
        }

        return false;
    }

    @Override
    public Optional<BackpackInstance> getOrLoadBackpack(UUID uuid) {
        BackpackInstance cached = loadedBackpacks.get(uuid);
        if (cached != null) {
            return Optional.of(cached);
        } else return Optional.empty();
    }

    @Override
    public DATA_TYPE getType() {
        return DATA_TYPE.MINECRAFT_STATE;
    }
}
