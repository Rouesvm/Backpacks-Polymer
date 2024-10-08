package com.rouesvm.servback.items;

import com.rouesvm.servback.Main;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class BasicPolymerItem extends Item implements PolymerItem, PolymerKeepModel {
    private final String name;
    private final PolymerModelData model;

    public BasicPolymerItem(String name, Item vanillaItem) {
        super(new Settings().maxCount(1));
        this.name = name;
        this.model = PolymerResourcePackUtils.requestModel(vanillaItem,
                Identifier.of(Main.MOD_ID, "item/" + getIdentifier().getPath()));
    }

    public Identifier getIdentifier() {
        return Identifier.of(Main.MOD_ID, this.name);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.model.item();
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.model.value();
    }
}
