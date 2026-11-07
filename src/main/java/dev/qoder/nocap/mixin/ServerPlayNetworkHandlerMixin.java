package dev.qoder.nocap.mixin;

import dev.qoder.nocap.BoostState;
import dev.qoder.nocap.SpeedLimits;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow
    @Final
    private ServerPlayerEntity player;

    @Invoker("isHost")
    public abstract boolean nocap$isHost();

    @Redirect(method = "onPlayerMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean nocap$skipBoostedElytraCheck(GameRules rules, GameRules.Key key) {
        PlayerEntity self = this.player;
        if (self instanceof BoostState state
                && self.isFallFlying()
                && self.getWorld().getTime() - state.nocap$getLastBoostTick() <= SpeedLimits.BOOST_GAP_TICKS) {
            return true;
        }

        return rules.getBoolean((GameRules.Key<GameRules.BooleanRule>) key);
    }

    @Redirect(method = "onVehicleMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;isHost()Z"))
    private boolean nocap$allowServerDrivenMinecart(ServerPlayNetworkHandler handler) {
        Entity rootVehicle = this.player.getRootVehicle();
        return nocap$isHost() || rootVehicle instanceof AbstractMinecartEntity && rootVehicle.hasPassengers();
    }
}
