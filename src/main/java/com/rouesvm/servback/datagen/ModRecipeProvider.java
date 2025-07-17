package com.rouesvm.servback.datagen;

import com.rouesvm.servback.content.registry.item.BackpackItemJsonRegistry;
import com.rouesvm.servback.content.registry.item.BackpackItemRegistry;
import com.rouesvm.servback.technical.crafting.BackpackRecipeJsonBuilder;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

import static com.rouesvm.servback.datagen.ModItemTags.MEDIUM_BACKPACKS;
import static com.rouesvm.servback.datagen.ModItemTags.SMALL_BACKPACKS;

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
    }

    @Override
    public String getName() {
        return "recipes";
    }
}
