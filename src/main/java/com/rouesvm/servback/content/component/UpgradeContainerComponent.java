package com.rouesvm.servback.content.component;

import com.mojang.serialization.Codec;
import com.rouesvm.servback.content.upgrade.PersistentUpgrade;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.content.upgrade.UpgradeType;
import com.rouesvm.servback.registry.BackpackUpgradeRegistry;
import com.rouesvm.servback.technical.manager.BackpackManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record UpgradeContainerComponent(List<Upgrade> baseUpgrades) {

    public static UpgradeContainerComponent of(List<Upgrade> baseUpgrades) {
        return new UpgradeContainerComponent(baseUpgrades);
    }

    public void add(Upgrade upgrade) {
        baseUpgrades.add(upgrade);
    }

    public static final StreamCodec<ByteBuf, UpgradeContainerComponent> PACKET_CODEC = null;

    public static final Codec<UpgradeContainerComponent> CODEC =
            Codec.unboundedMap(Codec.STRING, CompoundTag.CODEC).xmap(
                    map -> {
                        List<Upgrade> upgrades = new ArrayList<>();
                        for (Map.Entry<String, CompoundTag> entry : map.entrySet()) {
                            Identifier id = Identifier.tryParse(entry.getKey());
                            CompoundTag data = entry.getValue();
                            if (id == null || data == null) continue;

                            UpgradeType<? extends Upgrade> upgradeType = BackpackUpgradeRegistry.get(id);
                            if (upgradeType == null) continue;

                            Upgrade upgrade = upgradeType.create();
                            if (upgrade instanceof PersistentUpgrade persistentUpgrade) {
                                persistentUpgrade.readView(TagValueInput.create(ProblemReporter.DISCARDING, BackpackManager.instance().server().registryAccess(), data));
                            }
                            upgrades.add(upgrade);
                        }
                        return UpgradeContainerComponent.of(upgrades);
                    },
                    upgradeContainer -> {
                        Map<String, CompoundTag> out = new HashMap<>();
                        for (Upgrade upgrade : upgradeContainer.baseUpgrades) {
                            TagValueOutput data = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
                            if (upgrade instanceof PersistentUpgrade persistentUpgrade) {
                                persistentUpgrade.writeView(data);
                            }
                            out.put(upgrade.getType().getId().toString(), data.buildResult());
                        }
                        return out;
                    }
            );
}
