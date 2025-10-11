package com.rouesvm.servback.content.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.registry.BackpackRecipeRegistry;
import com.rouesvm.servback.registry.item.BackpackItemJsonRegistry;
import com.rouesvm.servback.technical.manager.BackpackUUID;
import eu.pb4.polymer.core.api.item.PolymerRecipe;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class BackpackRecipe extends ShapedRecipe implements PolymerRecipe {
    public final RawShapedRecipe raw;
    public final ItemStack result;

    public static final Serializer SERIALIZER = new Serializer();

    public BackpackRecipe(String group, CraftingRecipeCategory category, RawShapedRecipe raw, ItemStack result, boolean showNotification) {
        super(group, category, raw, result, showNotification);
        this.raw = raw;
        this.result = result;
    }

    public RecipeSerializer<? extends ShapedRecipe> getSerializer() {
        return BackpackRecipeRegistry.BACKPACK_CRAFTING_RECIPE;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        ItemStack resultStack = super.craft(craftingRecipeInput, wrapperLookup);

        ItemStack stack = craftingRecipeInput.getStackInSlot(4);
        if (stack.getItem() instanceof ContainerItem containerItem) {
            int id = BackpackItemJsonRegistry.getBackpackId(containerItem);
            int order = BackpackItemJsonRegistry.getBackpackUpgradeOrder(containerItem.getSize());

            ItemStack upgradeStack = BackpackItemJsonRegistry.getBackpackByOrder(id, order + 1).getDefaultStack();
            if (upgradeStack.isEmpty()) return stack.copy();

            upgradeStack = upgradeStack.copy();
            upgradeStack.set(BackpackDataComponentTypes.BACKPACK_UUID, BackpackUUID.getStackUUID(stack));
            upgradeStack.set(DataComponentTypes.ENCHANTMENTS, stack.get(DataComponentTypes.ENCHANTMENTS));

            resultStack = upgradeStack;
        }

        return resultStack;
    }

    @Override
    public @Nullable Recipe<?> getPolymerReplacement(ServerPlayerEntity player) {
        return PolymerRecipe.createCraftingRecipe(this);
    }

    public RawShapedRecipe getRaw() {
        return raw;
    }

    public ItemStack getResult() {
        return result;
    }

    public static class Serializer implements RecipeSerializer<BackpackRecipe>, PolymerObject {
        public static final MapCodec<BackpackRecipe> CODEC = RecordCodecBuilder.mapCodec(
                (instance) ->
                        instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter(BackpackRecipe::getGroup),
                                CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(BackpackRecipe::getCategory),
                                RawShapedRecipe.CODEC.forGetter(BackpackRecipe::getRaw),
                                ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(BackpackRecipe::getResult),
                                Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(BackpackRecipe::showNotification))
                                .apply(instance, BackpackRecipe::new));

        public MapCodec<BackpackRecipe> codec() {
            return CODEC;
        }

        public PacketCodec<RegistryByteBuf, BackpackRecipe> packetCodec() {
            return null;
        }
    }
}
