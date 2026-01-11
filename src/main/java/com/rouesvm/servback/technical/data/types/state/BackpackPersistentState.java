package com.rouesvm.servback.technical.data.types.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.data.codecs.BackpackInstanceData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.*;
import java.util.stream.Collectors;

import static com.rouesvm.servback.ServerBackpacks.MOD_ID;

public class BackpackPersistentState extends SavedData {
    public final Set<BackpackInstanceData> storedInventories;

    private static final Codec<BackpackPersistentState> SAVE_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                      BackpackInstanceData.CODEC.listOf().fieldOf("backpackContents").forGetter(BackpackPersistentState::getStoredInventories)
                    ).apply(instance, BackpackPersistentState::new));

    private static final SavedDataType<BackpackPersistentState> type = new SavedDataType<>(
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
        ServerLevel world = server.getLevel(Level.OVERWORLD);
        if (world != null) {
            DimensionDataStorage persistentStateManager = world.getDataStorage();
            BackpackPersistentState state = persistentStateManager.computeIfAbsent(type);
            state.setDirty();

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
