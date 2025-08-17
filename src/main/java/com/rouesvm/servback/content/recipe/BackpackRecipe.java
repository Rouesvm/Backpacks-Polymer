package com.rouesvm.servback.content.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.registry.BackpackRecipeRegistry;
import com.rouesvm.servback.registry.item.BackpackItemJsonRegistry;
import com.rouesvm.servback.technical.manager.BackpackUUID;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;

public class BackpackRecipe extends ShapedRecipe {
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
            upgradeStack = upgradeStack.copy();
            upgradeStack.set(BackpackDataComponentTypes.BACKPACK_UUID, BackpackUUID.getStackUUID(stack));
            upgradeStack.set(DataComponentTypes.ENCHANTMENTS, stack.get(DataComponentTypes.ENCHANTMENTS));

            resultStack = upgradeStack;
        }

        return resultStack;
    }

    public RawShapedRecipe getRaw() {
        return raw;
    }

    public ItemStack getResult() {
        return result;
    }

    public static class Serializer implements RecipeSerializer<BackpackRecipe> {
        public static final MapCodec<BackpackRecipe> CODEC = RecordCodecBuilder.mapCodec(
                (instance) ->
                        instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter(BackpackRecipe::getGroup),
                                CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(BackpackRecipe::getCategory),
                                RawShapedRecipe.CODEC.forGetter(BackpackRecipe::getRaw),
                                ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(BackpackRecipe::getResult),
                                Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(BackpackRecipe::showNotification))
                                .apply(instance, BackpackRecipe::new));

        public static final PacketCodec<RegistryByteBuf, BackpackRecipe> PACKET_CODEC = PacketCodec.ofStatic(BackpackRecipe.Serializer::write, BackpackRecipe.Serializer::read);

        public MapCodec<BackpackRecipe> codec() {
            return CODEC;
        }

        public PacketCodec<RegistryByteBuf, BackpackRecipe> packetCodec() {
            return PACKET_CODEC;
        }

        private static BackpackRecipe read(RegistryByteBuf buf) {
            String string = buf.readString();
            CraftingRecipeCategory craftingRecipeCategory = buf.readEnumConstant(CraftingRecipeCategory.class);
            RawShapedRecipe rawShapedRecipe = RawShapedRecipe.PACKET_CODEC.decode(buf);
            ItemStack itemStack = ItemStack.PACKET_CODEC.decode(buf);
            boolean bl = buf.readBoolean();
            return new BackpackRecipe(string, craftingRecipeCategory, rawShapedRecipe, itemStack, bl);
        }

        private static void write(RegistryByteBuf buf, BackpackRecipe recipe) {
            buf.writeString(recipe.getGroup());
            buf.writeEnumConstant(recipe.getCategory());
            RawShapedRecipe.PACKET_CODEC.encode(buf, recipe.getRaw());
            ItemStack.PACKET_CODEC.encode(buf, recipe.getResult());
            buf.writeBoolean(recipe.showNotification());
        }
    }
}
