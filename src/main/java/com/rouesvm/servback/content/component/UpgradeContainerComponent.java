package com.rouesvm.servback.content.component;

import com.mojang.serialization.Codec;
import com.rouesvm.servback.content.upgrade.BaseUpgrade;
import com.rouesvm.servback.registry.BackpackUpgradeRegistry;
import com.rouesvm.servback.technical.data.BackpackManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpgradeContainerComponent {
    public List<BaseUpgrade> baseUpgrades;

    public UpgradeContainerComponent(List<BaseUpgrade> baseUpgrades) {
        this.baseUpgrades = baseUpgrades;
    }

    public static UpgradeContainerComponent of(List<BaseUpgrade> baseUpgrades) {
        return new UpgradeContainerComponent(baseUpgrades);
    }

    public static PacketCodec<ByteBuf, UpgradeContainerComponent> PACKET_CODEC = new PacketCodec<>() {
        @Override
        public void encode(ByteBuf buf, UpgradeContainerComponent value) {
            buf.writeInt(value.baseUpgrades.size());
            for (BaseUpgrade upgrade : value.baseUpgrades) {
                Identifier id = upgrade.id;
                NbtWriteView data = NbtWriteView.create(ErrorReporter.EMPTY);
                upgrade.writeView(data);

                if (id == null) throw new IllegalArgumentException("Unknown upgrade: " + upgrade);
                writeIdentifier(buf, id);
                writeView(buf, data.getNbt());
            }
        }

        @Override
        public UpgradeContainerComponent decode(ByteBuf buf) {
            int size = buf.readInt();
            List<BaseUpgrade> upgrades = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                BaseUpgrade upgrade = BackpackUpgradeRegistry.UPGRADES.get(readIdentifier(buf));
                NbtCompound data = readView(buf);
                if (upgrade != null) {
                    upgrade.readView(NbtReadView.create(ErrorReporter.EMPTY, BackpackManager.instance.server.getRegistryManager(), data));
                    upgrades.add(upgrade);
                }
            }
            return of(upgrades);
        }
    };

    public static final Codec<UpgradeContainerComponent> CODEC =
            Codec.unboundedMap(Codec.STRING, NbtCompound.CODEC).xmap(
                    map -> {
                        List<BaseUpgrade> upgrades = new ArrayList<>();
                        for (Map.Entry<String, NbtCompound> entry : map.entrySet()) {
                            Identifier id = Identifier.tryParse(entry.getKey());
                            NbtCompound data = entry.getValue();
                            if (id != null && data != null) {
                                BaseUpgrade upgrade = BackpackUpgradeRegistry.UPGRADES.get(id);
                                if (upgrade != null) {
                                    upgrade.readView(NbtReadView.create(ErrorReporter.EMPTY, BackpackManager.instance.server.getRegistryManager(), data));
                                    upgrades.add(upgrade);
                                }
                            }
                        }
                        return UpgradeContainerComponent.of(upgrades);
                    },
                    upgradeContainer -> {
                        Map<String, NbtCompound> out = new HashMap<>();
                        for (BaseUpgrade upgrade : upgradeContainer.baseUpgrades) {
                            NbtWriteView data = NbtWriteView.create(ErrorReporter.EMPTY);
                            upgrade.writeView(data);
                            out.put(upgrade.id.toString(), data.getNbt());
                        }
                        return out;
                    }
            );

    private static void writeView(ByteBuf buf, NbtCompound compound) {
        PacketByteBuf packetBuf = new PacketByteBuf(buf);
        packetBuf.writeNbt(compound);
    }

    private static NbtCompound readView(ByteBuf buf) {
        PacketByteBuf packetBuf = new PacketByteBuf(buf);
        return packetBuf.readNbt();
    }

    private static void writeIdentifier(ByteBuf buf, Identifier id) {
        PacketByteBuf packetBuf = new PacketByteBuf(buf);
        packetBuf.writeIdentifier(id);
    }

    private static Identifier readIdentifier(ByteBuf buf) {
        PacketByteBuf packetBuf = new PacketByteBuf(buf);
        return packetBuf.readIdentifier();
    }
}
