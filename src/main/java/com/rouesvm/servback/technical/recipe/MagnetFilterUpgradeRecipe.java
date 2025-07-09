package com.rouesvm.servback.technical.recipe;

import com.rouesvm.servback.content.component.UpgradeContainerComponent;
import com.rouesvm.servback.content.item.UpgradeItem;
import com.rouesvm.servback.content.upgrade.impl.MagnetUpgrade;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.registry.BackpackRecipeRegistry;
import com.rouesvm.servback.registry.BackpackUpgradeRegistry;
import com.rouesvm.servback.registry.item.BackpackItemRegistry;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class MagnetFilterUpgradeRecipe extends SpecialCraftingRecipe {
    public static final RecipeSerializer<MagnetFilterUpgradeRecipe> SERIALIZER = new MagnetFilterUpgradeRecipe.Serializer(MagnetFilterUpgradeRecipe::new);

    public MagnetFilterUpgradeRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        if (input.getStackCount() == 0) return false;

        int magnetCount = 0;
        int count = 0;
        for (int i = 0; i < input.getStackCount(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.getItem() instanceof UpgradeItem) magnetCount++;
            if (!stack.isEmpty()) count++;
        }

        return count > 0 && count <= 4 && magnetCount == 1;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        ItemStack center = ItemStack.EMPTY;
        ItemStack result = ItemStack.EMPTY;

        List<Item> list = new ArrayList<>();

        int size = 0;
        for (int i = 0; i < input.getStackCount() && size < 4; i++) {
            ItemStack stack = input.getStackInSlot(i);

            if (stack.isEmpty()) continue;
            if (stack.isOf(BackpackItemRegistry.MAGNET_UPGRADE)) {
                center = stack;
                continue;
            }

            list.add(stack.getItem());
            size++;
        }

        if (size != 0) {
            result = center.copy();
            MagnetUpgrade newUpgrade = BackpackUpgradeRegistry.MAGNET.create();
            newUpgrade.setList(list);;
            UpgradeContainerComponent component = new UpgradeContainerComponent(List.of(newUpgrade));

            result.set(BackpackDataComponentTypes.UPGRADE_CONTAINER_COMPONENT_COMPONENT_TYPE, component);

            return result;
        }

        return result;
    }

    @Override
    public RecipeSerializer<MagnetFilterUpgradeRecipe> getSerializer() {
        return BackpackRecipeRegistry.MAGNET_FILTER_APPLIER_RECIPE;
    }

    @Override
    public CraftingRecipeCategory getCategory() {
        return CraftingRecipeCategory.MISC;
    }

    public static class Serializer extends SpecialRecipeSerializer<MagnetFilterUpgradeRecipe> implements PolymerObject {
        public Serializer(Factory<MagnetFilterUpgradeRecipe> factory) {
            super(factory);
        }

        @Override
        public PacketCodec<RegistryByteBuf, MagnetFilterUpgradeRecipe> packetCodec() {
            return null;
        }
    }
}
