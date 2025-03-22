package com.rouesvm.servback.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rouesvm.servback.utils.BackpackInstance;
import com.rouesvm.servback.utils.BackpackInventory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.rouesvm.servback.Main.MOD_ID;

public class BackpackState extends PersistentState {
    public List<BackpackData> storedInventories;

    private static final Codec<BackpackState> SAVE_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                      BackpackData.CODEC.listOf().fieldOf("inventories").forGetter(BackpackState::getStoredInventories)
                    ).apply(instance, BackpackState::new));

    public BackpackState(List<BackpackData> data) {
        this.storedInventories = new ArrayList<>();
    }

    public BackpackState() {
        this(new ArrayList<>());
    }

    private static final PersistentStateType<BackpackState> type = new PersistentStateType<>(
            MOD_ID,
            BackpackState::new,
            SAVE_CODEC,
            null
    );

    public static BackpackState getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getOverworld().getPersistentStateManager();
        BackpackState state = persistentStateManager.getOrCreate(type);
        state.markDirty();
        return state;
   }

    public List<BackpackData> getStoredInventories() {
        return this.storedInventories;
    }

    public Set<BackpackInstance> getBackpackInstances() {
        Set<BackpackInstance> backpackInstances = new HashSet<>();
        this.storedInventories.forEach(data ->
                backpackInstances.add(
                        new BackpackInstance(data.getUuid(),
                        new BackpackInventory(data.getHeldStacks()))
                ));
        return backpackInstances;
    }

    public void setStoredInventories(Set<BackpackInstance> backpackInstances) {
        backpackInstances.forEach(instance ->
                this.storedInventories.add(new BackpackData(instance.getUuid(), instance.getHeldInventory())));
    }
}
