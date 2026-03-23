package com.rouesvm.servback.content.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rouesvm.servback.content.component.UpgradeContainerComponent;
import com.rouesvm.servback.content.item.UpgradeItem;
import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.registry.BackpackRecipeRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BackpackUpgradeRecipe implements SmithingRecipe {
    private static final MapCodec<BackpackUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(Ingredient.CODEC.fieldOf("base").forGetter(BackpackUpgradeRecipe::baseIngredient),
                            Ingredient.CODEC.optionalFieldOf("addition").forGetter(BackpackUpgradeRecipe::additionIngredient),
                            ItemStackTemplate.CODEC.fieldOf("result").forGetter(BackpackUpgradeRecipe::getResult))
                    .apply(instance, BackpackUpgradeRecipe::new));

    final Ingredient base;
    final Optional<Ingredient> addition;
    final ItemStackTemplate result;
    @Nullable
    private PlacementInfo ingredientPlacement;

    public static final RecipeSerializer<BackpackUpgradeRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, null);

    public BackpackUpgradeRecipe(Ingredient base, Optional<Ingredient> addition, ItemStackTemplate result) {
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    @Override
    public boolean matches(SmithingRecipeInput input, @NotNull Level world) {
        boolean baseMatch = Ingredient.testOptionalIngredient(this.templateIngredient(), input.template())
                && this.baseIngredient().test(input.base())
                && Ingredient.testOptionalIngredient(this.additionIngredient(), input.addition());

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
    public @NotNull ItemStack assemble(SmithingRecipeInput recipeInput) {
        ItemStack base = recipeInput.base();
        ItemStack addition = recipeInput.addition();

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

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    public ItemStackTemplate getResult() {
        return result;
    }

    public @NotNull Optional<Ingredient> templateIngredient() {
        return Optional.empty();
    }

    public @NotNull Ingredient baseIngredient() {
        return this.base;
    }

    public @NotNull Optional<Ingredient> additionIngredient() {
        return this.addition;
    }

    public @NotNull RecipeSerializer<@NotNull BackpackUpgradeRecipe> getSerializer() {
        return BackpackRecipeRegistry.BACKPACK_UPGRADE_RECIPE;
    }

    public @NotNull PlacementInfo placementInfo() {
        if (this.ingredientPlacement == null) {
            this.ingredientPlacement = PlacementInfo.createFromOptionals(List.of(Optional.empty(), Optional.of(this.base), this.addition));
        }

        return this.ingredientPlacement;
    }

    public @NotNull List<RecipeDisplay> display() {
        return List.of(new SmithingRecipeDisplay(Ingredient.optionalIngredientToDisplay(Optional.empty()), this.base.display(), Ingredient.optionalIngredientToDisplay(this.addition), this.result.display(), new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)));
    }
}
