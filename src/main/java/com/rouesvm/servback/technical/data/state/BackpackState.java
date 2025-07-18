package com.rouesvm.servback.technical.data.state;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
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

    public static void loadNbt(Set<BackpackInstance> instances, NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.getList("backpackContents", NbtCompound.COMPOUND_TYPE).forEach(element ->
                instances.add(BackpackInstance.load((NbtCompound) element, registryLookup)));
    }

    public static void saveNbt(Set<BackpackInstance> instances, NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        if (!instances.isEmpty()) {
            NbtList nbtList = new NbtList();
            instances.forEach(instance -> nbtList.add(instance.save(registryLookup)));
            nbt.put("backpackContents", nbtList);
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.put("global", globalInventory.save(registryLookup));
        saveNbt(storedInventories, nbt, registryLookup);
        return nbt;
    }

    public static BackpackState createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        BackpackState state = new BackpackState();
        state.globalInventory = BackpackInventory.load(tag.getCompound("global"), registryLookup);
        loadNbt(state.storedInventories, tag, registryLookup);
        return state;
    }

    public static BackpackState getServerState(MinecraftServer server) {
        if (server.getWorld(World.OVERWORLD) == null) return null;

        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        BackpackState state = persistentStateManager.getOrCreate(type, ServerBackpacks.MOD_ID);
        state.markDirty();

        return state;
    }
}
