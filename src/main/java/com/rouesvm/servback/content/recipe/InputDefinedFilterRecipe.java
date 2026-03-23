package com.rouesvm.servback.content.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rouesvm.servback.content.component.UpgradeComponent;
import com.rouesvm.servback.content.item.UpgradeItem;
import com.rouesvm.servback.content.upgrade.FilterableUpgrade;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.content.upgrade.extension.ItemFilter;
import com.rouesvm.servback.content.upgrade.impl.MagnetUpgrade;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.registry.BackpackRecipeRegistry;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.Set;

public class InputDefinedFilterRecipe extends CustomRecipe {
    public static final MapCodec<InputDefinedFilterRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            (i) -> i.group(
                            CraftingBookInfo.MAP_CODEC.forGetter((o) -> new CraftingBookInfo(o.category(), "")))
                    .apply(i, (c) -> new InputDefinedFilterRecipe(c.category()))
    );

    public static final RecipeSerializer<InputDefinedFilterRecipe> SERIALIZER = new RecipeSerializer<>(
            MAP_CODEC, null
    );

    public InputDefinedFilterRecipe(CraftingBookCategory category) {
        super();
    }

    @Override
    public boolean matches(CraftingInput input, Level world) {
        if (input.isEmpty()) return false;
        if (input.ingredientCount() > MagnetUpgrade.MAX_SIZE + 1) return false;

        int magnetCount = 0;
        Set<String> seenItems = new ObjectOpenHashSet<>(MagnetUpgrade.MAX_SIZE);

        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) continue;

            Item item = stack.getItem();

            if (item instanceof UpgradeItem) {
                magnetCount++;
                continue;
            }

            if (!seenItems.add(getItemKey(stack))) return false;
        }

        int totalItems = seenItems.size();

        return totalItems > 0
                && totalItems <= MagnetUpgrade.MAX_SIZE
                && magnetCount == 1;
    }

    @Override
    public @NonNull ItemStack assemble(CraftingInput input) {
        ItemStack stack = findUpgradeStack(input);
        if (stack.isEmpty()) return ItemStack.EMPTY;

        UpgradeComponent oldComponent = stack.get(BackpackDataComponentTypes.UPGRADE);
        if (oldComponent == null) return ItemStack.EMPTY;

        Set<String> uniqueItems = extractFilterKeysFromInput(stack, input);
        if (uniqueItems.isEmpty()) return ItemStack.EMPTY;

        return getResultStack(oldComponent, stack, uniqueItems);
    }

    private ItemStack getResultStack(UpgradeComponent oldComponent, ItemStack center, Set<String> uniqueItems) {
        Upgrade oldUpgrade = oldComponent.upgrade();
        Upgrade upgrade = oldUpgrade.getType().create();
        if (!(upgrade instanceof FilterableUpgrade newUpgrade)) return ItemStack.EMPTY;

        ItemFilter filter = newUpgrade.getFilter();
        filter.filterList().addAll(uniqueItems);
        filter.setMode(((FilterableUpgrade) oldUpgrade).getFilter().getMode());

        ItemStack result = center.copy();
        result.set(BackpackDataComponentTypes.UPGRADE, UpgradeComponent.of(upgrade));

        return result;
    }

    private ItemStack findUpgradeStack(CraftingInput input) {
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) continue;

            UpgradeComponent component = stack.get(BackpackDataComponentTypes.UPGRADE);
            if (component != null && component.upgrade() instanceof FilterableUpgrade
            ) return stack;
        }

        return ItemStack.EMPTY;
    }

    private Set<String> extractFilterKeysFromInput(ItemStack filterStack, CraftingInput input) {
        Set<String> uniqueItems = new ObjectOpenHashSet<>(MagnetUpgrade.MAX_SIZE);

        for (ItemStack stack : input.items()) {
            if (uniqueItems.size() >= MagnetUpgrade.MAX_SIZE) break;
            if (stack.isEmpty()) continue;
            if (stack.is(filterStack.getItem())) continue;

            uniqueItems.add(getItemKey(stack));
        }

        return uniqueItems;
    }

    private String parseTagNameFromStack(ItemStack stack) {
        String tagString = stack.getHoverName().getString();
        if (!tagString.startsWith("#")) return null;

        Identifier tagId = Identifier.tryParse(tagString.substring(1));
        if (tagId == null) return null;

        TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);
        var tagKey = BuiltInRegistries.ITEM.get(tag);
        if (tagKey.isEmpty()) return null;
        if (!stack.is(tagKey.get())) return null;

        return tagString;
    }

    private String getItemKey(ItemStack stack) {
        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String itemKey = itemId.toString();
        String tagKey = parseTagNameFromStack(stack);
        return (hasCustomName(stack) && tagKey != null) ? tagKey : itemKey;
    }

    private boolean hasCustomName(ItemStack stack) {
        return stack.getCustomName() != null;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remainders = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof UpgradeItem) continue;

            remainders.set(i, stack.copyAndClear());
        }

        return remainders;
    }

    @Override
    public @NonNull RecipeSerializer<InputDefinedFilterRecipe> getSerializer() {
        return BackpackRecipeRegistry.MAGNET_FILTER_APPLIER_RECIPE;
    }

    @Override
    public @NonNull CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }
}
