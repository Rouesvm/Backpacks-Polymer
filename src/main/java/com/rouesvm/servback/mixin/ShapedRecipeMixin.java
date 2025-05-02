package com.rouesvm.servback.mixin;

import com.rouesvm.servback.items.ContainerItem;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeMixin {

    @Shadow @Final
    ItemStack result;

    @Inject(method = "craft(Lnet/minecraft/recipe/input/CraftingRecipeInput;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private void onCraft(CraftingRecipeInput inventory, RegistryWrapper.WrapperLookup registriesLookup, CallbackInfoReturnable<ItemStack> callBack) {
        ItemStack resultStack = this.result.copy();

        if (resultStack.getItem() instanceof ContainerItem) {
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (stack.getItem() instanceof ContainerItem) {
                    resultStack.set(BackpackDataComponentTypes.UUID_TYPE, stack.get(BackpackDataComponentTypes.UUID_TYPE));
                    resultStack.set(DataComponentTypes.ENCHANTMENTS, stack.get(DataComponentTypes.ENCHANTMENTS));
                    callBack.setReturnValue(resultStack);
                    break;
                }
            }
        }
    }
}
