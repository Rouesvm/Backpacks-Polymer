package com.rouesvm.servback.content.upgrade;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record UpgradeType<T extends Upgrade>(Identifier id, UpgradeFactory<T> factory) {

    public Item getItem() {
        return Registries.ITEM.get(id.withSuffixedPath("_upgrade"));
    }

    public T create() {
        return factory.create();
    }

    public Text getTranslationKey() {
        return Text.translatable(id.toTranslationKey("upgrade"));
    }

    public interface UpgradeFactory<T extends Upgrade> {
        T create();
    }
}
