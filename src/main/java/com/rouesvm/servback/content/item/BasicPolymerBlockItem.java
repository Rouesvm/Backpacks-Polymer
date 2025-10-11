package com.rouesvm.servback.content.item;

import com.rouesvm.servback.ServerBackpacks;
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

    private final PolymerModelData customModelId;
    private final PolymerModelData customModelId3D;

    private final Item vanillaItem;

    public BasicPolymerBlockItem(String name, Item vanillaItem, Block block) {
        super(block, new Settings().maxCount(1));
        this.id = Identifier.of(ServerBackpacks.MOD_ID, name);
        this.vanillaItem = vanillaItem;
        this.customModelId = PolymerResourcePackUtils.requestModel(vanillaItem,
                Identifier.of(ServerBackpacks.MOD_ID, "item/" + getIdentifier().getPath()));
        this.customModelId3D = PolymerResourcePackUtils.requestModel(vanillaItem,
                Identifier.of(ServerBackpacks.MOD_ID, "item/model/" + getIdentifier().getPath()));

    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity serverPlayerEntity) {
        return this.vanillaItem;
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return customModelId.value();
    }

    public Identifier getIdentifier() {
        return this.id;
    }
}

