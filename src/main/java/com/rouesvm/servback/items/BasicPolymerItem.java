package com.rouesvm.servback.items;

import com.rouesvm.servback.Main;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class BasicPolymerItem extends Item implements PolymerItem, PolymerKeepModel {
    private final Identifier id;
    private final Item vanillaItem;

    public BasicPolymerItem(String name, Item vanillaItem) {
        super(new Settings().maxCount(1).registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Main.MOD_ID, name))));
        this.id = Identifier.of(Main.MOD_ID, name);
        this.vanillaItem = vanillaItem;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return this.vanillaItem;
    }

    @Override
    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return this.id;
    }

    public Identifier getIdentifier() {
        return this.id;
    }
}

