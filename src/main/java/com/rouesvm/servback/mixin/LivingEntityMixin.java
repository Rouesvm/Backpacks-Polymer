package com.rouesvm.servback.mixin;

import com.rouesvm.servback.technical.cosmetic.BackInterface;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
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
        var isPlayer = (self instanceof PlayerEntity);

        if ((prevX != 0 && prevZ != 0) && isPlayer)
            backpacks$tickMovement(self);
        else this.customBodyYaw = self.bodyYaw;

        prevX = self.getX();
        prevZ = self.getZ();
    }

    @Unique
    private void backpacks$tickMovement(final LivingEntity entity) {
        float currentYaw = entity.getYaw();
        double dx = entity.getX() - this.prevX;
        double dz = entity.getZ() - this.prevZ;
        double moveSq = dx * dx + dz * dz;

        float targetYaw = this.customBodyYaw;

        if (moveSq > 0.0025) {
            float movementYaw = (float) (Math.atan2(dz, dx) * MathHelper.DEGREES_PER_RADIAN) - 90.0f;
            float yawDiff = MathHelper.wrapDegrees(currentYaw - movementYaw);

            if (yawDiff > 95f && yawDiff < 265f) movementYaw -= 180f;
            targetYaw = movementYaw;
        }

        this.backpacks$turnBody(targetYaw, currentYaw);
    }

    @Unique
    public void backpacks$turnBody(float bodyRotation, float yaw) {
        float diff = MathHelper.wrapDegrees(bodyRotation - this.customBodyYaw);
        this.customBodyYaw += diff * 0.3f;

        float clampDiff = MathHelper.wrapDegrees(yaw - this.customBodyYaw);
        clampDiff = MathHelper.clamp(clampDiff, -75f, 75f);

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
