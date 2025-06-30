package com.rouesvm.servback.datagen;

import com.rouesvm.servback.content.item.ContainerItem;
import com.rouesvm.servback.content.registry.item.BackpackItemJsonRegistry;
import com.rouesvm.servback.content.registry.item.BackpackItemRegistry;
import com.rouesvm.servback.technical.crafting.BackpackRecipeJsonBuilder;
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

import static com.rouesvm.servback.ServerBackpacks.MOD_ID;
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

    @Override
    protected Identifier getRecipeIdentifier(Identifier identifier) {
        return Identifier.of(MOD_ID, identifier.getPath());
    }

    private void itemRecipes(RegistryWrapper.WrapperLookup wrapperLookup, RecipeExporter exporter) {
        RegistryWrapper.Impl<Item> itemWrap = wrapperLookup.getOrThrow(RegistryKeys.ITEM);

        ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.MISC, BackpackItemRegistry.ENDER_BACKPACK, 1)
                .pattern("#i#")
                .pattern("SES")
                .pattern(" N ")
                .input('#', Items.LEATHER).input('S', Items.STRING)
                .input('i', Items.IRON_INGOT).input('E', LARGE_BACKPACKS)
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

        BackpackRecipeJsonBuilder.create(itemWrap, RecipeCategory.MISC, BackpackItemJsonRegistry.getBackpackByOrder(1), 1)
                .pattern("#S#")
                .pattern("SCS")
                .pattern(" # ")
                .input('#', Items.LEATHER).input('S', Items.STRING).input('C', Items.CHEST)
                .criterion("get_chest", InventoryChangedCriterion.Conditions.items(Items.CHEST))
                .offerTo(exporter);

        BackpackRecipeJsonBuilder.create(itemWrap, RecipeCategory.TRANSPORTATION, BackpackItemJsonRegistry.getBackpackByOrder(2), 1)
                .criterion("get_chest", InventoryChangedCriterion.Conditions.items(Items.CHEST))
                .pattern("iLi")
                .pattern("S0S")
                .pattern(" O ")
                .input('L', Items.LEATHER).input('S', Items.STRING)
                .input('i', Items.IRON_INGOT).input('O', ItemTags.PLANKS)
                .input('0', Ingredient.ofTag(itemWrap.getOrThrow(SMALL_BACKPACKS)))
                .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(MOD_ID, "medium_backpack")));

        BackpackRecipeJsonBuilder.create(itemWrap, RecipeCategory.TRANSPORTATION, BackpackItemJsonRegistry.getBackpackByOrder(3), 1)
                .criterion("get_chest", InventoryChangedCriterion.Conditions.items(Items.CHEST))
                .pattern("ZiZ")
                .pattern("S0S")
                .input('Z', Items.STRING).input('i', Items.IRON_INGOT)
                .input('S', Items.SHULKER_SHELL)
                .input('0', Ingredient.ofTag(itemWrap.getOrThrow(MEDIUM_BACKPACKS)))
                .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(MOD_ID, "large_backpack")));

        BackpackRecipeJsonBuilder.create(itemWrap, RecipeCategory.TRANSPORTATION, BackpackItemRegistry.getBackpack(DyeColor.BROWN, 2))
                .criterion("get_chest", InventoryChangedCriterion.Conditions.items(Items.CHEST))
                .pattern("iLi")
                .pattern("S0S")
                .pattern(" O ")
                .input('L', Items.LEATHER).input('S', Items.STRING)
                .input('i', Items.IRON_INGOT).input('O', ItemTags.PLANKS)
                .input('0', Ingredient.fromTag(itemWrap.getOrThrow(SMALL_BACKPACKS)))
                .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(MOD_ID, "medium_backpack")));

        BackpackRecipeJsonBuilder.create(itemWrap, RecipeCategory.TRANSPORTATION, BackpackItemRegistry.getBackpack(DyeColor.BROWN, 3))
                .criterion("get_chest", InventoryChangedCriterion.Conditions.items(Items.CHEST))
                .pattern("ZiZ")
                .pattern("S0S")
                .input('Z', Items.STRING).input('i', Items.IRON_INGOT)
                .input('S', Items.SHULKER_SHELL)
                .input('0', Ingredient.fromTag(itemWrap.getOrThrow(MEDIUM_BACKPACKS)))
                .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(MOD_ID, "large_backpack")));

        dyedBackpackRecipes(itemWrap, exporter);
    }

    private void dyedBackpackRecipes(RegistryWrapper.Impl<Item> itemWrap, RecipeExporter exporter) {
        for (int i = 1; i <= 3; i++) {
            ContainerItem baseBackpack = (ContainerItem) BackpackItemJsonRegistry.getBackpackByOrder(i);
            RegistryEntryList<Item> matchingBackpacks = switch (i) {
                case 1 -> itemWrap.getOrThrow(SMALL_BACKPACKS);
                case 2 -> itemWrap.getOrThrow(MEDIUM_BACKPACKS);
                case 3 -> itemWrap.getOrThrow(LARGE_BACKPACKS);
                default -> throw new IllegalStateException("Unexpected value: " + i);
            };

            for (DyeColor color : DyeColor.values()) {
                ContainerItem dyedBackpack = (ContainerItem) BackpackItemJsonRegistry.getBackpackByOrder(color, i);
                String dyeName = color.name().toLowerCase();
                Item dye = DyeItem.byColor(color);

                String transmuteId = String.format("%s_%s_%d", dyeName, baseBackpack.getIdentifier().getPath(), i);
                createTransmuteRecipe(exporter, matchingBackpacks, dye, dyedBackpack, i, transmuteId);
            }
        }
    }

    private void createTransmuteRecipe(RecipeExporter exporter, RegistryEntryList<Item> backpack, Item dyeColor, Item result, int tier, String name) {
        TransmuteRecipeJsonBuilder.create(RecipeCategory.MISC, Ingredient.fromTag(backpack), Ingredient.ofItem(dyeColor), result)
                .group(tier + "_dyedbackpacks")
                .criterion(backpack.toString(), InventoryChangedCriterion.Conditions.items(BackpackItemJsonRegistry.getBackpackByOrder(tier)))
                .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(MOD_ID, name)));
    }

    @Override
    public String getName() {
        return "recipes";
    }
}
