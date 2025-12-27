package com.rouesvm.servback.technical.data.types.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.data.codecs.BackpackInstanceData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

import static com.rouesvm.servback.ServerBackpacks.MOD_ID;

public class BackpackPersistentState extends PersistentState {
    public final Set<BackpackInstanceData> storedInventories;

    private static final Codec<BackpackPersistentState> SAVE_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                      BackpackInstanceData.CODEC.listOf().fieldOf("backpackContents").forGetter(BackpackPersistentState::getStoredInventories)
                    ).apply(instance, BackpackPersistentState::new));

    private static final PersistentStateType<BackpackPersistentState> type = new PersistentStateType<>(
            MOD_ID + "-v2",
            BackpackPersistentState::new,
            SAVE_CODEC,
            null
    );

    public BackpackPersistentState(List<BackpackInstanceData> data) {
        this.storedInventories = new HashSet<>();
        this.storedInventories.addAll(data);
    }

    public BackpackPersistentState() {
        this(Collections.emptyList());
    }

    public static BackpackPersistentState getServerState(MinecraftServer server) {
        ServerWorld world = server.getWorld(World.OVERWORLD);
        if (world != null) {
            PersistentStateManager persistentStateManager = world.getPersistentStateManager();
            BackpackPersistentState state = persistentStateManager.getOrCreate(type);
            state.markDirty();

            return state;
        } else return null;
   }

    public List<BackpackInstanceData> getStoredInventories() {
        return new ArrayList<>(this.storedInventories);
    }

    public void clearBackpackInstances() {
        this.storedInventories.clear();
    }

    public Set<BackpackInstance> getBackpackInstances() {
        return this.storedInventories.stream()
                .map(BackpackInstanceData::toInstance)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
