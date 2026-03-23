package com.rouesvm.servback.content.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.registry.BackpackRecipeRegistry;
import com.rouesvm.servback.technical.manager.BackpackUUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class TransferNBTRecipe extends CustomRecipe {
    public static final MapCodec<ShapedRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            (i) -> i.group(CommonInfo.MAP_CODEC.forGetter((o) -> o.commonInfo),
                    CraftingBookInfo.MAP_CODEC.forGetter((o) -> o.bookInfo),
                    ShapedRecipePattern.MAP_CODEC.forGetter((o) -> o.pattern),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter((o) -> o.result))
                    .apply(i, ShapedRecipe::new)
    );

    public final ShapedRecipePattern raw;
    public final ItemStack result;

    public static final RecipeSerializer<TransferNBTRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, null);

    public TransferNBTRecipe(String group, CraftingBookCategory category, ShapedRecipePattern raw, ItemStack result, boolean showNotification) {
        super();
        this.raw = raw;
        this.result = result;
    }

    public @NonNull RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return BackpackRecipeRegistry.TRANSFER_NBT_RECIPE;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return this.raw.matches(input);
    }

    @Override
    public @NonNull ItemStack assemble(@NonNull CraftingInput craftingRecipeInput) {
        ItemStack resultStack = this.result.copy();

        ItemStack stack = craftingRecipeInput.getItem(4);
        if (stack.getItem() instanceof ContainerItem) {
            resultStack.set(BackpackDataComponentTypes.BACKPACK_UUID, BackpackUUID.getStackUUID(stack));
            resultStack.set(DataComponents.ENCHANTMENTS, stack.get(DataComponents.ENCHANTMENTS));
        }

        return resultStack;
    }

    public ShapedRecipePattern getRaw() {
        return raw;
    }

    public ItemStack getResult() {
        return result;
    }
}
