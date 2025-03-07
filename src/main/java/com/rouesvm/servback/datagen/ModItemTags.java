package com.rouesvm.servback.datagen;

import com.rouesvm.servback.items.ContainerItem;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

import static com.rouesvm.servback.Main.MOD_ID;

public class ModItemTags extends FabricTagProvider.ItemTagProvider {
    public static final TagKey<Item> SMALL_BACKPACKS = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "small_backpacks"));
    public static final TagKey<Item> MEDIUM_BACKPACKS = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "medium_backpacks"));
    public static final TagKey<Item> LARGE_BACKPACKS = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "large_backpacks"));

    public ModItemTags(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {

        for (int i = 1; i <= 3; i++) {
            for (DyeColor color : DyeColor.values()) {
                ContainerItem item = (ContainerItem) ContainerItem.getColoredBackpack(color, i);
                switch (i) {
                    case 1 -> this.getOrCreateTagBuilder(SMALL_BACKPACKS).add(item);
                    case 2 -> this.getOrCreateTagBuilder(MEDIUM_BACKPACKS).add(item);
                    case 3 -> this.getOrCreateTagBuilder(LARGE_BACKPACKS).add(item);
                }
            }
        }
    }
}
