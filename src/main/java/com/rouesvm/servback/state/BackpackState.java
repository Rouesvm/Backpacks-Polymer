package com.rouesvm.servback.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rouesvm.servback.state.codecs.BackpackData;
import com.rouesvm.servback.state.codecs.InventoryData;
import com.rouesvm.servback.state.codecs.SlotData;
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
    public Set<BackpackData> storedInventories;

    private static final Codec<BackpackState> SAVE_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                      BackpackData.CODEC.listOf().fieldOf("backpackContents").forGetter(BackpackState::getStoredInventories)
                    ).apply(instance, BackpackState::new));

    public BackpackState(List<BackpackData> data) {
        this.storedInventories = new HashSet<>();
        this.storedInventories.addAll(data);
    }

    public BackpackState() {
        this(new ArrayList<>());
    }

    private static final PersistentStateType<BackpackState> type = new PersistentStateType<>(
            MOD_ID + "-v2",
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
        return this.storedInventories.stream().toList();
    }

    public Set<BackpackInstance> getBackpackInstances() {
        Set<BackpackInstance> backpackInstances = new HashSet<>();
        this.storedInventories.forEach(data ->
                backpackInstances.add(
                        new BackpackInstance(data.getUuid(),
                        new BackpackInventory(InventoryData.getHeldStacks(data.getInventoryData().getItemStacks())))
                ));
        return backpackInstances;
    }

    public void setStoredInventories(Set<BackpackInstance> backpackInstances) {
        this.storedInventories = new HashSet<>();
        backpackInstances.forEach(instance ->
                this.storedInventories.add(new BackpackData(instance.getUuid(), new InventoryData(SlotData.writeToCodec(instance.getHeldInventory())))));
    }
}
