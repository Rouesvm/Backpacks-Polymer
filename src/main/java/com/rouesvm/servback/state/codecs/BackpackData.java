package com.rouesvm.servback.state.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Uuids;

import java.util.Objects;
import java.util.UUID;

public class BackpackData {
    private final UUID uuid;
    private final InventoryData itemStacks;

    public BackpackData(UUID uuid, InventoryData inventory) {
        this.uuid = uuid;
        this.itemStacks = inventory;
    }

    public static final Codec<BackpackData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Uuids.INT_STREAM_CODEC.fieldOf("uuid").forGetter(BackpackData::getUuid),
            InventoryData.CODEC.fieldOf("contents").forGetter(BackpackData::getInventoryData)
    ).apply(instance, BackpackData::new));

    public UUID getUuid() {
        return uuid;
    }

    public InventoryData getInventoryData() {
        return itemStacks;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BackpackData that = (BackpackData) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
}
