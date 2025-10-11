package com.rouesvm.servback.mixin;

import com.rouesvm.servback.registry.item.BackpackItemRegistry;
import com.rouesvm.servback.technical.BackpackGlobalLinker;
import com.rouesvm.servback.technical.config.Configuration;
import net.minecraft.entity.ItemEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (!Configuration.isDisabled(BackpackItemRegistry.GLOBAL_BACKPACK)) {
            ItemEntity source = (ItemEntity) (Object) this;
            World world = source.getEntityWorld();
            BackpackGlobalLinker.testLink(source, world);
        }
    }
}
