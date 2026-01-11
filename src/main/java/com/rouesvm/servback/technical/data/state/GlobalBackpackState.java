package com.rouesvm.servback.technical.data.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rouesvm.servback.technical.data.codecs.SlotData;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.ArrayList;
import java.util.List;

import static com.rouesvm.servback.ServerBackpacks.MOD_ID;

public class GlobalBackpackState extends SavedData {
    public static final int GLOBAL_SIZE = 9 * 3;
    public static final Codec<GlobalBackpackState> CODEC = RecordCodecBuilder.create(
            (instance) ->
                    instance.group(
                            SlotData.CODEC.listOf().fieldOf("itemStacks").forGetter(GlobalBackpackState::getInventory)
                    ).apply(instance, GlobalBackpackState::new));

    private static final SavedDataType<GlobalBackpackState> type = new SavedDataType<>(
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
        DimensionDataStorage persistentStateManager = server.overworld().getDataStorage();
        GlobalBackpackState state = persistentStateManager.computeIfAbsent(type);
        state.setDirty();
        return state;
    }

    public List<SlotData> getInventory() {
        return SlotData.writeToCodec(this.globalInventory.heldStacks());
    }
}
