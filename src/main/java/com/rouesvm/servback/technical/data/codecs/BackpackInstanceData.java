package com.rouesvm.servback.technical.data.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.core.UUIDUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record BackpackInstanceData(Optional<UUID> uuid, Optional<InventoryData> inventoryData, Optional<Long> last_accessed, Optional<Integer> size, Optional<Integer> data_version) {
    public static final Codec<BackpackInstanceData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("uuid").forGetter(BackpackInstanceData::uuid),
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

    public @Nullable BackpackInstance toInstance() {
        long lastAccessed = this.last_accessed().orElse(System.currentTimeMillis());
        int size = this.size().orElse(9 * 6);

        if (this.uuid().isEmpty()
        ) return null;

        UUID uuid = this.uuid().get();

        if (this.inventoryData().isEmpty()
        ) return new BackpackInstance(lastAccessed, uuid, new BackpackInventory(size));

        InventoryData inventoryData = this.inventoryData().get();
        BackpackInventory inventory = new BackpackInventory(InventoryData.getHeldStacks(inventoryData.itemStacks(), size));
        return new BackpackInstance(lastAccessed, uuid, inventory);
    }

    public @Nullable BackpackInstance toInstance(@NonNull UUID uuid) {
        long lastAccessed = this.last_accessed().orElse(System.currentTimeMillis());
        int size = this.size().orElse(9 * 6);

        InventoryData data = this.inventoryData().orElse(null);

        if (data != null) {
            return new BackpackInstance(lastAccessed, uuid, new BackpackInventory(
                    InventoryData.getHeldStacks(data.itemStacks(), size)));
        } else return toInstance();
    }
}
