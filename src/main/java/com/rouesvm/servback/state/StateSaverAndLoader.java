package com.rouesvm.servback.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rouesvm.servback.utils.BackpackInstance;
import com.rouesvm.servback.utils.BackpackInventory;
import com.rouesvm.servback.utils.BackpackManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.rouesvm.servback.Main.MOD_ID;

public class StateSaverAndLoader extends PersistentState {
    public BackpackInventory globalInventory = new BackpackInventory(9 * 3);
    public Set<BackpackInstance> storedInventories = new HashSet<>();

    private static final Codec<List<BackpackData>> SAVE_CODEC = BackpackData.CODEC.listOf().fieldOf("inventories").codec();

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

    private static PersistentStateType<StateSaverAndLoader> type = new PersistentStateType<>(
            MOD_ID,
            StateSaverAndLoader::createFromNbt,
            SAVE_CODEC,
            null
    );

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getOverworld().getPersistentStateManager();

        StateSaverAndLoader state = persistentStateManager.getOrCreate(type);
        state.markDirty();

        return state;
    }

    public record BackpackData(UUID uuid, List<ItemStack> inventory) {
        public static final Codec<BackpackData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Uuids.CODEC.fieldOf("id").forGetter(BackpackData::uuid),
                ItemStack.CODEC.listOf().fieldOf("itemStacks").forGetter(BackpackData::inventory)
        ).apply(instance, BackpackData::new));
    }
}
