package dev.qoder.nocap.mixin;

import dev.qoder.nocap.BoostState;
import dev.qoder.nocap.SpeedLimits;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(net.minecraft.entity.projectile.FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin {
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"
            )
    )
    private void nocap$accelerateElytra(LivingEntity glider, Vec3d vanillaVelocity) {
        if (!(glider instanceof PlayerEntity) || !glider.isFallFlying() || !(glider instanceof BoostState)) {
            glider.setVelocity(vanillaVelocity);
            return;
        }

        BoostState state = (BoostState) glider;
        long time = glider.getWorld().getTime();

        if (state.nocap$getLastBoostTick() != time) {
            if (time - state.nocap$getLastBoostTick() > SpeedLimits.BOOST_GAP_TICKS) {
                state.nocap$setBoostTicks(0);
            }
            state.nocap$setBoostTicks(Math.min(SpeedLimits.RAMP_TICKS, state.nocap$getBoostTicks() + 1));
            state.nocap$setLastBoostTick(time);
        }

        double progress = (double) state.nocap$getBoostTicks() / SpeedLimits.RAMP_TICKS;
        double targetSpeed = SpeedLimits.BASE_BPT
                + (SpeedLimits.ELYTRA_MAX_BPT - SpeedLimits.BASE_BPT) * progress;
        double currentSpeed = glider.getVelocity().length();
        double desiredSpeed = Math.min(SpeedLimits.ELYTRA_MAX_BPT, Math.max(currentSpeed, targetSpeed));

        glider.setVelocity(glider.getRotationVector().multiply(desiredSpeed));
    }
}
