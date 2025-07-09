package com.rouesvm.servback.content.upgrade;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class UpgradeType<T extends Upgrade> {
    private final Identifier id;
    private final UpgradeFactory<T> factory;

    public UpgradeType(Identifier id, UpgradeFactory<T> factory) {
        this.id = id;
        this.factory = factory;
    }

    public Item getItem() {
        return Registries.ITEM.get(Identifier.of(id.getNamespace(), id.getPath() + "_upgrade"));
    }

    public T create() {
        return factory.create();
    }

    public Identifier getId() {
        return id;
    }

    public Text getTranslationKey() {
        return Text.translatable(id.toTranslationKey("upgrade"));
    }

    public interface UpgradeFactory<T extends Upgrade> {
        T create();
    }
}
