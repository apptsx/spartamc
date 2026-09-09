package com.minecraft.core.api.collectible.util.math;

public class CosSineTable {

    private static final double[] COS = new double[361];
    private static final double[] SIN = new double[361];

    static {
        for (int angle = 0; angle <= 360; angle++) {
            COS[angle] = Math.cos(Math.toRadians(angle));
            SIN[angle] = Math.sin(Math.toRadians(angle));
        }
    }

    public static double cos(int angle) {
        return COS[angle % 360];
    }

    public static double sin(int angle) {
        return SIN[angle % 360];
    }
}