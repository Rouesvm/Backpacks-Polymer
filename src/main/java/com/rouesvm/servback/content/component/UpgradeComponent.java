package com.rouesvm.servback.content.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rouesvm.servback.content.upgrade.PersistentUpgrade;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.content.upgrade.UpgradeType;
import com.rouesvm.servback.registry.BackpackUpgradeRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;

public record UpgradeComponent(Upgrade upgrade) {

    public static UpgradeComponent of(Upgrade upgrade) {
        return new UpgradeComponent(upgrade);
    }

    public static final PacketCodec<ByteBuf, UpgradeComponent> PACKET_CODEC = null;

    public static final Codec<UpgradeComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(component ->
                    BackpackUpgradeRegistry.getRegistry().getId(component.upgrade().getType())),
            NbtCompound.CODEC.fieldOf("data").forGetter(component -> {
                NbtCompound data = new NbtCompound();
                Upgrade upgrade = component.upgrade();
                if (upgrade instanceof PersistentUpgrade persistentUpgrade) {
                    persistentUpgrade.writeView(data);
                }

                return data;
            })
            ).apply(instance, (id, data) -> {
                UpgradeType<? extends Upgrade> type = BackpackUpgradeRegistry.get(id);
                Upgrade upgrade = type.create();
                if (upgrade instanceof PersistentUpgrade persistentUpgrade) {
                    persistentUpgrade.readView(data);
                }

                return UpgradeComponent.of(upgrade);
            })
    );
}
