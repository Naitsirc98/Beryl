package naitsirc98.beryl.util;

import org.joml.Math;

import static naitsirc98.beryl.util.Asserts.assertTrue;

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

    public static float degrees(float radians) {
        return (float) Math.toDegrees(radians);
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

    public static float asin(float radians) {
        return (float) Math.asin(radians);
    }

    public static float acos(float radians) {
        return (float) Math.acos(radians);
    }

    public static int roundUp(int number, int multiple) {
        assertTrue(number >= 0);
        assertTrue(multiple > 0);
        return ((number + multiple - 1) / multiple) * multiple;
    }

    public static int roundUp2(int number, int multiple) {
        assertTrue(number > 0);
        assertTrue(multiple > 0 && multiple % 2 == 0);
        return (number + multiple - 1) & (-multiple);
    }

    public static int log2(int n){
        assertTrue(n > 0);
        return Integer.SIZE - 1 - Integer.numberOfLeadingZeros(n);
    }
}
