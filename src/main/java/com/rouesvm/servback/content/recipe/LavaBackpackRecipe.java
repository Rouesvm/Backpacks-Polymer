package com.rouesvm.servback.content.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.registry.BackpackRecipeRegistry;
import com.rouesvm.servback.registry.item.BackpackItemJsonRegistry;
import com.rouesvm.servback.registry.item.BackpackItemRegistry;
import com.rouesvm.servback.technical.manager.BackpackUUID;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.Set;

public class LavaBackpackRecipe extends CustomRecipe {
    public static final MapCodec<LavaBackpackRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            (i) -> i.group(
                            CraftingBookInfo.MAP_CODEC.forGetter((o) -> new CraftingBookInfo(o.category(), "")))
                    .apply(i, (c) -> new LavaBackpackRecipe())
    );

    public static final RecipeSerializer<LavaBackpackRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, null);

    public LavaBackpackRecipe() {
        super();
    }

    @Override
    public boolean matches(CraftingInput input, @NonNull Level world) {
        if (input.isEmpty()) return false;

        boolean hasBackpack = false;
        boolean hasLavaBucket = false;

        Set<Item> mediumBackpacks = BackpackItemJsonRegistry.getBackpacksByName("medium");

        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof ContainerItem) {
                if (hasBackpack) return false;
                if (mediumBackpacks != null && mediumBackpacks.contains(stack.getItem())) {
                    hasBackpack = true;
                } else {
                    return false;
                }
            } else if (stack.is(Items.LAVA_BUCKET)) {
                if (hasLavaBucket) return false;
                hasLavaBucket = true;
            } else {
                return false;
            }
        }

        return hasBackpack && hasLavaBucket;
    }

    @Override
    public @NonNull ItemStack assemble(CraftingInput input) {
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof ContainerItem) {
                ItemStack result = new ItemStack(BackpackItemRegistry.LAVA_BACKPACK);
                result.set(BackpackDataComponentTypes.BACKPACK_UUID, BackpackUUID.getStackUUID(stack));
                result.set(DataComponents.ENCHANTMENTS, stack.get(DataComponents.ENCHANTMENTS));
                if (stack.has(BackpackDataComponentTypes.UPGRADE_CONTAINER)) {
                    result.set(BackpackDataComponentTypes.UPGRADE_CONTAINER, stack.get(BackpackDataComponentTypes.UPGRADE_CONTAINER));
                }
                if (stack.has(DataComponents.CUSTOM_NAME)) {
                    result.set(DataComponents.CUSTOM_NAME, stack.get(DataComponents.CUSTOM_NAME));
                }
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NonNull NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remainders = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < input.size(); i++) {
            if (input.getItem(i).is(Items.LAVA_BUCKET)) {
                remainders.set(i, new ItemStack(Items.BUCKET));
            }
        }
        return remainders;
    }

    @Override
    public @NonNull RecipeSerializer<LavaBackpackRecipe> getSerializer() {
        return BackpackRecipeRegistry.LAVA_BACKPACK_RECIPE;
    }

    @Override
    public @NonNull CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }
}
