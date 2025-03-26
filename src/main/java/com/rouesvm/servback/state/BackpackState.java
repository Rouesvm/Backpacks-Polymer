package com.rouesvm.servback.state;

import com.rouesvm.servback.Main;
import com.rouesvm.servback.ui.inventory.BackpackInventory;
import com.rouesvm.servback.utils.BackpackInstance;
import com.rouesvm.servback.utils.BackpackManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class BackpackState extends PersistentState {
    public BackpackInventory globalInventory = new BackpackInventory(9 * 3);
    public Set<BackpackInstance> storedInventories = new HashSet<>();

    private static final Type<BackpackState> type = new Type<>(
            BackpackState::new,
            BackpackState::createFromNbt,
            null
    );

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.put("global", globalInventory.save(registryLookup));
        BackpackManager.saveNbt(storedInventories, nbt, registryLookup);
        return nbt;
    }

    public static BackpackState createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        BackpackState state = new BackpackState();
        state.globalInventory = BackpackInventory.load(tag.getCompound("global"), registryLookup);
        BackpackManager.loadNbt(state.storedInventories, tag, registryLookup);
        return state;
    }

    public static BackpackState getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        BackpackState state = persistentStateManager.getOrCreate(type, Main.MOD_ID);
        state.markDirty();

        return state;
    }
}
