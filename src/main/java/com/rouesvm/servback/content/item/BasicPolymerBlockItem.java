package com.rouesvm.servback.content.item;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.compat.geyser.bedrock.BedrockItem;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class BasicPolymerBlockItem extends BlockItem implements PolymerItem, PolymerClientDecoded, PolymerKeepModel, BedrockItem {
    private final Identifier id;
    private final Item vanillaItem;

    public BasicPolymerBlockItem(String name, Item vanillaItem, Block block) {
        super(block, new Settings()
                .maxCount(1)
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ServerBackpacks.MOD_ID, name)))
        );
        this.id = Identifier.of(ServerBackpacks.MOD_ID, name);
        this.vanillaItem = vanillaItem;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        if (ServerBackpacks.isBedrock(context.getPlayer()))
            return this;
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

