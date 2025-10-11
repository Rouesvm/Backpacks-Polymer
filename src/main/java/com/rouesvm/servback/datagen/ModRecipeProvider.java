package com.rouesvm.servback.datagen;

import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.content.recipe.builder.BackpackRecipeJsonBuilder;
import com.rouesvm.servback.content.recipe.builder.BackpackUpgradeRecipeJsonBuilder;
import com.rouesvm.servback.registry.item.BackpackItemJsonRegistry;
import com.rouesvm.servback.registry.item.BackpackItemRegistry;
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
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
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
    public void generate(RecipeExporter exporter) {
        itemRecipes(exporter);
    }

    private void itemRecipes(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, BackpackItemRegistry.ENDER_BACKPACK, 1)
                .pattern(" # ")
                .pattern("SES")
                .pattern(" N ")
                .input('#', Items.STRING).input('S', Items.OBSIDIAN)
                .input('E', LARGE_BACKPACKS).input('N', Items.ENDER_EYE)
                .criterion("get_obsidian", InventoryChangedCriterion.Conditions.items(Items.OBSIDIAN))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, BackpackItemRegistry.GLOBAL_BACKPACK, 1)
                .pattern("#i#")
                .pattern("SES")
                .pattern(" N ")
                .input('#', Items.ENDER_EYE).input('S', Items.STRING)
                .input('i', Items.IRON_INGOT).input('E', LARGE_BACKPACKS)
                .input('N', Items.NETHER_STAR)
                .criterion("get_eye", InventoryChangedCriterion.Conditions.items(Items.ENDER_EYE))
                .offerTo(exporter);

        BackpackRecipeJsonBuilder.create(RecipeCategory.MISC, BackpackItemJsonRegistry.getBackpackByOrder(1), 1)
                .pattern("SiS")
                .pattern("#C#")
                .pattern(" S ")
                .input('#', Items.LEATHER).input('S', Items.STRING)
                .input('i', Items.COPPER_INGOT).input('C', Items.CHEST)
                .criterion("get_chest", InventoryChangedCriterion.Conditions.items(Items.CHEST))
                .offerTo(exporter);

        BackpackRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, BackpackItemJsonRegistry.getBackpackByOrder(2), 1)
                .criterion("get_chest", InventoryChangedCriterion.Conditions.items(Items.CHEST))
                .pattern("iLi")
                .pattern("S0S")
                .pattern("LOL")
                .input('L', Items.LEATHER).input('S', Items.STRING)
                .input('i', Items.GOLD_INGOT).input('O', ItemTags.PLANKS)
                .input('0', Ingredient.fromTag(SMALL_BACKPACKS))
                .offerTo(exporter, Identifier.of(MOD_ID, "medium_backpack"));

        BackpackRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, BackpackItemJsonRegistry.getBackpackByOrder(3), 1)
                .criterion("get_chest", InventoryChangedCriterion.Conditions.items(Items.CHEST))
                .pattern("LSL")
                .pattern("i0i")
                .pattern("SBS")
                .input('S', Items.STRING).input('i', Items.IRON_INGOT)
                .input('B', Items.ENDER_EYE).input('L', Items.LEATHER)
                .input('0', Ingredient.fromTag(MEDIUM_BACKPACKS))
                .offerTo(exporter, Identifier.of(MOD_ID, "large_backpack"));

        upgradeRecipes(exporter);
        dyedBackpackRecipes(exporter);
    }

    private void upgradeRecipes(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, BackpackItemRegistry.VOID_UPGRADE, 1)
                .pattern("#L#")
                .pattern("KEK")
                .pattern("#L#")
                .input('#', Items.STRING)
                .input('L', Items.LEATHER)
                .input('E', Items.ENDER_PEARL).input('K', Items.LAVA_BUCKET)
                .criterion("get_leather", InventoryChangedCriterion.Conditions.items(Items.LEATHER))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, BackpackItemRegistry.MAGNET_UPGRADE, 1)
                .pattern("#S#")
                .pattern("LEL")
                .pattern("#S#")
                .input('#', Items.IRON_INGOT).input('S', Items.STRING)
                .input('L', Items.LEATHER).input('E', Items.ENDER_PEARL)
                .criterion("get_leather", InventoryChangedCriterion.Conditions.items(Items.LEATHER))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, BackpackItemRegistry.CRAFTING_UPGRADE, 1)
                .pattern("#L#")
                .pattern("LEL")
                .pattern("#L#")
                .input('#', ItemTags.PLANKS).input('L', Items.LEATHER)
                .input('E', Items.CRAFTING_TABLE)
                .criterion("get_leather", InventoryChangedCriterion.Conditions.items(Items.LEATHER))
                .offerTo(exporter);

        BackpackUpgradeRecipeJsonBuilder.create(
                        Ingredient.fromTag(SUPPORTED_BACKPACKS),
                        Ingredient.ofItems(BackpackItemRegistry.VOID_UPGRADE),
                        RecipeCategory.TOOLS,
                        BackpackItemJsonRegistry.getBackpackByOrder(1))
                .criterion("get_chest", InventoryChangedCriterion.Conditions.items(Items.CHEST))
                .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(MOD_ID, "void_upgrade_backpack")));

        BackpackUpgradeRecipeJsonBuilder.create(
                        Ingredient.fromTag(SUPPORTED_BACKPACKS),
                        Ingredient.ofItems(BackpackItemRegistry.MAGNET_UPGRADE),
                        RecipeCategory.TOOLS,
                        BackpackItemJsonRegistry.getBackpackByOrder(1))
                .criterion("get_chest", InventoryChangedCriterion.Conditions.items(Items.CHEST))
                .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(MOD_ID, "magnet_upgrade_backpack")));

        BackpackUpgradeRecipeJsonBuilder.create(
                        Ingredient.fromTag(SUPPORTED_BACKPACKS),
                        Ingredient.ofItems(BackpackItemRegistry.CRAFTING_UPGRADE),
                        RecipeCategory.TOOLS,
                        BackpackItemJsonRegistry.getBackpackByOrder(1))
                .criterion("get_chest", InventoryChangedCriterion.Conditions.items(Items.CHEST))
                .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(MOD_ID, "crafting_upgrade_backpack")));
    }

    private void dyedBackpackRecipes(RecipeExporter exporter) {
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
                .input(Ingredient.ofItems(dyeColor))
                .input(Ingredient.fromTag(backpack))
                .group(tier + "_dyedbackpacks")
                .criterion(backpack.toString(), InventoryChangedCriterion.Conditions.items(BackpackItemJsonRegistry.getBackpackByOrder(tier)))
                .offerTo(exporter, name);
    }
    
    @Override
    public String getName() {
        return "recipes";
    }
}
