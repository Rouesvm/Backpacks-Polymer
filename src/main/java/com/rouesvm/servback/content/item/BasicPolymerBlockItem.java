package com.rouesvm.servback.content.item;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class BasicPolymerBlockItem extends BlockItem implements PolymerItem, PolymerClientDecoded, PolymerKeepModel {
    private final Identifier id;
    private final PolymerModelData model;
    private final PolymerModelData model_3D;

    public BasicPolymerBlockItem(String name, Item vanillaItem, Block block) {
        super(block, new Settings().maxCount(1));
        this.id = Identifier.of(Main.MOD_ID, name);
        this.model = PolymerResourcePackUtils.requestModel(vanillaItem,
                Identifier.of(Main.MOD_ID, "item/" + getIdentifier().getPath()));
        this.model_3D = PolymerResourcePackUtils.requestModel(vanillaItem,
                Identifier.of(Main.MOD_ID, "item/model/" + getIdentifier().getPath()));
    }

    public String getTranslationKey() {
        return this.getOrCreateTranslationKey();
    }

    public Identifier getIdentifier() {
        return this.id;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        if (itemStack.getOrDefault(BackpackDataComponentTypes.EQUIPPED_TYPE, false))
            return this.model_3D.item();
        return this.model.item();
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        if (itemStack.getOrDefault(BackpackDataComponentTypes.EQUIPPED_TYPE, false))
            return this.model_3D.value();
        return this.model.value();
    }
}

