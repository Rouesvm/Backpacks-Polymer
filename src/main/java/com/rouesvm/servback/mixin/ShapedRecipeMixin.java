package com.rouesvm.servback.mixin;

import com.rouesvm.servback.technical.config.Configuration;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapedRecipe.class)
public class ShapedRecipeMixin {
    @Shadow @Final
    private ItemStackTemplate result;

    @Inject(method = "matches(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Z", at = @At("HEAD"), cancellable = true)
    public void matches(CraftingInput craftingRecipeInput, Level world, CallbackInfoReturnable<Boolean> cir) {
        if (Configuration.isDisabled(this.result.item().value())) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "assemble(Lnet/minecraft/world/item/crafting/CraftingInput;)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    public void craft(CraftingInput input, CallbackInfoReturnable<ItemStack> cir) {
        if (Configuration.isDisabled(this.result.item().value())) {
            cir.setReturnValue(ItemStack.EMPTY);
            cir.cancel();
        }
    }
}
