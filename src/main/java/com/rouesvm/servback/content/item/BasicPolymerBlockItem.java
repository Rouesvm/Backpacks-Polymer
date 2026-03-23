package com.rouesvm.servback.content.item;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.compat.geyser.bedrock.BedrockItem;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class BasicPolymerBlockItem extends BlockItem implements PolymerItem, PolymerClientDecoded, BedrockItem {
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
        if (ServerBackpacks.isBedrock(context.orElse(PacketContext.GAME_PROFILE, ServerBackpacks.NIL).id()))
            return this;
        else return this.vanillaItem;
    }

    @Override
    public @org.jspecify.annotations.Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        return this.id;
    }

    public Identifier getIdentifier() {
        return this.id;
    }
}

