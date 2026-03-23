package com.rouesvm.servback.datagen;

import com.rouesvm.servback.content.item.BasicPolymerBlockItem;
import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.registry.item.BackpackItemJsonRegistry;
import com.rouesvm.servback.registry.item.BackpackItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.rouesvm.servback.ServerBackpacks.MOD_ID;

public class ModItemTags extends FabricTagProvider.ItemTagProvider {
    public static final TagKey<Item> SMALL_BACKPACKS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "small_backpacks"));
    public static final TagKey<Item> MEDIUM_BACKPACKS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "medium_backpacks"));
    public static final TagKey<Item> LARGE_BACKPACKS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "large_backpacks"));

    public static final TagKey<Item> BLACKLISTED = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "blacklisted"));
    public static final TagKey<Item> SUPPORTED_BACKPACKS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "supported_backpacks"));
    public static final TagKey<Item> UPGRADABLE_BACKPACKS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "upgradeable_backpacks"));

    public static final TagKey<Item> BACKPACKS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "backpacks"));

    public ModItemTags(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        Set<Item> small = BackpackItemJsonRegistry.getBackpacksByName("small");
        small.forEach((item) -> {
            Identifier itemID = ((ContainerItem) item).getIdentifier();
            this.getOrCreateRawBuilder(SMALL_BACKPACKS).addOptionalElement(itemID);
        });

        Set<Item> medium = BackpackItemJsonRegistry.getBackpacksByName("medium");
        medium.forEach((item) -> {
            Identifier itemID = ((ContainerItem) item).getIdentifier();
            this.getOrCreateRawBuilder(MEDIUM_BACKPACKS).addOptionalElement(itemID);
        });

        Set<Item> large = BackpackItemJsonRegistry.getBackpacksByName("large");
        large.forEach((item) -> {
            Identifier itemID = ((ContainerItem) item).getIdentifier();
            this.getOrCreateRawBuilder(LARGE_BACKPACKS).addOptionalElement(itemID);
        });

        this.getOrCreateRawBuilder(BACKPACKS).addOptionalElement(((BasicPolymerBlockItem) BackpackItemRegistry.GLOBAL_BACKPACK).getIdentifier());
        this.getOrCreateRawBuilder(BACKPACKS).addOptionalElement(((BasicPolymerBlockItem) BackpackItemRegistry.ENDER_BACKPACK).getIdentifier());
        this.getOrCreateRawBuilder(BACKPACKS).addOptionalElement(((BasicPolymerBlockItem) BackpackItemRegistry.LAVA_BACKPACK).getIdentifier());

        this.getOrCreateRawBuilder(BLACKLISTED).addOptionalElement(((BasicPolymerBlockItem) BackpackItemRegistry.ENDER_BACKPACK).getIdentifier());

        this.getOrCreateRawBuilder(BACKPACKS).addOptionalTag(SMALL_BACKPACKS.location());
        this.getOrCreateRawBuilder(BACKPACKS).addOptionalTag(MEDIUM_BACKPACKS.location());
        this.getOrCreateRawBuilder(BACKPACKS).addOptionalTag(LARGE_BACKPACKS.location());

        this.getOrCreateRawBuilder(SUPPORTED_BACKPACKS).addOptionalTag(MEDIUM_BACKPACKS.location());
        this.getOrCreateRawBuilder(SUPPORTED_BACKPACKS).addOptionalTag(LARGE_BACKPACKS.location());

        this.getOrCreateRawBuilder(UPGRADABLE_BACKPACKS).addElement(((BasicPolymerBlockItem) BackpackItemRegistry.GLOBAL_BACKPACK).getIdentifier());
        this.getOrCreateRawBuilder(UPGRADABLE_BACKPACKS).addOptionalTag(SUPPORTED_BACKPACKS.location());
    }
}
