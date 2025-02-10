package com.rouesvm.servback.datagen;

import com.rouesvm.servback.items.ContainerItem;
import com.rouesvm.servback.items.ItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.data.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.DyeColor;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup wrapperLookup, RecipeExporter recipeExporter) {
        return new RecipeGenerator(wrapperLookup, recipeExporter) {
            @Override
            public void generate() {
                itemRecipes(wrapperLookup, exporter);
            }
        };
    }

    private void itemRecipes(RegistryWrapper.WrapperLookup wrapperLookup, RecipeExporter exporter) {
        RegistryWrapper.Impl<Item> itemWrap = wrapperLookup.getOrThrow(RegistryKeys.ITEM);

        ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.MISC, ItemRegistry.SMALL_BACKPACK, 1)
                .pattern("#i#")
                .pattern("SES")
                .pattern(" N ")
                .input('#', Items.LEATHER).input('S', Items.STRING)
                .input('i', Items.IRON_INGOT).input('E', ModItemTags.LARGE_BACKPACKS)
                .input('N', Items.ENDER_EYE)
                .criterion("get_chest", InventoryChangedCriterion.Conditions.items(Items.OBSIDIAN))
                .offerTo(exporter, "enderpack");

        ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.MISC, ItemRegistry.SMALL_BACKPACK, 1)
                .pattern("#i#")
                .pattern("SES")
                .pattern(" N ")
                .input('#', Items.ENDER_EYE).input('S', Items.STRING)
                .input('i', Items.IRON_INGOT).input('E', ItemRegistry.ENDER_BACKPACK)
                .input('N', Items.NETHER_STAR)
                .criterion("get_chest", InventoryChangedCriterion.Conditions.items(Items.ENDER_EYE))
                .offerTo(exporter, "globalpack");

        ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.MISC, ItemRegistry.SMALL_BACKPACK, 1)
                .pattern("#S#")
                .pattern("SCS")
                .pattern(" # ")
                .input('#', Items.LEATHER).input('S', Items.STRING).input('C', Items.CHEST)
                .criterion("get_chest", InventoryChangedCriterion.Conditions.items(Items.CHEST))
                .offerTo(exporter, "smallpack");

        dyedBackpackRecipes(itemWrap, exporter);
    }

    private void dyedBackpackRecipes(RegistryWrapper.Impl<Item> itemWrap, RecipeExporter exporter) {
        for (int i = 1; i <= 3; i++) {
            ContainerItem backpack = (ContainerItem) ContainerItem.getDefaultBackpack(i);
            String backpackName = backpack.getIdentifier().getPath();

            for (DyeColor color : DyeColor.values()) {
                ContainerItem backpackUpATier = (ContainerItem) ContainerItem.getColoredBackpack(color, i + 1);
                int slots = backpackUpATier.getSize();
                String tierUpBackpackName = "_" + backpackUpATier.getIdentifier().getPath();

                String name = color.getName().toLowerCase() + "_" + backpackName;
                Item dyeColor = DyeItem.byColor(color);

                createTransmuteRecipe(itemWrap, exporter, backpack, dyeColor, ContainerItem.getColoredBackpack(color, i), name);

                if (i+1 == 4) continue;
                createUpgradeRecipe(itemWrap, exporter, ContainerItem.getColoredBackpack(color, i), backpackUpATier, slots, name, tierUpBackpackName);
            }
        }
    }

    private void createTransmuteRecipe(RegistryWrapper.Impl<Item> itemWrap, RecipeExporter exporter, Item backpack, Item dyeColor, Item result, String name) {
        ShapelessRecipeJsonBuilder.create(itemWrap, RecipeCategory.MISC, result)
                .input(backpack)
                .input(dyeColor)
                .group(name)
                .criterion(name, InventoryChangedCriterion.Conditions.items(ItemRegistry.SMALL_BACKPACK))
                .offerTo(exporter, name);
    }

    private void createUpgradeRecipe(RegistryWrapper.Impl<Item> itemWrap, RecipeExporter exporter,
                                     Item backpack, Item backpackUpATier,
                                     int slots,
                                     String name,
                                     String tierUpBackpackName) {
        ShapedRecipeJsonBuilder builder = ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.TRANSPORTATION, backpackUpATier)
                .group(name)
                .criterion(name + tierUpBackpackName, InventoryChangedCriterion.Conditions.items(backpack));

        switch (slots) {
            case 2 -> builder
                    .pattern("iLi")
                    .pattern("SES")
                    .pattern(" O ")
                    .input('L', Items.LEATHER).input('S', Items.STRING)
                    .input('i', Items.IRON_INGOT).input('O', ItemTags.PLANKS)
                    .input('E', backpack)
                    .offerTo(exporter, name + tierUpBackpackName);
            case 3 -> builder
                    .pattern("ZiZ")
                    .pattern("SLS")
                    .input('Z', Items.STRING).input('i', Items.IRON_INGOT)
                    .input('S', Items.SHULKER_SHELL)
                    .input('L', backpack)
                    .offerTo(exporter, name + tierUpBackpackName);
        }
    }

    @Override
    public String getName() {
        return "recipes";
    }
}
