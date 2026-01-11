package com.rouesvm.servback.mixin;

import com.rouesvm.servback.technical.cosmetic.BackInterface;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements BackInterface {
    @Unique private double prevX = 0;
    @Unique private double prevZ = 0;
    @Unique private float customBodyYaw = 0;

    @Inject(method = "tick", at = @At("RETURN"))
    private void rotationTick(CallbackInfo ci) {
        var self = LivingEntity.class.cast(this);
        var isPlayer = (self instanceof Player);

        if ((prevX != 0 && prevZ != 0) && isPlayer)
            backpacks$tickMovement(self);
        else this.customBodyYaw = self.yBodyRot;

        prevX = self.getX();
        prevZ = self.getZ();
    }

    @Unique
    private void backpacks$tickMovement(final LivingEntity entity) {
        float currentYaw = entity.getYRot();
        double dx = entity.getX() - this.prevX;
        double dz = entity.getZ() - this.prevZ;
        double moveSq = dx * dx + dz * dz;

        float targetYaw = this.customBodyYaw;

        if (moveSq > 0.0025) {
            float movementYaw = (float) (Math.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0f;
            float yawDiff = Mth.wrapDegrees(currentYaw - movementYaw);

            if (yawDiff > 95f && yawDiff < 265f) movementYaw -= 180f;
            targetYaw = movementYaw;
        }

        this.backpacks$turnBody(targetYaw, currentYaw);
    }

    @Unique
    public void backpacks$turnBody(float bodyRotation, float yaw) {
        float diff = Mth.wrapDegrees(bodyRotation - this.customBodyYaw);
        this.customBodyYaw += diff * 0.3f;

        float clampDiff = Mth.wrapDegrees(yaw - this.customBodyYaw);
        clampDiff = Mth.clamp(clampDiff, -75f, 75f);

        this.customBodyYaw = yaw - clampDiff;

        if (clampDiff * clampDiff > 2500f) {
            this.customBodyYaw += clampDiff * 0.25f;
        }
    }

    @Override
    @Unique
    public float backpacks$bodyYaw() {
        return customBodyYaw;
    }
}
