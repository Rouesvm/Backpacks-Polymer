package com.rouesvm.servback.content.recipe.builder;

import com.rouesvm.servback.content.recipe.BackpackUpgradeRecipe;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class BackpackUpgradeRecipeJsonBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final Ingredient input;
    private final Ingredient material;
    private final RecipeUnlockAdvancementBuilder advancementBuilder = new RecipeUnlockAdvancementBuilder();
    private @Nullable String group;

    public BackpackUpgradeRecipeJsonBuilder(Ingredient base, Ingredient addition, RecipeCategory category, Item result) {
        this.category = category;
        this.input = base;
        this.material = addition;
        this.result = result;
    }

    public static BackpackUpgradeRecipeJsonBuilder create(Ingredient base, Ingredient addition, RecipeCategory category, Item result) {
        return new BackpackUpgradeRecipeJsonBuilder(base, addition, category, result);
    }

    public BackpackUpgradeRecipeJsonBuilder unlockedBy(final String name, final Criterion<?> criterion) {
        this.advancementBuilder.unlockedBy(name, criterion);
        return this;
    }

    public BackpackUpgradeRecipeJsonBuilder group(final @Nullable String group) {
        this.group = group;
        return this;
    }

    public void save(RecipeOutput output, ResourceKey<Recipe<?>> location) {
        BackpackUpgradeRecipe recipe = new BackpackUpgradeRecipe(this.input, Optional.of(this.material), new ItemStackTemplate(result));
        output.accept(location, recipe, this.advancementBuilder.build(output, location, this.category));
    }
}
