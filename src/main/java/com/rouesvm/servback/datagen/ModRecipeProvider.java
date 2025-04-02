package com.rouesvm.servback.datagen;

import com.rouesvm.servback.items.ContainerItem;
import com.rouesvm.servback.registry.BackpackItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.data.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.recipe.TransmuteRecipeJsonBuilder;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

import static com.rouesvm.servback.Main.MOD_ID;
import static com.rouesvm.servback.datagen.ModItemTags.*;

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

        ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.MISC, BackpackItemRegistry.ENDER_BACKPACK, 1)
                .pattern("#i#")
                .pattern("SES")
                .pattern(" N ")
                .input('#', Items.LEATHER).input('S', Items.STRING)
                .input('i', Items.IRON_INGOT).input('E', ModItemTags.LARGE_BACKPACKS)
                .input('N', Items.ENDER_EYE)
                .criterion("get_obsidian", InventoryChangedCriterion.Conditions.items(Items.OBSIDIAN))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.MISC, BackpackItemRegistry.GLOBAL_BACKPACK, 1)
                .pattern("#i#")
                .pattern("SES")
                .pattern(" N ")
                .input('#', Items.ENDER_EYE).input('S', Items.STRING)
                .input('i', Items.IRON_INGOT).input('E', BackpackItemRegistry.ENDER_BACKPACK)
                .input('N', Items.NETHER_STAR)
                .criterion("get_eye", InventoryChangedCriterion.Conditions.items(Items.ENDER_EYE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.MISC, BackpackItemRegistry.SMALL_BACKPACK, 1)
                .pattern("#S#")
                .pattern("SCS")
                .pattern(" # ")
                .input('#', Items.LEATHER).input('S', Items.STRING).input('C', Items.CHEST)
                .criterion("get_chest", InventoryChangedCriterion.Conditions.items(Items.CHEST))
                .offerTo(exporter);

        dyedBackpackRecipes(itemWrap, exporter);
    }

    private void dyedBackpackRecipes(RegistryWrapper.Impl<Item> itemWrap, RecipeExporter exporter) {
        for (int i = 1; i <= 3; i++) {
            ContainerItem backpack = (ContainerItem) ContainerItem.getDefaultBackpack(i);
            String backpackName = backpack.getIdentifier().getPath();

            for (DyeColor color : DyeColor.values()) {
                ContainerItem regular_dyed_backpack = (ContainerItem) ContainerItem.getColoredBackpack(color, i);
                String name = color.getName().toLowerCase() + "_" + backpackName;
                Item dyeColor = DyeItem.byColor(color);

                switch (i) {
                    case 1 -> createTransmuteRecipe(exporter, itemWrap.getOrThrow(SMALL_BACKPACKS), dyeColor, regular_dyed_backpack, i, name + "_" + i);
                    case 2 -> createTransmuteRecipe(exporter, itemWrap.getOrThrow(MEDIUM_BACKPACKS), dyeColor, regular_dyed_backpack, i, name + "_" + i);
                    case 3 -> createTransmuteRecipe(exporter, itemWrap.getOrThrow(LARGE_BACKPACKS), dyeColor, regular_dyed_backpack, i, name + "_" + i);
                }

                if (i+1 == 4) continue;
                ContainerItem backpackUpATier = (ContainerItem) ContainerItem.getColoredBackpack(color, i + 1);
                createUpgradeRecipe(itemWrap, exporter,
                        regular_dyed_backpack, backpackUpATier,
                        backpackUpATier.getSize(),
                        name + "_" + backpackUpATier.getIdentifier().getPath()
                );
            }
        }
    }

    private void createTransmuteRecipe(RecipeExporter exporter, RegistryEntryList<Item> backpack, Item dyeColor, Item result, int slots, String name) {
        TransmuteRecipeJsonBuilder.create(RecipeCategory.MISC, Ingredient.fromTag(backpack), Ingredient.ofItem(dyeColor), result)
                .group(slots + "_dyedbackpacks")
                .criterion(backpack.toString(), InventoryChangedCriterion.Conditions.items(ContainerItem.getDefaultBackpack(slots)))
                .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(MOD_ID, name)));
    }

    private void createUpgradeRecipe(RegistryWrapper.Impl<Item> itemWrap, RecipeExporter exporter,
                                     Item backpack, Item backpackUpATier,
                                     int slots,
                                     String name) {
        ShapedRecipeJsonBuilder builder = ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.TRANSPORTATION, backpackUpATier)
                .group(slots + "_upgraded")
                .criterion(name, InventoryChangedCriterion.Conditions.items(backpack));

        switch (slots) {
            case 2 -> builder
                    .pattern("iLi")
                    .pattern("SES")
                    .pattern(" O ")
                    .input('L', Items.LEATHER).input('S', Items.STRING)
                    .input('i', Items.IRON_INGOT).input('O', ItemTags.PLANKS)
                    .input('E', backpack)
                    .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(MOD_ID, name)));
            case 3 -> builder
                    .pattern("ZiZ")
                    .pattern("SLS")
                    .input('Z', Items.STRING).input('i', Items.IRON_INGOT)
                    .input('S', Items.SHULKER_SHELL)
                    .input('L', backpack)
                    .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(MOD_ID, name)));
        }
    }

    @Override
    public String getName() {
        return "recipes";
    }
}
