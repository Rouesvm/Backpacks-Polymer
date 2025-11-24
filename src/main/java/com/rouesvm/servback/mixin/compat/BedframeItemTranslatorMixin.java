package com.rouesvm.servback.mixin.compat;

import lol.sylvie.bedframe.geyser.translator.ItemTranslator;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ItemTranslator.class)
public class BedframeItemTranslatorMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.serverbackpacks$removeOwnItems(this);
    }

    @Unique
    public void serverbackpacks$removeOwnItems(Object instance) {
        try {
            var field = instance.getClass().getDeclaredField("items");
            field.setAccessible(true);
            Map<Identifier, ?> items = (Map<Identifier, ?>) field.get(instance);

            items.entrySet().removeIf(e ->
                    e.getKey().getNamespace().equals("serverbackpacks"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

