package com.rouesvm.servback.state;

import com.rouesvm.servback.utils.BackpackInstance;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import com.rouesvm.servback.Main;

import java.util.HashSet;
import java.util.Set;

public class StateSaverAndLoader extends PersistentState {
    public DefaultedList<ItemStack> globalInventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
    public Set<BackpackInstance> storedInventories = new HashSet<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        Inventories.writeNbt(nbt, globalInventory, registryLookup);

        if (!storedInventories.isEmpty()) {
            NbtList nbtList = new NbtList();
            storedInventories.forEach(instance -> nbtList.add(instance.save(registryLookup)));
            nbt.put("backpackContents", nbtList);
        }

        return nbt;
    }

    public static StateSaverAndLoader createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        StateSaverAndLoader state = new StateSaverAndLoader();
        Inventories.readNbt(tag, state.globalInventory, registryLookup);

        tag.getList("backpackContents", NbtCompound.COMPOUND_TYPE).forEach(element ->
                state.storedInventories.add(BackpackInstance.load((NbtCompound) element, registryLookup)));

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
