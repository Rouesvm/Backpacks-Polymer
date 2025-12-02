package com.rouesvm.servback.content.recipe;

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
            Upgrade upgrade = upgradeBaseItem.getUpgrade(addition);

            if (component == null)
                return true;
            else return !component.baseUpgrades().contains(upgrade);
        }

        return true;
    }

    @Override
    public ItemStack craft(SmithingRecipeInput smithingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        ItemStack base = smithingRecipeInput.base();
        ItemStack addition = smithingRecipeInput.addition();

        ItemStack resultStack = base.copy();

        if (!(base.getItem() instanceof ContainerItem)
                || !(addition.getItem() instanceof UpgradeItem upgradeBaseItem)
        ) return resultStack;

        UpgradeContainerComponent component = base.getOrDefault(
                BackpackDataComponentTypes.UPGRADE_CONTAINER,
                UpgradeContainerComponent.of(new ArrayList<>())
        );

        List<Upgrade> upgrades = new ArrayList<>(component.baseUpgrades());
        Upgrade upgrade = upgradeBaseItem.getUpgrade(addition);

        if (upgrades.contains(upgrade)) return resultStack;

        upgrades.add(upgrade);
        resultStack.set(BackpackDataComponentTypes.UPGRADE_CONTAINER, UpgradeContainerComponent.of(upgrades));

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
