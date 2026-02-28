package com.rouesvm.servback.datagen;

import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.content.recipe.builder.BackpackRecipeJsonBuilder;
import com.rouesvm.servback.content.recipe.builder.BackpackUpgradeRecipeJsonBuilder;
import com.rouesvm.servback.content.recipe.builder.TransferNBTJsonBuilder;
import com.rouesvm.servback.registry.item.BackpackItemJsonRegistry;
import com.rouesvm.servback.registry.item.BackpackItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

import static com.rouesvm.servback.ServerBackpacks.MOD_ID;
import static com.rouesvm.servback.datagen.ModItemTags.*;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
protected @NonNull RecipeProvider createRecipeProvider(HolderLookup.Provider wrapperLookup, RecipeOutput recipeExporter) {
        return new RecipeProvider(wrapperLookup, recipeExporter) {
            @Override
            public void buildRecipes() {
                itemRecipes(wrapperLookup, output);
            }
        };
    }

    @Override
    protected @NonNull Identifier getRecipeIdentifier(Identifier identifier) {
        return Identifier.fromNamespaceAndPath(MOD_ID, identifier.getPath());
    }

    private void itemRecipes(HolderLookup.Provider wrapperLookup, RecipeOutput exporter) {
        HolderLookup.RegistryLookup<Item> itemWrap = wrapperLookup.lookupOrThrow(Registries.ITEM);

        ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.MISC, BackpackItemRegistry.ENDER_BACKPACK, 1)
                .pattern(" # ")
                .pattern("SES")
                .pattern(" N ")
                .define('#', Items.STRING).define('S', Items.OBSIDIAN)
                .define('E', LARGE_BACKPACKS).define('N', Items.ENDER_EYE)
                .unlockedBy("get_obsidian", InventoryChangeTrigger.TriggerInstance.hasItems(Items.OBSIDIAN))
                .save(exporter);

        TransferNBTJsonBuilder.create(itemWrap, RecipeCategory.MISC, BackpackItemRegistry.GLOBAL_BACKPACK, 1)
                .pattern("#i#")
                .pattern("SES")
                .pattern(" N ")
                .input('#', Items.ENDER_EYE).input('S', Items.STRING)
                .input('i', Items.IRON_INGOT).input('E', LARGE_BACKPACKS)
                .input('N', Items.NETHER_STAR)
                .unlockedBy("get_eye", InventoryChangeTrigger.TriggerInstance.hasItems(Items.ENDER_EYE))
                .save(exporter);

        ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.MISC, BackpackItemJsonRegistry.getBackpackByName("small"), 1)
                .pattern("SiS")
                .pattern("#C#")
                .pattern(" S ")
                .define('#', Items.LEATHER).define('S', Items.STRING)
                .define('i', Items.COPPER_INGOT).define('C', Items.CHEST)
                .unlockedBy("get_chest", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CHEST))
                .save(exporter);

        BackpackRecipeJsonBuilder.create(itemWrap, RecipeCategory.TRANSPORTATION, BackpackItemJsonRegistry.getBackpackByName("medium"), 1)
                .unlockedBy("get_chest", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CHEST))
                .pattern("iLi")
                .pattern("S0S")
                .pattern("LOL")
                .input('L', Items.LEATHER).input('S', Items.STRING)
                .input('i', Items.GOLD_INGOT).input('O', ItemTags.PLANKS)
                .input('0', Ingredient.of(itemWrap.getOrThrow(SMALL_BACKPACKS)))
                .save(exporter, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(MOD_ID, "medium_backpack")));

        BackpackRecipeJsonBuilder.create(itemWrap, RecipeCategory.TRANSPORTATION, BackpackItemJsonRegistry.getBackpackByName("large"), 1)
                .unlockedBy("get_chest", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CHEST))
                .pattern("LSL")
                .pattern("i0i")
                .pattern("SBS")
                .input('S', Items.STRING).input('i', Items.IRON_INGOT)
                .input('B', Items.ENDER_EYE).input('L', Items.LEATHER)
                .input('0', Ingredient.of(itemWrap.getOrThrow(MEDIUM_BACKPACKS)))
                .save(exporter, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(MOD_ID, "large_backpack")));

        upgradeRecipes(itemWrap, exporter);
        dyedBackpackRecipes(itemWrap, exporter);
    }

    private void upgradeRecipes(HolderLookup.RegistryLookup<Item> itemWrap, RecipeOutput exporter) {
        ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.MISC, BackpackItemRegistry.VOID_UPGRADE, 1)
                .pattern("#L#")
                .pattern("KEK")
                .pattern("#L#")
                .define('#', Items.STRING)
                .define('L', Items.LEATHER)
                .define('E', Items.ENDER_PEARL).define('K', Items.LAVA_BUCKET)
                .unlockedBy("get_leather", InventoryChangeTrigger.TriggerInstance.hasItems(Items.LEATHER))
                .save(exporter);

        ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.MISC, BackpackItemRegistry.MAGNET_UPGRADE, 1)
                .pattern("#S#")
                .pattern("LEL")
                .pattern("#S#")
                .define('#', Items.IRON_INGOT).define('S', Items.STRING)
                .define('L', Items.LEATHER).define('E', Items.ENDER_PEARL)
                .unlockedBy("get_leather", InventoryChangeTrigger.TriggerInstance.hasItems(Items.LEATHER))
                .save(exporter);

        ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.MISC, BackpackItemRegistry.CRAFTING_UPGRADE, 1)
                .pattern("#L#")
                .pattern("LEL")
                .pattern("#L#")
                .define('#', ItemTags.PLANKS).define('L', Items.LEATHER)
                .define('E', Items.CRAFTING_TABLE)
                .unlockedBy("get_leather", InventoryChangeTrigger.TriggerInstance.hasItems(Items.LEATHER))
                .save(exporter);

        ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.MISC, BackpackItemRegistry.STONECUTTER_UPGRADE, 1)
                .pattern("#L#")
                .pattern("LEL")
                .pattern("#L#")
                .define('#', Items.IRON_INGOT).define('L', Items.LEATHER)
                .define('E', Items.STONECUTTER)
                .unlockedBy("get_leather", InventoryChangeTrigger.TriggerInstance.hasItems(Items.LEATHER))
                .save(exporter);

        ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.MISC, BackpackItemRegistry.JUKEBOX_UPGRADE, 1)
                .pattern("#L#")
                .pattern("LEL")
                .pattern("#L#")
                .define('#', ItemTags.PLANKS).define('L', Items.LEATHER)
                .define('E', Items.JUKEBOX)
                .unlockedBy("get_leather", InventoryChangeTrigger.TriggerInstance.hasItems(Items.LEATHER))
                .save(exporter);

        BackpackUpgradeRecipeJsonBuilder.create(
                        Ingredient.of(itemWrap.getOrThrow(UPGRADABLE_BACKPACKS)),
                        Ingredient.of(BackpackItemRegistry.VOID_UPGRADE),
                        RecipeCategory.TOOLS,
                        BackpackItemJsonRegistry.getBackpackByName("small"))
                .criterion("get_chest", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CHEST))
                .offerTo(exporter, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(MOD_ID, "void_upgrade_backpack")));

        BackpackUpgradeRecipeJsonBuilder.create(
                        Ingredient.of(itemWrap.getOrThrow(UPGRADABLE_BACKPACKS)),
                        Ingredient.of(BackpackItemRegistry.JUKEBOX_UPGRADE),
                        RecipeCategory.TOOLS,
                        BackpackItemJsonRegistry.getBackpackByName("small"))
                .criterion("get_chest", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CHEST))
                .offerTo(exporter, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(MOD_ID, "jukebox_upgrade_backpack")));

        BackpackUpgradeRecipeJsonBuilder.create(
                        Ingredient.of(itemWrap.getOrThrow(UPGRADABLE_BACKPACKS)),
                        Ingredient.of(BackpackItemRegistry.MAGNET_UPGRADE),
                        RecipeCategory.TOOLS,
                        BackpackItemJsonRegistry.getBackpackByName("small"))
                .criterion("get_chest", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CHEST))
                .offerTo(exporter, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(MOD_ID, "magnet_upgrade_backpack")));

        BackpackUpgradeRecipeJsonBuilder.create(
                        Ingredient.of(itemWrap.getOrThrow(UPGRADABLE_BACKPACKS)),
                        Ingredient.of(BackpackItemRegistry.CRAFTING_UPGRADE),
                        RecipeCategory.TOOLS,
                        BackpackItemJsonRegistry.getBackpackByName("small"))
                .criterion("get_chest", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CHEST))
                .offerTo(exporter, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(MOD_ID, "crafting_upgrade_backpack")));
    }

    private void dyedBackpackRecipes(HolderLookup.RegistryLookup<Item> itemWrap, RecipeOutput exporter) {
        for (int i = 1; i <= 3; i++) {
            HolderSet<Item> matchingBackpacks = switch (i) {
                case 1 -> itemWrap.getOrThrow(SMALL_BACKPACKS);
                case 2 -> itemWrap.getOrThrow(MEDIUM_BACKPACKS);
                case 3 -> itemWrap.getOrThrow(LARGE_BACKPACKS);
                default -> throw new IllegalStateException("Unexpected value: " + i);
            };

            String tierName = switch(i) {
                case 2 -> "medium";
                case 3 -> "large";
                default -> "small";
            };

            ContainerItem baseBackpack = (ContainerItem) BackpackItemJsonRegistry.getBackpackByName(tierName);
            for (DyeColor color : DyeColor.values()) {
                ContainerItem dyedBackpack = (ContainerItem) BackpackItemJsonRegistry.getBackpackByName(color.toString().toLowerCase() + "_" + tierName);
                String dyeName = color.name().toLowerCase();
                Item dye = DyeItem.byColor(color);

                String transmuteId = String.format("%s_%s_%d", dyeName, baseBackpack.getIdentifier().getPath(), i);
                createTransmuteRecipe(exporter, matchingBackpacks, dye, dyedBackpack, baseBackpack, tierName, transmuteId);
            }
        }
    }

    private void createTransmuteRecipe(RecipeOutput exporter, HolderSet<Item> backpackTag,
                                       Item dyeColor, Item result,
                                       Item baseItem, String tierName, String name
    ) {
        TransmuteRecipeBuilder.transmute(RecipeCategory.MISC, Ingredient.of(backpackTag), Ingredient.of(dyeColor), result)
                .group(tierName + "_dyed_backpacks")
                .unlockedBy(backpackTag.toString(), InventoryChangeTrigger.TriggerInstance.hasItems(baseItem))
                .save(exporter, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(MOD_ID, name)));
    }

    @Override
    public String getName() {
        return "recipes";
    }
}
