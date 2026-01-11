package com.rouesvm.servback.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxBlockEntity.class)
public class ShulkerBoxMixin {

    @Inject(method = "canPlaceItemThroughFace", at = @At("HEAD"), cancellable = true)
    public void canInsert(int slot, ItemStack stack, Direction dir, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(stack.getItem().canFitInsideContainerItems());
    }
}
