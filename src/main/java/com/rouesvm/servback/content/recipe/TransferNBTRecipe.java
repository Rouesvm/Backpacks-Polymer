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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class TransferNBTRecipe extends NormalCraftingRecipe {
    public static final MapCodec<TransferNBTRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            (i) -> i.group(CommonInfo.MAP_CODEC.forGetter((o) -> o.commonInfo),
                    CraftingBookInfo.MAP_CODEC.forGetter((o) -> o.bookInfo),
                    ShapedRecipePattern.MAP_CODEC.forGetter((o) -> o.pattern),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter((o) -> o.result))
                    .apply(i, TransferNBTRecipe::new)
    );

    public final ShapedRecipePattern pattern;
    public final ItemStackTemplate result;

    public static final RecipeSerializer<TransferNBTRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, null);

    public TransferNBTRecipe(final Recipe.CommonInfo commonInfo, final CraftingRecipe.CraftingBookInfo bookInfo, final ShapedRecipePattern raw, final ItemStackTemplate result) {
        super(commonInfo, bookInfo);
        this.pattern = raw;
        this.result = result;
    }

    @Override
    public @NonNull List<RecipeDisplay> display() {
        return List.of(new ShapedCraftingRecipeDisplay(this.pattern.width(), this.pattern.height(), this.pattern.ingredients().stream().map((e) -> e.map(Ingredient::display).orElse(SlotDisplay.Empty.INSTANCE)).toList(), new SlotDisplay.ItemStackSlotDisplay(this.result), new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }

    @Override
    protected @NonNull PlacementInfo createPlacementInfo() {
        return PlacementInfo.createFromOptionals(this.pattern.ingredients());
    }

    public @NonNull RecipeSerializer<TransferNBTRecipe> getSerializer() {
        return BackpackRecipeRegistry.TRANSFER_NBT_RECIPE;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return this.pattern.matches(input);
    }

    @Override
    public @NonNull ItemStack assemble(@NonNull CraftingInput craftingRecipeInput) {
        ItemStack resultStack = this.result.create();

        ItemStack stack = craftingRecipeInput.getItem(4);
        if (stack.getItem() instanceof ContainerItem) {
            resultStack.set(BackpackDataComponentTypes.BACKPACK_UUID, BackpackUUID.getStackUUID(stack));
            resultStack.set(DataComponents.ENCHANTMENTS, stack.get(DataComponents.ENCHANTMENTS));
        }

        return resultStack;
    }
}
