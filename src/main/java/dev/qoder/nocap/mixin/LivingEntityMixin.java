package dev.qoder.nocap.mixin;

import dev.qoder.nocap.BoostState;
import dev.qoder.nocap.SpeedLimits;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements BoostState {
    @Unique
    private int nocap$boostTicks;

    @Unique
    private long nocap$lastBoostTick = -1000;

    @Override
    public int nocap$getBoostTicks() {
        return nocap$boostTicks;
    }

    @Override
    public void nocap$setBoostTicks(int ticks) {
        nocap$boostTicks = ticks;
    }

    @Override
    public long nocap$getLastBoostTick() {
        return nocap$lastBoostTick;
    }

    @Override
    public void nocap$setLastBoostTick(long tick) {
        nocap$lastBoostTick = tick;
    }

    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;multiply(DDD)Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d nocap$removeGlideDrag(Vec3d velocity, double horizontalDrag, double verticalDrag, double zDrag) {
        if (Math.abs(horizontalDrag - 0.99D) < 0.001D
                && Math.abs(verticalDrag - 0.98D) < 0.001D
                && Math.abs(zDrag - 0.99D) < 0.001D
                && ((LivingEntity) (Object) this) instanceof PlayerEntity
                && nocap$getBoostTicks() > 0) {
            return velocity.multiply(1.0D, verticalDrag, 1.0D);
        }

        return velocity.multiply(horizontalDrag, verticalDrag, zDrag);
    }

    @Inject(method = "travel", at = @At("TAIL"))
    private void nocap$afterTravel(Vec3d movementInput, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof PlayerEntity)) {
            return;
        }

        if (!self.isFallFlying()) {
            nocap$setBoostTicks(0);
            return;
        }

        if (self.getWorld().getTime() - nocap$getLastBoostTick() > SpeedLimits.BOOST_GAP_TICKS) {
            nocap$setBoostTicks(0);
        }

        Vec3d velocity = self.getVelocity();
        double length = velocity.length();
        if (length > SpeedLimits.ELYTRA_MAX_BPT) {
            self.setVelocity(velocity.multiply(SpeedLimits.ELYTRA_MAX_BPT / length));
        }
    }
}
