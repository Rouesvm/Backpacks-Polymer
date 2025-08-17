package com.rouesvm.servback.content.component;

import com.mojang.serialization.Codec;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpgradeContainerComponent {
    private final List<Upgrade> baseUpgrades;

    private UpgradeContainerComponent(List<Upgrade> baseUpgrades) {
        this.baseUpgrades = baseUpgrades;
    }

    public static UpgradeContainerComponent of(List<Upgrade> baseUpgrades) {
        return new UpgradeContainerComponent(baseUpgrades);
    }

    public List<Upgrade> getBaseUpgrades() {
        return baseUpgrades;
    }

    public void add(Upgrade upgrade) {
        baseUpgrades.add(upgrade);
    }

    public static final PacketCodec<ByteBuf, UpgradeContainerComponent> PACKET_CODEC = null;

    public static final Codec<UpgradeContainerComponent> CODEC =
            Codec.unboundedMap(Codec.STRING, NbtCompound.CODEC).xmap(
                    map -> {
                        List<Upgrade> upgrades = new ArrayList<>();
                        for (Map.Entry<String, NbtCompound> entry : map.entrySet()) {
                            Identifier id = Identifier.tryParse(entry.getKey());
                            NbtCompound data = entry.getValue();
                            if (id == null || data == null) continue;

                            UpgradeType<? extends Upgrade> upgradeType = BackpackUpgradeRegistry.get(id);
                            if (upgradeType == null) continue;

                            Upgrade upgrade = upgradeType.create();
                            if (upgrade instanceof PersistentUpgrade saveableUpgrade) {
                                saveableUpgrade.readView(NbtReadView.create(ErrorReporter.EMPTY, BackpackManager.server().getRegistryManager(), data));
                            }
                            upgrades.add(upgrade);
                        }
                        return UpgradeContainerComponent.of(upgrades);
                    },
                    upgradeContainer -> {
                        Map<String, NbtCompound> out = new HashMap<>();
                        for (Upgrade upgrade : upgradeContainer.baseUpgrades) {
                            NbtWriteView data = NbtWriteView.create(ErrorReporter.EMPTY);
                            if (upgrade instanceof PersistentUpgrade persistentUpgrade) {
                                persistentUpgrade.writeView(data);
                            }
                            out.put(upgrade.getType().getId().toString(), data.getNbt());
                        }
                        return out;
                    }
            );
}
