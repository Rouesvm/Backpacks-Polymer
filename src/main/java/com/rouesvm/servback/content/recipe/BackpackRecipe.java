package com.rouesvm.servback.content.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.registry.BackpackRecipeRegistry;
import com.rouesvm.servback.registry.item.BackpackItemJsonRegistry;
import com.rouesvm.servback.technical.manager.BackpackUUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class BackpackRecipe extends NormalCraftingRecipe {
    public static final MapCodec<BackpackRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            (i) -> i.group(CommonInfo.MAP_CODEC.forGetter((o) -> o.commonInfo),
                            CraftingBookInfo.MAP_CODEC.forGetter((o) -> o.bookInfo),
                            ShapedRecipePattern.MAP_CODEC.forGetter((o) -> o.pattern),
                            ItemStackTemplate.CODEC.fieldOf("result").forGetter((o) -> o.result))
                    .apply(i, BackpackRecipe::new)
    );

    private final ShapedRecipePattern pattern;
    private final ItemStackTemplate result;

    public static final RecipeSerializer<BackpackRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, null);

    public BackpackRecipe(final Recipe.CommonInfo commonInfo, final CraftingRecipe.CraftingBookInfo bookInfo, final ShapedRecipePattern pattern, final ItemStackTemplate result) {
        super(commonInfo, bookInfo);
        this.pattern = pattern;
        this.result = result;
    }

    public @NonNull RecipeSerializer<BackpackRecipe> getSerializer() {
        return BackpackRecipeRegistry.BACKPACK_CRAFTING_RECIPE;
    }

    @Override
    protected @NonNull PlacementInfo createPlacementInfo() {
        return PlacementInfo.createFromOptionals(this.pattern.ingredients());
    }

    @Override
    public boolean matches(CraftingInput input, @NonNull Level level) {
        return this.pattern.matches(input);
    }

    @Override
    public @NonNull ItemStack assemble(@NonNull CraftingInput craftingRecipeInput) {
        ItemStack resultStack = this.result.create();

        ItemStack stack = craftingRecipeInput.getItem(4);
        if (stack.getItem() instanceof ContainerItem backpack) {
            ItemStack upgradeStack = BackpackItemJsonRegistry.getBackpackUpgrade(backpack).getDefaultInstance();
            upgradeStack = upgradeStack.copy();
            upgradeStack.set(BackpackDataComponentTypes.BACKPACK_UUID, BackpackUUID.getStackUUID(stack));
            upgradeStack.set(DataComponents.ENCHANTMENTS, stack.get(DataComponents.ENCHANTMENTS));

            resultStack = upgradeStack;
        }

        return resultStack;
    }

    public ShapedRecipePattern getPattern() {
        return pattern;
    }

    public ItemStack getResult() {
        return result.create();
    }
}
