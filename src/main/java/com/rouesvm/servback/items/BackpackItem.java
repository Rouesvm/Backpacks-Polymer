package com.rouesvm.servback.items;

import com.rouesvm.servback.Main;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class BackpackItem extends ContainerItem implements PolymerItem, PolymerKeepModel {

    private final PolymerModelData model;

    public BackpackItem(String id, int slots) {
        super(id, slots);
        this.model = PolymerResourcePackUtils.requestModel(Items.LEATHER,
                Identifier.of(Main.MOD_ID + "item/"
                        + Registries.ITEM.getId(Items.LEATHER).getPath().replace("shovel", id)));
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
