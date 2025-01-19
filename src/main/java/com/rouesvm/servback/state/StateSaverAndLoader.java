package com.rouesvm.servback.state;

import com.rouesvm.servback.utils.BackpackInstance;
import com.rouesvm.servback.utils.BackpackInventory;
import com.rouesvm.servback.utils.BackpackManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import com.rouesvm.servback.Main;

import java.util.HashSet;
import java.util.Set;

public class StateSaverAndLoader extends PersistentState {
    public BackpackInventory globalInventory = new BackpackInventory(9 * 3);
    public Set<BackpackInstance> storedInventories = new HashSet<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        globalInventory.save(registryLookup);
        BackpackManager.saveNbt(storedInventories, nbt, registryLookup);
        return nbt;
    }

    public static StateSaverAndLoader createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        StateSaverAndLoader state = new StateSaverAndLoader();
        state.globalInventory = BackpackInventory.load(tag, registryLookup);
        BackpackManager.loadNbt(state.storedInventories, tag, registryLookup);
        return state;
    }

    private static Type<StateSaverAndLoader> type = new Type<>(
            StateSaverAndLoader::new,
            StateSaverAndLoader::createFromNbt,
            null
    );

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        StateSaverAndLoader state = persistentStateManager.getOrCreate(type, Main.MOD_ID);
        state.markDirty();

        return state;
    }
}
