package com.rouesvm.servback.technical.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rouesvm.servback.content.component.UpgradeContainerComponent;
import com.rouesvm.servback.content.item.UpgradeItem;
import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.registry.BackpackRecipeRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.*;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.recipe.display.SmithingRecipeDisplay;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BackpackUpgradeRecipe implements SmithingRecipe {
    final Ingredient base;
    final Optional<Ingredient> addition;
    final TransmuteRecipeResult result;
    @Nullable
    private IngredientPlacement ingredientPlacement;

    public static final BackpackUpgradeRecipe.Serializer SERIALIZER = new BackpackUpgradeRecipe.Serializer();

    public BackpackUpgradeRecipe(Ingredient base, Optional<Ingredient> addition, TransmuteRecipeResult result) {
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    @Override
    public boolean matches(SmithingRecipeInput input, World world) {
        boolean baseMatch = Ingredient.matches(this.template(), input.template())
                && this.base().test(input.base())
                && Ingredient.matches(this.addition(), input.addition());

        if (!baseMatch) return false;

        ItemStack base = input.base();
        ItemStack addition = input.addition();

        if (base.getItem() instanceof ContainerItem
                && addition.getItem() instanceof UpgradeItem upgradeBaseItem
        ) {
            UpgradeContainerComponent component = base.get(BackpackDataComponentTypes.UPGRADE_CONTAINER);
            Upgrade upgrade = upgradeBaseItem.getUpgradeList(addition).getFirst();

            return component == null || !component.baseUpgrades.contains(upgrade);
        }

        return true;
    }

    public ItemStack craft(SmithingRecipeInput smithingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        ItemStack resultStack = this.result.apply(smithingRecipeInput.base());

        ItemStack base = smithingRecipeInput.base().copy();
        ItemStack addition = smithingRecipeInput.addition();
        if (base.getItem() instanceof ContainerItem
                && addition.getItem() instanceof UpgradeItem upgradeBaseItem
        ) {
            UpgradeContainerComponent component = base.getOrDefault(
                    BackpackDataComponentTypes.UPGRADE_CONTAINER,
                    UpgradeContainerComponent.of(new ArrayList<>())
            );

            Upgrade upgrade = upgradeBaseItem.getUpgradeList(addition).getFirst();

            if (!component.baseUpgrades.contains(upgrade)) {
                component.baseUpgrades.add(upgradeBaseItem.getUpgradeList(addition).getFirst());
                base.set(BackpackDataComponentTypes.UPGRADE_CONTAINER, component);
                resultStack = base;
            }
        }

        return resultStack;
    }

    public TransmuteRecipeResult result() {
        return result;
    }

    public Optional<Ingredient> template() {
        return Optional.empty();
    }

    public Ingredient base() {
        return this.base;
    }

    public Optional<Ingredient> addition() {
        return this.addition;
    }

    public RecipeSerializer<BackpackUpgradeRecipe> getSerializer() {
        return BackpackRecipeRegistry.BACKPACK_UPGRADE_RECIPE;
    }

    public IngredientPlacement getIngredientPlacement() {
        if (this.ingredientPlacement == null) {
            this.ingredientPlacement = IngredientPlacement.forMultipleSlots(List.of(Optional.empty(), Optional.of(this.base), this.addition));
        }

        return this.ingredientPlacement;
    }

    public List<RecipeDisplay> getDisplays() {
        return List.of(new SmithingRecipeDisplay(Ingredient.toDisplay(Optional.empty()), this.base.toDisplay(), Ingredient.toDisplay(this.addition), this.result.createSlotDisplay(), new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)));
    }

    public static class Serializer implements RecipeSerializer<BackpackUpgradeRecipe> {
        private static final MapCodec<BackpackUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) ->
                instance.group(Ingredient.CODEC.fieldOf("base").forGetter(BackpackUpgradeRecipe::base),
                        Ingredient.CODEC.optionalFieldOf("addition").forGetter(BackpackUpgradeRecipe::addition),
                        TransmuteRecipeResult.CODEC.fieldOf("result").forGetter(BackpackUpgradeRecipe::result))
                        .apply(instance, BackpackUpgradeRecipe::new));

        public static final PacketCodec<RegistryByteBuf, BackpackUpgradeRecipe> PACKET_CODEC;

        public MapCodec<BackpackUpgradeRecipe> codec() {
            return CODEC;
        }

        public PacketCodec<RegistryByteBuf, BackpackUpgradeRecipe> packetCodec() {
            return PACKET_CODEC;
        }

        static {
            PACKET_CODEC = PacketCodec.tuple(Ingredient.PACKET_CODEC, BackpackUpgradeRecipe::base,
                    Ingredient.OPTIONAL_PACKET_CODEC, BackpackUpgradeRecipe::addition,
                    TransmuteRecipeResult.PACKET_CODEC, BackpackUpgradeRecipe::result,
                    BackpackUpgradeRecipe::new);
        }
    }
}
