package com.rouesvm.servback.registry;

import com.rouesvm.servback.content.recipe.BackpackRecipe;
import com.rouesvm.servback.content.recipe.BackpackUpgradeRecipe;
import com.rouesvm.servback.content.recipe.InputDefinedFilterRecipe;
import com.rouesvm.servback.content.recipe.LavaBackpackRecipe;
import com.rouesvm.servback.content.recipe.TransferNBTRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;

import static com.rouesvm.servback.ServerBackpacks.MOD_ID;

public class BackpackRecipeRegistry {
    public static final RecipeSerializer<TransferNBTRecipe> TRANSFER_NBT_RECIPE = register("nbt_transfer", TransferNBTRecipe.SERIALIZER);
    public static final RecipeSerializer<BackpackRecipe> BACKPACK_CRAFTING_RECIPE = register("backpack_crafting", BackpackRecipe.SERIALIZER);
    public static final RecipeSerializer<BackpackUpgradeRecipe> BACKPACK_UPGRADE_RECIPE = register("backpack_upgrading", BackpackUpgradeRecipe.SERIALIZER);
    public static final RecipeSerializer<InputDefinedFilterRecipe> MAGNET_FILTER_APPLIER_RECIPE = register("magnet_filter_applier", InputDefinedFilterRecipe.SERIALIZER);
    public static final RecipeSerializer<LavaBackpackRecipe> LAVA_BACKPACK_RECIPE = register("lava_backpack", LavaBackpackRecipe.SERIALIZER);

    public static <T extends Recipe<A>, A extends RecipeInput> RecipeSerializer<T> register(String name, RecipeSerializer<T> serializer) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(MOD_ID, name), serializer);
    }

    public static void initialize() {}
}
