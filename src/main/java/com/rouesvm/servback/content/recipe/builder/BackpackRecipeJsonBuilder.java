package com.rouesvm.servback.content.recipe.builder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rouesvm.servback.content.recipe.BackpackRecipe;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class BackpackRecipeJsonBuilder implements RecipeBuilder {
    private final HolderGetter<Item> items;
    private final RecipeCategory category;
    private final ItemStackTemplate result;
    private final List<String> rows;
    private final Map<Character, Ingredient> key;
    private final RecipeUnlockAdvancementBuilder advancementBuilder;
    private @org.jspecify.annotations.Nullable String group;
    private boolean showNotification;

    private BackpackRecipeJsonBuilder(final HolderGetter<Item> items, final RecipeCategory category, final ItemStackTemplate result) {
        this.rows = Lists.newArrayList();
        this.key = Maps.newLinkedHashMap();
        this.advancementBuilder = new RecipeUnlockAdvancementBuilder();
        this.showNotification = true;
        this.items = items;
        this.category = category;
        this.result = result;
    }

    private BackpackRecipeJsonBuilder(final HolderGetter<Item> items, final RecipeCategory category, final ItemLike result, final int count) {
        this(items, category, new ItemStackTemplate(result.asItem(), count));
    }

    public static BackpackRecipeJsonBuilder shaped(final HolderGetter<Item> items, final RecipeCategory category, final ItemLike item) {
        return shaped(items, category, item, 1);
    }

    public static BackpackRecipeJsonBuilder shaped(final HolderGetter<Item> items, final RecipeCategory category, final ItemLike item, final int count) {
        return new BackpackRecipeJsonBuilder(items, category, item, count);
    }

    public BackpackRecipeJsonBuilder define(final Character symbol, final TagKey<Item> tag) {
        return this.define(symbol, Ingredient.of(this.items.getOrThrow(tag)));
    }

    public BackpackRecipeJsonBuilder define(final Character symbol, final ItemLike item) {
        return this.define(symbol, Ingredient.of(item));
    }

    public BackpackRecipeJsonBuilder define(final Character symbol, final Ingredient ingredient) {
        if (this.key.containsKey(symbol)) {
            throw new IllegalArgumentException("Symbol '" + symbol + "' is already defined!");
        } else if (symbol == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        } else {
            this.key.put(symbol, ingredient);
            return this;
        }
    }

    public BackpackRecipeJsonBuilder pattern(final String row) {
        if (!this.rows.isEmpty() && row.length() != ((String)this.rows.get(0)).length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        } else {
            this.rows.add(row);
            return this;
        }
    }

    public BackpackRecipeJsonBuilder unlockedBy(final String name, final Criterion<?> criterion) {
        this.advancementBuilder.unlockedBy(name, criterion);
        return this;
    }

    public BackpackRecipeJsonBuilder group(final @Nullable String group) {
        this.group = group;
        return this;
    }

    public BackpackRecipeJsonBuilder showNotification(final boolean showNotification) {
        this.showNotification = showNotification;
        return this;
    }

    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(this.result);
    }

    public void save(final RecipeOutput output, final ResourceKey<Recipe<?>> id) {
        ShapedRecipePattern pattern = ShapedRecipePattern.of(this.key, this.rows);
        BackpackRecipe recipe = new BackpackRecipe(RecipeBuilder.createCraftingCommonInfo(this.showNotification), RecipeBuilder.createCraftingBookInfo(this.category, this.group), pattern, this.result);
        output.accept(id, recipe, this.advancementBuilder.build(output, id, this.category));
    }
}
