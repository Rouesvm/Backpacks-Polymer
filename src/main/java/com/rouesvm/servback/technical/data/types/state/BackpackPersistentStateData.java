package com.rouesvm.servback.technical.data.types.state;

import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.data.DATA_TYPE;
import com.rouesvm.servback.technical.data.types.FallbackData;
import com.rouesvm.servback.technical.manager.Manager;

import java.util.Set;

public class BackpackPersistentStateData extends FallbackData {
    public BackpackPersistentStateData(Manager manager) {
        super(manager);
    }

    @Override
    public boolean initializeData(boolean hasLoaded) {
        BackpackPersistentState state = BackpackPersistentState.getServerState(manager().server());

        if (!hasLoaded && state != null) {
            Set<BackpackInstance> stateInstances = state.getBackpackInstances();
            if (stateInstances != null && !stateInstances.isEmpty()) {
                addBackpackInstances(stateInstances);
                state.clearBackpackInstances();
                state.setDirty();
                return true;
            }
        }

        return false;
    }

    @Override
    public DATA_TYPE getType() {
        return DATA_TYPE.MINECRAFT_STATE;
    }
}
