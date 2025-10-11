package com.rouesvm.servback.content.recipe;

import com.rouesvm.servback.content.component.UpgradeComponent;
import com.rouesvm.servback.content.item.UpgradeItem;
import com.rouesvm.servback.content.upgrade.FilterableUpgrade;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.content.upgrade.extension.ItemFilter;
import com.rouesvm.servback.content.upgrade.impl.MagnetUpgrade;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.registry.BackpackRecipeRegistry;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.Set;

public class InputDefinedFilterRecipe extends SpecialCraftingRecipe {
    public static final RecipeSerializer<InputDefinedFilterRecipe> SERIALIZER = new InputDefinedFilterRecipe.Serializer(InputDefinedFilterRecipe::new);

    public InputDefinedFilterRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        if (input.isEmpty()) return false;
        if (input.getStackCount() > MagnetUpgrade.MAX_SIZE + 1) return false;

        int magnetCount = 0;
        Set<String> seenItems = new ObjectOpenHashSet<>(MagnetUpgrade.MAX_SIZE);

        for (ItemStack stack : input.getStacks()) {
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
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
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

    private ItemStack findUpgradeStack(CraftingRecipeInput input) {
        for (ItemStack stack : input.getStacks()) {
            if (stack.isEmpty()) continue;

            UpgradeComponent component = stack.get(BackpackDataComponentTypes.UPGRADE);
            if (component != null && component.upgrade() instanceof FilterableUpgrade
            ) return stack;
        }

        return ItemStack.EMPTY;
    }

    private Set<String> extractFilterKeysFromInput(ItemStack filterStack, CraftingRecipeInput input) {
        Set<String> uniqueItems = new ObjectOpenHashSet<>(MagnetUpgrade.MAX_SIZE);

        for (ItemStack stack : input.getStacks()) {
            if (uniqueItems.size() >= MagnetUpgrade.MAX_SIZE) break;
            if (stack.isEmpty()) continue;
            if (stack.isOf(filterStack.getItem())) continue;

            uniqueItems.add(getItemKey(stack));
        }

        return uniqueItems;
    }

    private String parseTagNameFromStack(ItemStack stack) {
        String tagString = stack.getName().getString();
        if (!tagString.startsWith("#")) return null;

        Identifier tagId = Identifier.tryParse(tagString.substring(1));
        if (tagId == null) return null;

        TagKey<Item> tag = TagKey.of(RegistryKeys.ITEM, tagId);
        var tagKey = Registries.ITEM.getOptional(tag);
        if (tagKey.isEmpty()) return null;
        if (!stack.isIn(tagKey.get())) return null;

        return tagString;
    }

    private String getItemKey(ItemStack stack) {
        Identifier itemId = Registries.ITEM.getId(stack.getItem());
        String itemKey = itemId.toString();
        String tagKey = parseTagNameFromStack(stack);
        return (hasCustomName(stack) && tagKey != null) ? tagKey : itemKey;
    }

    private boolean hasCustomName(ItemStack stack) {
        return stack.getCustomName() != null;
    }

    @Override
    public DefaultedList<ItemStack> getRecipeRemainders(CraftingRecipeInput input) {
        DefaultedList<ItemStack> remainders = DefaultedList.ofSize(input.size(), ItemStack.EMPTY);

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.getItem() instanceof UpgradeItem) continue;

            remainders.set(i, stack.copyAndEmpty());
        }

        return remainders;
    }

    @Override
    public RecipeSerializer<InputDefinedFilterRecipe> getSerializer() {
        return BackpackRecipeRegistry.MAGNET_FILTER_APPLIER_RECIPE;
    }

    @Override
    public CraftingRecipeCategory getCategory() {
        return CraftingRecipeCategory.MISC;
    }

    public static class Serializer extends SpecialRecipeSerializer<InputDefinedFilterRecipe> {
        public Serializer(Factory<InputDefinedFilterRecipe> factory) {
            super(factory);
        }

        @Override
        public PacketCodec<RegistryByteBuf, InputDefinedFilterRecipe> packetCodec() {
            return null;
        }
    }
}
