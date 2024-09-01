package com.rouesvm.servback.mixin;

import com.rouesvm.servback.items.ContainerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeMixin {
    @Shadow
    public abstract ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup);

    @Inject(method = "craft", at = @At("HEAD"), cancellable = true)
    private void onCraft(CraftingRecipeInput inventory, RegistryWrapper.WrapperLookup registriesLookup, CallbackInfoReturnable<ItemStack> callBack) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ContainerItem) {
                ItemStack resultStack = this.getResult(registriesLookup).copy();
                if (resultStack.getItem() instanceof ContainerItem) {
                    resultStack.set(DataComponentTypes.CONTAINER, stack.get(DataComponentTypes.CONTAINER));
                    callBack.setReturnValue(resultStack);
                }
            }
        }
    }
}
