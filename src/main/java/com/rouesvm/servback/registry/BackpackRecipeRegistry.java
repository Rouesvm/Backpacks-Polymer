package com.rouesvm.servback.registry;

import com.rouesvm.servback.technical.recipe.BackpackRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static com.rouesvm.servback.ServerBackpacks.MOD_ID;

public class BackpackRecipeRegistry {
    public static RecipeSerializer<BackpackRecipe> recipe = register("backpack_crafting", BackpackRecipe.BACKPACK_CRAFTING);

    public static <T extends Recipe<A>, A extends RecipeInput> RecipeSerializer<T> register(String name, RecipeSerializer<T> serializer) {
        return Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(MOD_ID, name), serializer);
    }

    public static void initialize() {}
}
