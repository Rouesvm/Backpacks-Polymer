package com.rouesvm.servback.technical.data.alternative;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.data.codecs.BackpackInstanceData;
import com.rouesvm.servback.technical.manager.BackpackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

import static com.rouesvm.servback.ServerBackpacks.MOD_ID;

public class BackpackState extends PersistentState {
    public final Set<BackpackInstanceData> storedInventories;

    private static final Codec<BackpackState> SAVE_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                      BackpackInstanceData.CODEC.listOf().fieldOf("backpackContents").forGetter(BackpackState::getStoredInventories)
                    ).apply(instance, BackpackState::new));

    private static final PersistentStateType<BackpackState> type = new PersistentStateType<>(
            MOD_ID + "-v2",
            BackpackState::new,
            SAVE_CODEC,
            null
    );

    public BackpackState(List<BackpackInstanceData> data) {
        this.storedInventories = new HashSet<>();
        this.storedInventories.addAll(data);
    }

    public BackpackState() {
        this(Collections.emptyList());
    }

    public static boolean loadData(MinecraftServer server, boolean hasLoaded) {
        BackpackState state = BackpackState.getServerState(server);

        if (!hasLoaded && state != null)
            return BackpackState.loadData(state);
        else return false;
    }

    private static boolean loadData(BackpackState state) {
        Set<BackpackInstance> stateInstances = state.getBackpackInstances();

        if (stateInstances != null && !stateInstances.isEmpty()) {
            BackpackManager.instance().loadIntoStoredInstances(stateInstances);

            state.clearBackpackInstances();
            state.markDirty();

            return true;
        }

        return false;
    }

    private static BackpackState getServerState(MinecraftServer server) {
        ServerWorld world = server.getWorld(World.OVERWORLD);
        if (world == null) return null;

        PersistentStateManager persistentStateManager = world.getPersistentStateManager();
        BackpackState state = persistentStateManager.getOrCreate(type);
        state.markDirty();

        return state;
   }

    private List<BackpackInstanceData> getStoredInventories() {
        return new ArrayList<>(this.storedInventories);
    }

    private void clearBackpackInstances() {
        this.storedInventories.clear();
    }

    private Set<BackpackInstance> getBackpackInstances() {
        return this.storedInventories.stream()
                .map(BackpackInstanceData::toInstance)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
