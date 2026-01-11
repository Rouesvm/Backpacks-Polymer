package com.rouesvm.servback.mixin;

import com.rouesvm.servback.technical.BackpackGlobalLinker;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        ItemEntity source = (ItemEntity) (Object) this;
        Level world = source.level();
        BackpackGlobalLinker.testLink(source, world);
    }
}
