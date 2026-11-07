package dev.qoder.nocap.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import java.util.Locale;

public final class SpeedTracker {
    private static final double SMOOTHING = 0.3D;
    private static Entity lastTarget;
    private static World lastWorld;
    private static double lastX;
    private static double lastY;
    private static double lastZ;
    private static double speed;

    private SpeedTracker() {
    }

    public static void tick(MinecraftClient client) {
        if (client.player == null) {
            reset();
            return;
        }

        Entity target = client.player.getRootVehicle();
        double x = target.getX();
        double y = target.getY();
        double z = target.getZ();

        if (target != lastTarget || target.world != lastWorld) {
            lastTarget = target;
            lastWorld = target.world;
            lastX = x;
            lastY = y;
            lastZ = z;
            speed = 0.0D;
            return;
        }

        double dx = x - lastX;
        double dy = y - lastY;
        double dz = z - lastZ;
        double moved = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (!Double.isFinite(moved) || moved > 64.0D) {
            lastX = x;
            lastY = y;
            lastZ = z;
            speed = 0.0D;
            return;
        }

        speed += (moved * 20.0D - speed) * SMOOTHING;
        if (speed < 0.05D) {
            speed = 0.0D;
        }

        lastX = x;
        lastY = y;
        lastZ = z;
    }

    public static void render(MinecraftClient client, MatrixStack matrices) {
        if (client.player == null || client.options.hudHidden) {
            return;
        }

        String text = String.format(Locale.ROOT, "速度: %.1f 格/秒", speed);
        client.textRenderer.drawWithShadow(matrices, text, 4.0F, 4.0F, 0xFFFFFF);
    }

    private static void reset() {
        lastTarget = null;
        lastWorld = null;
        lastX = 0.0D;
        lastY = 0.0D;
        lastZ = 0.0D;
        speed = 0.0D;
    }
}
