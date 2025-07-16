package com.rouesvm.servback.technical.data.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.technical.data.BackpackData;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.data.BackpackManager;
import com.rouesvm.servback.technical.data.state.codecs.BackpackInstanceData;
import com.rouesvm.servback.technical.data.state.codecs.InventoryData;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.server.MinecraftServer;
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

    public static boolean loadOldData(BackpackState state) {
        Set<BackpackInstance> stateInstances = state.getBackpackInstances();
        if (stateInstances != null && !stateInstances.isEmpty()) {
            BackpackManager.instance().loadData(stateInstances);

            BackpackData.setStoredInventories(stateInstances);
            state.clearBackpackInstances();
            state.markDirty();

            ServerBackpacks.LOGGER.info("Loaded Server Backpack's old format.");

            return true;
        }

        return false;
    }

    public static BackpackState getServerState(MinecraftServer server) {
        if (server.getWorld(World.OVERWORLD) == null) return null;

        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        BackpackState state = persistentStateManager.getOrCreate(type);
        state.markDirty();
        return state;
   }

    public List<BackpackInstanceData> getStoredInventories() {
        return new ArrayList<>(this.storedInventories);
    }

    public void clearBackpackInstances() {
        this.storedInventories.clear();
    }

    public Set<BackpackInstance> getBackpackInstances() {
        return this.storedInventories.stream()
                .map(data -> new BackpackInstance(
                        data.getUuid(),
                        new BackpackInventory(InventoryData.getHeldStacks(data.getInventoryData().getItemStacks()))
                ))
                .collect(Collectors.toSet());
    }
}
