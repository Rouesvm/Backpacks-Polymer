package com.rouesvm.servback.technical.data.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Uuids;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record BackpackInstanceData(Optional<UUID> uuid, Optional<InventoryData> inventoryData, Optional<Long> last_accessed, Optional<Integer> size, Optional<Integer> data_version) {
    public static final Codec<BackpackInstanceData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Uuids.INT_STREAM_CODEC.optionalFieldOf("uuid").forGetter(BackpackInstanceData::uuid),
            InventoryData.CODEC.optionalFieldOf("contents").forGetter(BackpackInstanceData::inventoryData),
            Codec.LONG.optionalFieldOf("last_accessed").forGetter(BackpackInstanceData::last_accessed),
            Codec.INT.optionalFieldOf("size").forGetter(BackpackInstanceData::size),
            Codec.INT.optionalFieldOf("data_version").forGetter(BackpackInstanceData::data_version)
    ).apply(instance, BackpackInstanceData::new));

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
