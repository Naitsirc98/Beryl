package naitsirc98.beryl.util;

import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3f;

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

    public static float barryCentric(Vector3f p1, Vector3f p2, Vector3f p3, Vector2f pos) {

        final float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
        final float l1 = ((p2.z - p3.z) * (pos.x - p3.x) + (p3.x - p2.x) * (pos.y - p3.z)) / det;
        final float l2 = ((p3.z - p1.z) * (pos.x - p3.x) + (p1.x - p3.x) * (pos.y - p3.z)) / det;
        final float l3 = 1.0f - l1 - l2;

        return l1 * p1.y + l2 * p2.y + l3 * p3.y;
    }

    public static float barryCentric(float x1, float y1, float z1,
                                     float x2, float y2, float z2,
                                     float x3, float y3, float z3,
                                     float x, float z) {

        final float det = (z2 - z3) * (x1 - x3) + (x3 - x2) * (z1 -z3);
        final float l1 = ((z2 - z3) * (x - x3) + (x3 - x2) * (z - z3)) / det;
        final float l2 = ((z3 - z1) * (x - x3) + (x1 - x3) * (z - z3)) / det;
        final float l3 = 1.0f - l1 - l2;

        return l1 * y1 + l2 * y2 + l3 * y3;
    }
}
