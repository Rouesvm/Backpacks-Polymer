package com.rouesvm.servback.content.recipe.builder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rouesvm.servback.content.recipe.BackpackRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BackpackRecipeJsonBuilder implements RecipeBuilder {
    private final HolderGetter<Item> registryLookup;
    private final RecipeCategory category;
    private final Item output;
    private final int count;
    private final List<String> pattern = Lists.newArrayList();
    private final Map<Character, Ingredient> inputs = Maps.newLinkedHashMap();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;

    private BackpackRecipeJsonBuilder(HolderGetter<Item> registryLookup, RecipeCategory category, ItemLike output, int count) {
        this.registryLookup = registryLookup;
        this.category = category;
        this.output = output.asItem();
        this.count = count;
    }

    public static BackpackRecipeJsonBuilder create(HolderGetter<Item> registryLookup, RecipeCategory category, ItemLike output) {
        return create(registryLookup, category, output, 1);
    }

    public static BackpackRecipeJsonBuilder create(HolderGetter<Item> registryLookup, RecipeCategory category, ItemLike output, int count) {
        return new BackpackRecipeJsonBuilder(registryLookup, category, output, count);
    }

    public BackpackRecipeJsonBuilder input(Character c, TagKey<Item> tag) {
        return this.input(c, Ingredient.of(this.registryLookup.getOrThrow(tag)));
    }

    public BackpackRecipeJsonBuilder input(Character c, ItemLike item) {
        return this.input(c, Ingredient.of(item));
    }

    public BackpackRecipeJsonBuilder input(Character c, Ingredient ingredient) {
        if (this.inputs.containsKey(c)) {
            throw new IllegalArgumentException("Symbol '" + c + "' is already defined!");
        } else if (c == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        } else {
            this.inputs.put(c, ingredient);
            return this;
        }
    }

    public BackpackRecipeJsonBuilder pattern(String patternStr) {
        if (!this.pattern.isEmpty() && patternStr.length() != this.pattern.getFirst().length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        } else {
            this.pattern.add(patternStr);
            return this;
        }
    }

    public @NonNull BackpackRecipeJsonBuilder unlockedBy(@NonNull String string, @NonNull Criterion<?> advancementCriterion) {
        this.criteria.put(string, advancementCriterion);
        return this;
    }

    public @NonNull BackpackRecipeJsonBuilder group(@Nullable String string) {
        this.group = string;
        return this;
    }

    @Override
    public @NonNull Item getResult() {
        return this.output;
    }

    @Override
    public void save(RecipeOutput exporter, @NonNull ResourceKey<Recipe<?>> recipeKey) {
        ShapedRecipePattern rawShapedRecipe = this.validate(recipeKey);
        Advancement.Builder builder = exporter.advancement().addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeKey)).rewards(AdvancementRewards.Builder.recipe(recipeKey)).requirements(AdvancementRequirements.Strategy.OR);
        Objects.requireNonNull(builder);
        this.criteria.forEach(builder::addCriterion);
        boolean showNotification = true;
        BackpackRecipe shapedRecipe = new BackpackRecipe(
                Objects.requireNonNullElse(this.group, ""),
                RecipeBuilder.determineBookCategory(this.category),
                rawShapedRecipe,
                new ItemStack(this.output, this.count), showNotification
        );
        exporter.accept(recipeKey, shapedRecipe, builder.build(recipeKey.identifier().withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private ShapedRecipePattern validate(ResourceKey<Recipe<?>> recipeKey) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeKey.identifier());
        } else {
            return ShapedRecipePattern.of(this.inputs, this.pattern);
        }
    }
}
