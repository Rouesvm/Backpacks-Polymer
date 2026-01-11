package com.rouesvm.servback.content.upgrade;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public class UpgradeType<T extends Upgrade> {
    private final Identifier id;
    private final UpgradeFactory<T> factory;

    public UpgradeType(Identifier id, UpgradeFactory<T> factory) {
        this.id = id;
        this.factory = factory;
    }

    public Item getItem() {
        return BuiltInRegistries.ITEM.getValue(id.withSuffix("_upgrade"));
    }

    public T create() {
        return factory.create();
    }

    public Identifier getId() {
        return id;
    }

    public Component getTranslationKey() {
        return Component.translatable(id.toLanguageKey("upgrade"));
    }

    public interface UpgradeFactory<T extends Upgrade> {
        T create();
    }
}
