package com.rouesvm.servback.technical.data.state.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Uuids;

import java.util.Objects;
import java.util.UUID;

public record BackpackInstanceData(UUID uuid, InventoryData itemStacks) {

    public static final Codec<BackpackInstanceData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Uuids.INT_STREAM_CODEC.fieldOf("uuid").forGetter(BackpackInstanceData::uuid),
            InventoryData.CODEC.fieldOf("contents").forGetter(BackpackInstanceData::getInventoryData)
    ).apply(instance, BackpackInstanceData::new));

    public InventoryData getInventoryData() {
        return itemStacks;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BackpackInstanceData that = (BackpackInstanceData) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
}
