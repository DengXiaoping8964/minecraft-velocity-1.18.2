package dev.qoder.nocap.mixin;

import dev.qoder.nocap.SpeedLimits;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityMixin {
    @Inject(method = "getMaxSpeed", at = @At("HEAD"), cancellable = true)
    private void nocap$raiseMaxSpeed(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue((double) SpeedLimits.MINECART_MAX_BPT);
    }

    @Redirect(method = "moveOnRail", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(DD)D"))
    private double nocap$removeRailSpeedCap(double a, double b) {
        AbstractMinecartEntity self = (AbstractMinecartEntity) (Object) this;
        boolean hasPassengers = self.hasPassengers();
        double factor = hasPassengers ? 0.75 : 1.0;
        return Math.min(SpeedLimits.MINECART_MAX_BPT / factor, b);
    }
}
