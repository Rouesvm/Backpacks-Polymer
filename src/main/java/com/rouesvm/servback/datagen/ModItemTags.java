package com.rouesvm.servback.datagen;

import com.rouesvm.servback.content.item.BasicPolymerBlockItem;
import com.rouesvm.servback.content.item.ContainerItem;
import com.rouesvm.servback.registry.item.BackpackItemJsonRegistry;
import com.rouesvm.servback.registry.item.BackpackItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

import static com.rouesvm.servback.ServerBackpacks.MOD_ID;

public class ModItemTags extends FabricTagProvider.ItemTagProvider {
    public static final TagKey<Item> SMALL_BACKPACKS = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "small_backpacks"));
    public static final TagKey<Item> MEDIUM_BACKPACKS = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "medium_backpacks"));
    public static final TagKey<Item> LARGE_BACKPACKS = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "large_backpacks"));

    public static final TagKey<Item> SUPPORTED_BACKPACKS = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "supported_backpacks"));
    public static final TagKey<Item> BACKPACKS = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "backpacks"));

    public ModItemTags(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        for (int i = 1; i <= 3; i++) {
            for (DyeColor color : DyeColor.values()) {
                ContainerItem item = (ContainerItem) BackpackItemJsonRegistry.getBackpackByOrder(color, i);
                Identifier itemID = item.getIdentifier();
                switch (i) {
                    case 1 -> this.getTagBuilder(SMALL_BACKPACKS).addOptional(itemID);
                    case 2 -> this.getTagBuilder(MEDIUM_BACKPACKS).addOptional(itemID);
                    case 3 -> this.getTagBuilder(LARGE_BACKPACKS).addOptional(itemID);
                }
            }
        }

        this.getTagBuilder(BACKPACKS).addOptional(((BasicPolymerBlockItem) BackpackItemRegistry.GLOBAL_BACKPACK).getIdentifier());
        this.getTagBuilder(BACKPACKS).addOptional(((BasicPolymerBlockItem) BackpackItemRegistry.ENDER_BACKPACK).getIdentifier());

        this.getTagBuilder(BACKPACKS).addOptionalTag(SMALL_BACKPACKS.id());
        this.getTagBuilder(BACKPACKS).addOptionalTag(MEDIUM_BACKPACKS.id());
        this.getTagBuilder(BACKPACKS).addOptionalTag(LARGE_BACKPACKS.id());

        this.getTagBuilder(SUPPORTED_BACKPACKS).addOptionalTag(MEDIUM_BACKPACKS.id());
        this.getTagBuilder(SUPPORTED_BACKPACKS).addOptionalTag(LARGE_BACKPACKS.id());
    }
}
