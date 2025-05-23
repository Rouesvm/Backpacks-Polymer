package com.rouesvm.servback.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rouesvm.servback.state.codecs.BackpackData;
import com.rouesvm.servback.state.codecs.InventoryData;
import com.rouesvm.servback.state.codecs.SlotData;
import com.rouesvm.servback.ui.inventory.BackpackInventory;
import com.rouesvm.servback.utils.BackpackInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

import static com.rouesvm.servback.Main.MOD_ID;

public class BackpackState extends PersistentState {
    public Set<BackpackData> storedInventories;

    private static final Codec<BackpackState> SAVE_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                      BackpackData.CODEC.listOf().fieldOf("backpackContents").forGetter(BackpackState::getStoredInventories)
                    ).apply(instance, BackpackState::new));

    private static final PersistentStateType<BackpackState> type = new PersistentStateType<>(
            MOD_ID + "-v2",
            BackpackState::new,
            SAVE_CODEC,
            null
    );

    public BackpackState(List<BackpackData> data) {
        this.storedInventories = new HashSet<>();
        this.storedInventories.addAll(data);
    }

    public BackpackState() {
        this(Collections.emptyList());
    }

    public static BackpackState getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        BackpackState state = persistentStateManager.getOrCreate(type);
        state.markDirty();
        return state;
   }

    public List<BackpackData> getStoredInventories() {
        return new ArrayList<>(this.storedInventories);
    }

    public Set<BackpackInstance> getBackpackInstances() {
        return this.storedInventories.stream()
                .map(data -> new BackpackInstance(
                        data.getUuid(),
                        new BackpackInventory(InventoryData.getHeldStacks(data.getInventoryData().getItemStacks()))
                ))
                .collect(Collectors.toSet());
    }

    public void setStoredInventories(Set<BackpackInstance> backpackInstances) {
        this.storedInventories.clear();
        backpackInstances.forEach(instance -> {
            BackpackData data = new BackpackData(
                    instance.getUuid(),
                    new InventoryData(SlotData.writeToCodec(instance.getHeldInventory()))
            );
            this.storedInventories.add(data);
        });
        markDirty();
    }
}
