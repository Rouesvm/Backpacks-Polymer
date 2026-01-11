package com.rouesvm.servback.content.item;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.compat.geyser.bedrock.BedrockItem;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class BasicPolymerBlockItem extends BlockItem implements PolymerItem, PolymerClientDecoded, PolymerKeepModel, BedrockItem {
    private final Identifier id;
    private final Item vanillaItem;

    public BasicPolymerBlockItem(String name, Item vanillaItem, Block block) {
        super(block, new Properties()
                .stacksTo(1)
                .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ServerBackpacks.MOD_ID, name)))
        );
        this.id = Identifier.fromNamespaceAndPath(ServerBackpacks.MOD_ID, name);
        this.vanillaItem = vanillaItem;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        if (ServerBackpacks.isBedrock(context.getPlayer()))
            return this;
        else return this.vanillaItem;
    }

    @Override
    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return this.id;
    }

    public Identifier getIdentifier() {
        return this.id;
    }
}

