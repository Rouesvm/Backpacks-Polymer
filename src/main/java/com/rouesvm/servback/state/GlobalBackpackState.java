package com.rouesvm.servback.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rouesvm.servback.utils.BackpackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;

import java.util.List;

import static com.rouesvm.servback.Main.MOD_ID;

public class GlobalBackpackState extends PersistentState {
    public BackpackInventory globalInventory = new BackpackInventory(9 * 3);

    public static final Codec<GlobalBackpackState> CODEC = RecordCodecBuilder.create(
            (instance) ->
                    instance.group(
                            ItemStack.CODEC.listOf().fieldOf("itemStacks").forGetter(GlobalBackpackState::getInventory)
                    ).apply(instance, GlobalBackpackState::new));;

    private GlobalBackpackState(List<ItemStack> data) {
        for(int i = 0; i < 9 * 3; ++i) {
            ItemStack itemStack = i < data.size() ? data.get(i) : ItemStack.EMPTY;
            this.globalInventory.setStack(i, itemStack.copy());
        }
    }

    private GlobalBackpackState() {
        this(DefaultedList.ofSize(9*3, ItemStack.EMPTY));
    }

    private static PersistentStateType<GlobalBackpackState> type = new PersistentStateType<>(
            MOD_ID + "-global-backpack",
            GlobalBackpackState::new,
            CODEC,
            null
    );

    public static GlobalBackpackState getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getOverworld().getPersistentStateManager();
        GlobalBackpackState state = persistentStateManager.getOrCreate(type);
        state.markDirty();
        return state;
    }

    public List<ItemStack> getInventory() {
        return this.globalInventory.getHeldStacks();
    }
}
