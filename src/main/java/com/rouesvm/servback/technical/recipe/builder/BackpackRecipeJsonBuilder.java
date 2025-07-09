package com.rouesvm.servback.technical.recipe.builder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rouesvm.servback.technical.recipe.BackpackRecipe;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BackpackRecipeJsonBuilder implements CraftingRecipeJsonBuilder {
    private final RegistryEntryLookup<Item> registryLookup;
    private final RecipeCategory category;
    private final Item output;
    private final int count;
    private final List<String> pattern = Lists.newArrayList();
    private final Map<Character, Ingredient> inputs = Maps.newLinkedHashMap();
    private final Map<String, AdvancementCriterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;
    private boolean showNotification = true;

    private BackpackRecipeJsonBuilder(RegistryEntryLookup<Item> registryLookup, RecipeCategory category, ItemConvertible output, int count) {
        this.registryLookup = registryLookup;
        this.category = category;
        this.output = output.asItem();
        this.count = count;
    }

    public static BackpackRecipeJsonBuilder create(RegistryEntryLookup<Item> registryLookup, RecipeCategory category, ItemConvertible output) {
        return create(registryLookup, category, output, 1);
    }

    public static BackpackRecipeJsonBuilder create(RegistryEntryLookup<Item> registryLookup, RecipeCategory category, ItemConvertible output, int count) {
        return new BackpackRecipeJsonBuilder(registryLookup, category, output, count);
    }

    public BackpackRecipeJsonBuilder input(Character c, TagKey<Item> tag) {
        return this.input(c, Ingredient.ofTag(this.registryLookup.getOrThrow(tag)));
    }

    public BackpackRecipeJsonBuilder input(Character c, ItemConvertible item) {
        return this.input(c, Ingredient.ofItem(item));
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
        if (!this.pattern.isEmpty() && patternStr.length() != ((String)this.pattern.get(0)).length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        } else {
            this.pattern.add(patternStr);
            return this;
        }
    }

    public BackpackRecipeJsonBuilder criterion(String string, AdvancementCriterion<?> advancementCriterion) {
        this.criteria.put(string, advancementCriterion);
        return this;
    }

    public BackpackRecipeJsonBuilder group(@Nullable String string) {
        this.group = string;
        return this;
    }

    @Override
    public Item getOutputItem() {
        return this.output;
    }

    @Override
    public void offerTo(RecipeExporter exporter, RegistryKey<Recipe<?>> recipeKey) {
        RawShapedRecipe rawShapedRecipe = this.validate(recipeKey);
        Advancement.Builder builder = exporter.getAdvancementBuilder().criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeKey)).rewards(AdvancementRewards.Builder.recipe(recipeKey)).criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        Objects.requireNonNull(builder);
        this.criteria.forEach(builder::criterion);
        BackpackRecipe shapedRecipe = new BackpackRecipe(
                Objects.requireNonNullElse(this.group, ""),
                CraftingRecipeJsonBuilder.toCraftingCategory(this.category),
                rawShapedRecipe,
                new ItemStack(this.output, this.count), this.showNotification
        );
        exporter.accept(recipeKey, shapedRecipe, builder.build(recipeKey.getValue().withPrefixedPath("recipes/" + this.category.getName() + "/")));
    }

    private RawShapedRecipe validate(RegistryKey<Recipe<?>> recipeKey) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeKey.getValue());
        } else {
            return RawShapedRecipe.create(this.inputs, this.pattern);
        }
    }
}
