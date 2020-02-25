package naitsirc98.beryl.util;

import org.joml.Math;

public class Maths {

    public static int clamp(int min, int max, int value) {
        return Math.max(min, Math.min(max, value));
    }

    public static float clamp(float min, float max, float value) {
        return Math.max(min, Math.min(max, value));
    }

    public static float radians(float angle) {
        return (float) Math.toRadians(angle);
    }

    public static float sin(float radians) {
        return (float) Math.sin(radians);
    }

    public static float cos(float radians) {
        return (float) Math.cos(radians);
    }

    public static float tan(float radians) {
        return (float) Math.tan(radians);
    }
}
