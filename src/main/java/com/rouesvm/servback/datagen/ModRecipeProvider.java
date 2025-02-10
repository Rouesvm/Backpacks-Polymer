package com.rouesvm.servback.datagen;

import com.rouesvm.servback.items.ContainerItem;
import com.rouesvm.servback.items.ItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

import static com.rouesvm.servback.Main.MOD_ID;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        itemRecipes(exporter);
    }

    private void itemRecipes(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ItemRegistry.ENDER_BACKPACK, 1)
                .pattern("#i#")
                .pattern("SES")
                .pattern(" N ")
                .input('#', Items.LEATHER).input('S', Items.STRING)
                .input('i', Items.IRON_INGOT).input('E', ModItemTags.LARGE_BACKPACKS)
                .input('N', Items.ENDER_EYE)
                .criterion(FabricRecipeProvider.hasItem(Items.OBSIDIAN), FabricRecipeProvider.conditionsFromItem(Items.OBSIDIAN))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ItemRegistry.GLOBAL_BACKPACK, 1)
                .pattern("#i#")
                .pattern("SES")
                .pattern(" N ")
                .input('#', Items.ENDER_EYE).input('S', Items.STRING)
                .input('i', Items.IRON_INGOT).input('E', ItemRegistry.ENDER_BACKPACK)
                .input('N', Items.NETHER_STAR)
                .criterion(FabricRecipeProvider.hasItem(Items.ENDER_EYE), FabricRecipeProvider.conditionsFromItem(Items.ENDER_EYE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ItemRegistry.SMALL_BACKPACK, 1)
                .pattern("#S#")
                .pattern("SCS")
                .pattern(" # ")
                .input('#', Items.LEATHER).input('S', Items.STRING).input('C', Items.CHEST)
                .criterion(FabricRecipeProvider.hasItem(Items.CHEST), FabricRecipeProvider.conditionsFromItem(Items.CHEST))
                .offerTo(exporter);

        dyedBackpackRecipes(exporter);
    }

    private void dyedBackpackRecipes(RecipeExporter exporter) {
        for (int i = 1; i <= 3; i++) {
            ContainerItem backpack = (ContainerItem) ContainerItem.getDefaultBackpack(i);
            String backpackName = backpack.getIdentifier().getPath();

            for (DyeColor color : DyeColor.values()) {
                ContainerItem coloredBackpack = (ContainerItem) ContainerItem.getColoredBackpack(color, i);

                String name = color.getName().toLowerCase() + "_" + backpackName;
                Item dyeColor = DyeItem.byColor(color);

                createTransmuteRecipe(exporter, backpack, dyeColor, coloredBackpack, name, i);

                if (i + 1 == 4) continue;
                ContainerItem backpackUpATier = (ContainerItem) ContainerItem.getColoredBackpack(color, i + 1);
                createUpgradeRecipe(exporter, coloredBackpack, backpackUpATier, backpackUpATier.getSize(), name + "_" + backpackUpATier.getIdentifier().getPath());
            }
        }
    }

    private void createTransmuteRecipe(RecipeExporter exporter, Item backpack, Item dyeColor, Item result, String name, int size) {
        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, result)
                .input(backpack)
                .input(dyeColor)
                .group(size + "_dyedBackpacks")
                .criterion(FabricRecipeProvider.hasItem(backpack), FabricRecipeProvider.conditionsFromItem(backpack))
                .offerTo(exporter, Identifier.of(MOD_ID, name));
    }

    private void createUpgradeRecipe(RecipeExporter exporter,
                                     Item backpack, Item backpackUpATier,
                                     int slots,
                                     String name) {
        ShapedRecipeJsonBuilder builder = ShapedRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, backpackUpATier)
                .group(slots + "_upgraded")
                .criterion(FabricRecipeProvider.hasItem(backpack), FabricRecipeProvider.conditionsFromItem(backpack));

        switch (slots) {
            case 2 -> builder
                    .pattern("iLi")
                    .pattern("SES")
                    .pattern(" O ")
                    .input('L', Items.LEATHER).input('S', Items.STRING)
                    .input('i', Items.IRON_INGOT).input('O', ItemTags.PLANKS)
                    .input('E', backpack)
                    .offerTo(exporter, Identifier.of(MOD_ID, name));
            case 3 -> builder
                    .pattern("ZiZ")
                    .pattern("SLS")
                    .input('Z', Items.STRING).input('i', Items.IRON_INGOT)
                    .input('S', Items.SHULKER_SHELL)
                    .input('L', backpack)
                    .offerTo(exporter, Identifier.of(MOD_ID, name));
        }
    }

    @Override
    public String getName() {
        return "recipes";
    }
}
