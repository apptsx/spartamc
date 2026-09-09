package com.minecraft.core.api.collectible.util.math;

import org.bukkit.util.EulerAngle;

public class MathUtils {

    public static EulerAngle angle(double x, double y, double z) {
        return new EulerAngle(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z));
    }

    public static float clampYaw(float yaw) {
        while (yaw < -180.0F) {
            yaw += 360.0F;
        }
        while (yaw >= 180.0F) {
            yaw -= 360.0F;
        }

        return yaw;
    }

    public static double square(double toSquare) {
        return toSquare * toSquare;
    }
}
