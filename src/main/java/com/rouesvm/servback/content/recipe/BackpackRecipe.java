package com.rouesvm.servback.content.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.registry.BackpackRecipeRegistry;
import com.rouesvm.servback.registry.item.BackpackItemJsonRegistry;
import com.rouesvm.servback.technical.manager.BackpackUUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

public class BackpackRecipe extends ShapedRecipe {
    public final ShapedRecipePattern raw;
    public final ItemStack result;

    public static final com.rouesvm.servback.content.recipe.BackpackRecipe.Serializer SERIALIZER = new com.rouesvm.servback.content.recipe.BackpackRecipe.Serializer();

    public BackpackRecipe(String group, CraftingBookCategory category, ShapedRecipePattern raw, ItemStack result, boolean showNotification) {
        super(group, category, raw, result, showNotification);
        this.raw = raw;
        this.result = result;
    }

    public RecipeSerializer<? extends ShapedRecipe> getSerializer() {
        return BackpackRecipeRegistry.BACKPACK_CRAFTING_RECIPE;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingRecipeInput, HolderLookup.Provider wrapperLookup) {
        ItemStack resultStack = super.assemble(craftingRecipeInput, wrapperLookup);

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

    public ShapedRecipePattern getRaw() {
        return raw;
    }

    public ItemStack getResult() {
        return result;
    }

    public static class Serializer implements RecipeSerializer<BackpackRecipe> {
        public static final MapCodec<BackpackRecipe> CODEC = RecordCodecBuilder.mapCodec(
                (instance) ->
                        instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter(BackpackRecipe::group),
                                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(BackpackRecipe::category),
                                ShapedRecipePattern.MAP_CODEC.forGetter(BackpackRecipe::getRaw),
                                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(BackpackRecipe::getResult),
                                Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(BackpackRecipe::showNotification))
                                .apply(instance, BackpackRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, BackpackRecipe> PACKET_CODEC = StreamCodec.of(BackpackRecipe.Serializer::write, BackpackRecipe.Serializer::read);

        public MapCodec<BackpackRecipe> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, BackpackRecipe> streamCodec() {
            return PACKET_CODEC;
        }

        private static BackpackRecipe read(RegistryFriendlyByteBuf buf) {
            String string = buf.readUtf();
            CraftingBookCategory craftingRecipeCategory = buf.readEnum(CraftingBookCategory.class);
            ShapedRecipePattern rawShapedRecipe = ShapedRecipePattern.STREAM_CODEC.decode(buf);
            ItemStack itemStack = ItemStack.STREAM_CODEC.decode(buf);
            boolean bl = buf.readBoolean();
            return new BackpackRecipe(string, craftingRecipeCategory, rawShapedRecipe, itemStack, bl);
        }

        private static void write(RegistryFriendlyByteBuf buf, BackpackRecipe recipe) {
            buf.writeUtf(recipe.group());
            buf.writeEnum(recipe.category());
            ShapedRecipePattern.STREAM_CODEC.encode(buf, recipe.getRaw());
            ItemStack.STREAM_CODEC.encode(buf, recipe.getResult());
            buf.writeBoolean(recipe.showNotification());
        }
    }
}
