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
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
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
        Set<Item> seenItems = new HashSet<>();

        for (ItemStack stack : input.getStacks()) {
            if (stack.isEmpty()) continue;

            Item item = stack.getItem();

            if (item instanceof UpgradeItem) {
                magnetCount++;
                continue;
            }

            if (!seenItems.add(item)) return false;
        }

        int totalItems = seenItems.size();

        return totalItems > 0
                && totalItems <= MagnetUpgrade.MAX_SIZE
                && magnetCount == 1;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        ItemStack center = ItemStack.EMPTY;
        List<Item> uniqueItems = new ArrayList<>();

        for (ItemStack stack : input.getStacks()) {
            if (uniqueItems.size() > MagnetUpgrade.MAX_SIZE) break;

            if (stack.isEmpty()) continue;

            if (stack.isOf(BackpackItemRegistry.MAGNET_UPGRADE)) {
                center = stack;
                continue;
            }

            Item item = stack.getItem();
            if (!uniqueItems.contains(item)) uniqueItems.add(item);
        }

        if (uniqueItems.isEmpty() || center.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = center.copy();

        MagnetUpgrade upgrade = BackpackUpgradeRegistry.MAGNET.create();
        upgrade.setList(uniqueItems);

        UpgradeContainerComponent component = new UpgradeContainerComponent(List.of(upgrade));
        result.set(BackpackDataComponentTypes.UPGRADE_CONTAINER_COMPONENT_COMPONENT_TYPE, component);

        return result;
    }

    @Override
    public DefaultedList<ItemStack> getRecipeRemainders(CraftingRecipeInput input) {
        DefaultedList<ItemStack> remainders = DefaultedList.ofSize(input.size(), ItemStack.EMPTY);

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isOf(BackpackItemRegistry.MAGNET_UPGRADE)) continue;

            remainders.set(i, stack.copy());
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
