package com.rouesvm.servback.state;

import com.mojang.serialization.Codec;
import com.rouesvm.servback.utils.BackpackInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.rouesvm.servback.Main.MOD_ID;

public class StateSaverAndLoader extends PersistentState {
    public Set<BackpackInstance> storedInventories;

    private static final Codec<List<BackpackData>> SAVE_CODEC = BackpackData.CODEC.listOf().fieldOf("inventories").codec();

    public StateSaverAndLoader(List<BackpackData> data) {
        this.storedInventories = new HashSet<>();
    }

    public StateSaverAndLoader() {
        this(new ArrayList<>());
    }

    private static PersistentStateType<StateSaverAndLoader> type = new PersistentStateType<>(
            MOD_ID,
            StateSaverAndLoader::new,
            SAVE_CODEC,
            null
    );

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getOverworld().getPersistentStateManager();

        StateSaverAndLoader state = persistentStateManager.getOrCreate(type);
        state.markDirty();

       return state;
   }
}
