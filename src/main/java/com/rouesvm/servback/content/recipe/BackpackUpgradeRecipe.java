package com.rouesvm.servback.content.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rouesvm.servback.content.component.UpgradeContainerComponent;
import com.rouesvm.servback.content.item.UpgradeItem;
import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.registry.BackpackRecipeRegistry;
import eu.pb4.polymer.core.api.item.PolymerRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BackpackUpgradeRecipe implements SmithingRecipe, PolymerRecipe {
    final Ingredient base;
    final Ingredient addition;
    final ItemStack result;

    public static final Serializer SERIALIZER = new Serializer();

    public BackpackUpgradeRecipe(Ingredient base, Ingredient addition, ItemStack result) {
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    @Override
    public boolean matches(SmithingRecipeInput input, World world) {
        boolean baseMatch = this.base().test(input.base())
                && ItemStack.areItemsAndComponentsEqual(this.addition().getMatchingStacks()[0], input.addition());

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
            else return !component.getBaseUpgrades().contains(upgrade);
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

        List<Upgrade> upgrades = new ArrayList<>(component.getBaseUpgrades());
        Upgrade upgrade = upgradeBaseItem.getUpgrade(addition);

        if (upgrades.contains(upgrade)) return resultStack;

        upgrades.add(upgrade);
        resultStack.set(BackpackDataComponentTypes.UPGRADE_CONTAINER, UpgradeContainerComponent.of(upgrades));

        return resultStack;
    }

    @Override
    public @Nullable Recipe<?> getPolymerReplacement(ServerPlayerEntity player) {
        return PolymerRecipe.createSmithingRecipe(this);
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return result;
    }

    public ItemStack result() {
        return result;
    }

    public Ingredient base() {
        return this.base;
    }

    public Ingredient addition() {
        return this.addition;
    }

    public RecipeSerializer<BackpackUpgradeRecipe> getSerializer() {
        return BackpackRecipeRegistry.BACKPACK_UPGRADE_RECIPE;
    }

    @Override
    public boolean testTemplate(ItemStack stack) {
        return false;
    }

    @Override
    public boolean testBase(ItemStack stack) {
        return false;
    }

    @Override
    public boolean testAddition(ItemStack stack) {
        return false;
    }

    public static class Serializer implements RecipeSerializer<BackpackUpgradeRecipe> {
        private static final MapCodec<BackpackUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) ->
                instance.group(Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("base").forGetter(BackpackUpgradeRecipe::base),
                        Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("addition").forGetter(BackpackUpgradeRecipe::addition),
                        ItemStack.CODEC.fieldOf("result").forGetter(BackpackUpgradeRecipe::result))
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
                    Ingredient.PACKET_CODEC, BackpackUpgradeRecipe::addition,
                    ItemStack.PACKET_CODEC, BackpackUpgradeRecipe::result,
                    BackpackUpgradeRecipe::new);
        }
    }
}
