package com.rouesvm.servback.technical.data.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rouesvm.servback.technical.data.codecs.SlotData;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;

import java.util.ArrayList;
import java.util.List;

import static com.rouesvm.servback.ServerBackpacks.MOD_ID;

public class GlobalBackpackState extends PersistentState {
    public static final int GLOBAL_SIZE = 9 * 3;
    public static final Codec<GlobalBackpackState> CODEC = RecordCodecBuilder.create(
            (instance) ->
                    instance.group(
                            SlotData.CODEC.listOf().fieldOf("itemStacks").forGetter(GlobalBackpackState::getInventory)
                    ).apply(instance, GlobalBackpackState::new));

    private static final PersistentStateType<GlobalBackpackState> type = new PersistentStateType<>(
            MOD_ID + "-v2-global",
            GlobalBackpackState::new,
            CODEC,
            null
    );

    public BackpackInventory globalInventory;

    private GlobalBackpackState(List<SlotData> data) {
        this.globalInventory = new BackpackInventory(GLOBAL_SIZE);

        if (!data.isEmpty()) {
            this.globalInventory.setInventoryDirectly(SlotData.readFromCodec(data, GLOBAL_SIZE));
        }
    }

    private GlobalBackpackState() {
        this(new ArrayList<>());
    }

    public static GlobalBackpackState getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getOverworld().getPersistentStateManager();
        GlobalBackpackState state = persistentStateManager.getOrCreate(type);
        state.markDirty();
        return state;
    }

    public List<SlotData> getInventory() {
        return SlotData.writeToCodec(this.globalInventory.heldStacks());
    }
}
