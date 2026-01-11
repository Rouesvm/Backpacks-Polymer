package com.rouesvm.servback.content.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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

public record UpgradeComponent(Upgrade upgrade) {

    public static UpgradeComponent of(Upgrade upgrade) {
        return new UpgradeComponent(upgrade);
    }

    public static final StreamCodec<ByteBuf, UpgradeComponent> PACKET_CODEC = null;

    public static final Codec<UpgradeComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(component ->
                    BackpackUpgradeRegistry.getRegistry().getKey(component.upgrade().getType())),
            CompoundTag.CODEC.fieldOf("data").forGetter(component -> {
                TagValueOutput data = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
                Upgrade upgrade = component.upgrade();
                if (upgrade instanceof PersistentUpgrade persistentUpgrade) {
                    persistentUpgrade.writeView(data);
                }

                return data.buildResult();
            })
            ).apply(instance, (id, data) -> {
                UpgradeType<? extends Upgrade> type = BackpackUpgradeRegistry.get(id);
                Upgrade upgrade = type.create();
                if (upgrade instanceof PersistentUpgrade persistentUpgrade) {
                    persistentUpgrade.readView(TagValueInput.create(ProblemReporter.DISCARDING, BackpackManager.instance().server().registryAccess(), data));
                }

                return UpgradeComponent.of(upgrade);
            })
    );
}
