package com.rouesvm.servback.datagen;

import com.rouesvm.servback.content.item.ContainerItem;
import com.rouesvm.servback.content.registry.item.BackpackItemJsonRegistry;
import com.rouesvm.servback.content.registry.item.BackpackItemRegistry;
import com.rouesvm.servback.technical.crafting.BackpackRecipeJsonBuilder;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.DyeColor;

import java.util.concurrent.CompletableFuture;

import static com.rouesvm.servback.datagen.ModItemTags.*;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        itemRecipes(exporter);
    }

    private void itemRecipes(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, BackpackItemRegistry.ENDER_BACKPACK, 1)
                .pattern("#i#")
                .pattern("SES")
                .pattern(" N ")
                .input('#', Items.LEATHER).input('S', Items.STRING)
                .input('i', Items.IRON_INGOT).input('E', ModItemTags.LARGE_BACKPACKS)
                .input('N', Items.ENDER_EYE)
                .criterion(FabricRecipeProvider.hasItem(Items.OBSIDIAN), FabricRecipeProvider.conditionsFromItem(Items.OBSIDIAN))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, BackpackItemRegistry.GLOBAL_BACKPACK, 1)
                .pattern("#i#")
                .pattern("SES")
                .pattern(" N ")
                .input('#', Items.ENDER_EYE).input('S', Items.STRING)
                .input('i', Items.IRON_INGOT).input('E', BackpackItemRegistry.ENDER_BACKPACK)
                .input('N', Items.NETHER_STAR)
                .criterion(FabricRecipeProvider.hasItem(Items.ENDER_EYE), FabricRecipeProvider.conditionsFromItem(Items.ENDER_EYE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, BackpackItemJsonRegistry.getBackpackByOrder(1), 1)
                .pattern("#S#")
                .pattern("SCS")
                .pattern(" # ")
                .input('#', Items.LEATHER).input('S', Items.STRING).input('C', Items.CHEST)
                .criterion(FabricRecipeProvider.hasItem(Items.CHEST), FabricRecipeProvider.conditionsFromItem(Items.CHEST))
                .offerTo(exporter);

        BackpackRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, BackpackItemJsonRegistry.getBackpackByOrder(2), 1)
                .criterion("get_chest", InventoryChangedCriterion.Conditions.items(Items.CHEST))
                .pattern("iLi")
                .pattern("S0S")
                .pattern(" O ")
                .input('L', Items.LEATHER).input('S', Items.STRING)
                .input('i', Items.IRON_INGOT).input('O', ItemTags.PLANKS)
                .input('0', Ingredient.fromTag(SMALL_BACKPACKS))
                .offerTo(exporter, "medium");

        BackpackRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, BackpackItemJsonRegistry.getBackpackByOrder(3), 1)
                .criterion("get_chest", InventoryChangedCriterion.Conditions.items(Items.CHEST))
                .pattern("ZiZ")
                .pattern("S0S")
                .input('Z', Items.STRING).input('i', Items.IRON_INGOT)
                .input('S', Items.SHULKER_SHELL)
                .input('0', Ingredient.fromTag(MEDIUM_BACKPACKS))
                .offerTo(exporter, "large");

        dyedBackpackRecipes(exporter);
    }

    private void dyedBackpackRecipes(net.minecraft.data.server.recipe.RecipeExporter exporter) {
        for (int i = 1; i <= 3; i++) {
            ContainerItem baseBackpack = (ContainerItem) BackpackItemJsonRegistry.getBackpackByOrder(i);
            TagKey<Item> matchingBackpacks = switch (i) {
                case 1 -> SMALL_BACKPACKS;
                case 2 -> MEDIUM_BACKPACKS;
                case 3 -> LARGE_BACKPACKS;
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

    private void createTransmuteRecipe(RecipeExporter exporter, TagKey<Item> backpack, Item dyeColor, Item result, int tier, String name) {
        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, result)
                .input(Ingredient.fromTag(backpack))
                .input(Ingredient.ofItems(dyeColor))
                .group(tier + "_dyedbackpacks")
                .criterion(backpack.toString(), InventoryChangedCriterion.Conditions.items(BackpackItemJsonRegistry.getBackpackByOrder(tier)))
                .offerTo(exporter, name);
    }


    @Override
    public String getName() {
        return "recipes";
    }
}
