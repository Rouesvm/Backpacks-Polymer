package com.rouesvm.servback.content.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rouesvm.servback.content.upgrade.PersistentUpgrade;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.content.upgrade.UpgradeType;
import com.rouesvm.servback.registry.BackpackUpgradeRegistry;
import com.rouesvm.servback.technical.manager.BackpackManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;

public class UpgradeComponent {
    private final Upgrade upgrade;

    private UpgradeComponent(Upgrade upgrade) {
        this.upgrade = upgrade;
    }

    public static UpgradeComponent of(Upgrade upgrade) {
        return new UpgradeComponent(upgrade);
    }

    public Upgrade getUpgrade() {
        return upgrade;
    }

    public static final PacketCodec<ByteBuf, UpgradeComponent> PACKET_CODEC = null;

    public static final Codec<UpgradeComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(component ->
                    BackpackUpgradeRegistry.getRegistry().getId(component.getUpgrade().getType())),
            NbtCompound.CODEC.fieldOf("data").forGetter(component -> {
                NbtWriteView data = NbtWriteView.create(ErrorReporter.EMPTY);
                Upgrade upgrade = component.getUpgrade();
                if (upgrade instanceof PersistentUpgrade saveableUpgrade) {
                    saveableUpgrade.writeView(data);
                }

                return data.getNbt();
            })
            ).apply(instance, (id, data) -> {
                UpgradeType<? extends Upgrade> type = BackpackUpgradeRegistry.get(id);
                Upgrade upgrade = type.create();
                if (upgrade instanceof PersistentUpgrade saveableUpgrade) {
                    saveableUpgrade.readView(NbtReadView.create(ErrorReporter.EMPTY, BackpackManager.server().getRegistryManager(), data));
                }

                return UpgradeComponent.of(upgrade);
            })
    );
}
