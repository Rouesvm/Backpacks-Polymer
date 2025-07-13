package com.rouesvm.servback.content.recipe.builder;

import com.rouesvm.servback.content.recipe.BackpackUpgradeRecipe;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.TransmuteRecipeResult;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class BackpackUpgradeRecipeJsonBuilder {
    private final Ingredient base;
    private final Ingredient addition;
    private final RecipeCategory category;
    private final Item result;
    private final Map<String, AdvancementCriterion<?>> criteria = new LinkedHashMap<>();

    public BackpackUpgradeRecipeJsonBuilder(Ingredient base, Ingredient addition, RecipeCategory category, Item result) {
        this.category = category;
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    public static BackpackUpgradeRecipeJsonBuilder create(Ingredient base, Ingredient addition, RecipeCategory category, Item result) {
        return new BackpackUpgradeRecipeJsonBuilder(base, addition, category, result);
    }

    public BackpackUpgradeRecipeJsonBuilder criterion(String name, AdvancementCriterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    public void offerTo(RecipeExporter exporter, String recipeId) {
        this.offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(recipeId)));
    }

    public void offerTo(RecipeExporter exporter, RegistryKey<Recipe<?>> recipeKey) {
        this.validate(recipeKey);
        Advancement.Builder builder = exporter.getAdvancementBuilder().criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeKey)).rewards(AdvancementRewards.Builder.recipe(recipeKey)).criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        Objects.requireNonNull(builder);
        this.criteria.forEach(builder::criterion);
        BackpackUpgradeRecipe smithingTransformRecipe = new BackpackUpgradeRecipe(this.base, Optional.of(this.addition), new TransmuteRecipeResult(this.result));
        exporter.accept(recipeKey, smithingTransformRecipe, builder.build(recipeKey.getValue().withPrefixedPath("recipes/" + this.category.getName() + "/")));
    }

    private void validate(RegistryKey<Recipe<?>> recipeKey) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeKey.getValue());
        }
    }
}
