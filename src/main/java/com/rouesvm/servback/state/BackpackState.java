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
    public Set<BackpackInstance> storedInventories;

    private static final Codec<BackpackState> SAVE_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                      BackpackData.CODEC.listOf().fieldOf("inventories").forGetter(BackpackState::getStoredInventories)
                    ).apply(instance, BackpackState::new));

    public BackpackState(List<BackpackData> data) {
        this.storedInventories = new HashSet<>();
        data.forEach(backpackData -> {
            BackpackInventory backpackInventory = new BackpackInventory(backpackData.getItemStacks().size());
            backpackInventory.setInventoryDirectly(backpackData.getHeldStacks());
            storedInventories.add(new BackpackInstance(backpackData.getUuid(), backpackInventory));
        });
    }

    public BackpackState() {
        this(new ArrayList<>());
    }

    private static PersistentStateType<BackpackState> type = new PersistentStateType<>(
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
        List<BackpackData> list = new ArrayList<>();
        storedInventories.forEach(backpackInstance ->
                list.add(new BackpackData(backpackInstance.uuid, backpackInstance.getHeldInventory()))
        );
        return list;
    }
}
