package dev.qoder.nocap.mixin;

import dev.qoder.nocap.client.SpeedTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "tick(Z)V", at = @At("TAIL"))
    private void nocap$tickSpeed(boolean paused, CallbackInfo ci) {
        if (!paused) {
            SpeedTracker.tick(client);
        }
    }

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;F)V", at = @At("TAIL"))
    private void nocap$renderSpeed(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        SpeedTracker.render(client, matrices);
    }
}
