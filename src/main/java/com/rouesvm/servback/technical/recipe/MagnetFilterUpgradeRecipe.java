package com.rouesvm.servback.technical.recipe;

import com.rouesvm.servback.content.component.UpgradeContainerComponent;
import com.rouesvm.servback.content.item.UpgradeItem;
import com.rouesvm.servback.content.upgrade.impl.MagnetUpgrade;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.registry.BackpackRecipeRegistry;
import com.rouesvm.servback.registry.BackpackUpgradeRegistry;
import com.rouesvm.servback.registry.item.BackpackItemRegistry;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MagnetFilterUpgradeRecipe extends SpecialCraftingRecipe {
    public static final RecipeSerializer<MagnetFilterUpgradeRecipe> SERIALIZER = new MagnetFilterUpgradeRecipe.Serializer(MagnetFilterUpgradeRecipe::new);

    public MagnetFilterUpgradeRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        if (input.isEmpty()) return false;
        if (input.getStackCount() > MagnetUpgrade.MAX_SIZE + 1) return false;

        int magnetCount = 0;
        Set<String> seenItems = new HashSet<>();

        for (ItemStack stack : input.getStacks()) {
            if (stack.isEmpty()) continue;

            Item item = stack.getItem();

            if (item instanceof UpgradeItem) {
                magnetCount++;
                continue;
            }

            if (isSpecialNameTag(stack)) {
                String tagId = stack.getName().getString();

                if (!tagId.startsWith("#")) continue;

                Identifier tagIdentifier = Identifier.tryParse(tagId.substring(1));
                if (tagIdentifier == null) continue;

                TagKey<Item> tag = TagKey.of(RegistryKeys.ITEM, tagIdentifier);
                if (Registries.ITEM.getOptional(tag).isEmpty()) continue;

                if (!seenItems.add(tagId)) return false;
                continue;
            }

            if (!seenItems.add(item.toString())) return false;
        }

        int totalItems = seenItems.size();

        return totalItems > 0
                && totalItems <= MagnetUpgrade.MAX_SIZE
                && magnetCount == 1;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        ItemStack center = ItemStack.EMPTY;

        List<String> uniqueItems = getFilter(input);

        for (ItemStack stack : input.getStacks()) {
            if (stack.isEmpty()) continue;
            if (stack.isOf(BackpackItemRegistry.MAGNET_UPGRADE)) {
                center = stack;
                break;
            }
        }

        if (uniqueItems.isEmpty() || center.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = center.copy();

        MagnetUpgrade upgrade = BackpackUpgradeRegistry.MAGNET.create();
        UpgradeContainerComponent defaultComponent = UpgradeContainerComponent.of(List.of(upgrade));

        UpgradeContainerComponent oldComponent = center.getOrDefault(BackpackDataComponentTypes.UPGRADE_CONTAINER, defaultComponent);
        MagnetUpgrade oldUpgrade = (MagnetUpgrade) oldComponent.getBaseUpgrades().getFirst();

        upgrade.addAllToList(uniqueItems);
        upgrade.setMode(oldUpgrade.getMode());

        result.set(BackpackDataComponentTypes.UPGRADE_CONTAINER, UpgradeContainerComponent.of(List.of(upgrade)));

        return result;
    }

    public List<String> getFilter(CraftingRecipeInput input) {
        List<String> uniqueItems = new ArrayList<>();

        for (ItemStack stack : input.getStacks()) {
            if (uniqueItems.size() >= MagnetUpgrade.MAX_SIZE) break;
            if (stack.isEmpty()) continue;
            if (stack.isOf(BackpackItemRegistry.MAGNET_UPGRADE)) continue;

            String itemKey;

            if (isSpecialNameTag(stack)) {
                itemKey = stack.getName().getString();
                if (!itemKey.startsWith("#")) continue;

                Identifier tagId = Identifier.tryParse(itemKey.substring(1));
                if (tagId == null) continue;

                TagKey<Item> tag = TagKey.of(RegistryKeys.ITEM, tagId);
                if (Registries.ITEM.getOptional(tag).isEmpty()) continue;

            } else itemKey = Registries.ITEM.getId(stack.getItem()).toString();

            if (!uniqueItems.contains(itemKey)) uniqueItems.add(itemKey);
        }

        return uniqueItems;
    }

    private boolean isSpecialNameTag(ItemStack stack) {
        return stack.isOf(Items.NAME_TAG) && stack.getCustomName() != null;
    }

    @Override
    public DefaultedList<ItemStack> getRecipeRemainders(CraftingRecipeInput input) {
        DefaultedList<ItemStack> remainders = DefaultedList.ofSize(input.size(), ItemStack.EMPTY);

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isOf(BackpackItemRegistry.MAGNET_UPGRADE)) continue;

            remainders.set(i, stack.copyAndEmpty());
        }

        return remainders;
    }

    @Override
    public RecipeSerializer<MagnetFilterUpgradeRecipe> getSerializer() {
        return BackpackRecipeRegistry.MAGNET_FILTER_APPLIER_RECIPE;
    }

    @Override
    public CraftingRecipeCategory getCategory() {
        return CraftingRecipeCategory.MISC;
    }

    public static class Serializer extends SpecialRecipeSerializer<MagnetFilterUpgradeRecipe> implements PolymerObject {
        public Serializer(Factory<MagnetFilterUpgradeRecipe> factory) {
            super(factory);
        }

        @Override
        public PacketCodec<RegistryByteBuf, MagnetFilterUpgradeRecipe> packetCodec() {
            return null;
        }
    }
}
