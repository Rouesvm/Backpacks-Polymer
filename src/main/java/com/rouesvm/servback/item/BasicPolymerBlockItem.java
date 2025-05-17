package com.rouesvm.servback.item;

import com.rouesvm.servback.Main;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class BasicPolymerBlockItem extends BlockItem implements PolymerItem, PolymerClientDecoded, PolymerKeepModel {
    private final Identifier id;
    private final PolymerModelData model;

    public BasicPolymerBlockItem(String name, Item vanillaItem, Block block) {
        super(block, new Settings().maxCount(1));
        this.id = Identifier.of(Main.MOD_ID, name);
        this.model = PolymerResourcePackUtils.requestModel(vanillaItem,
                Identifier.of(Main.MOD_ID, "item/" + getIdentifier().getPath()));
    }

    public Identifier getIdentifier() {
        return this.id;
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

