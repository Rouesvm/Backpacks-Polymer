package com.rouesvm.servback.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ProgressListener;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class WorldSaveMixin {
    @Shadow @NotNull public abstract MinecraftServer getServer();

    @Inject(at=@At("HEAD"),method="save(Lnet/minecraft/util/ProgressListener;ZZ)V")
    private void saveGlobal(ProgressListener progressListener, boolean flush, boolean bl, CallbackInfo ci) {
    }
}
