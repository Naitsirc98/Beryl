package naitsirc98.beryl.util;

import org.joml.*;
import org.joml.Math;

import static naitsirc98.beryl.util.Asserts.assertTrue;

public class Maths {

    private static final Random RANDOM = new Random(System.nanoTime());

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

    public static float randomFloat() {
        return RANDOM.nextFloat();
    }

    public static int randomInt(int limit) {
        return RANDOM.nextInt(limit);
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

    public static float lerp(float a, float b, float t) {
        return (1 - t) * a + t * b;
    }

    public static Vector3f lerp(Vector3f start, Vector3f end, float t) {
        final float x = start.x + (end.x - start.x) * t;
        final float y = start.y + (end.y - start.y) * t;
        final float z = start.z + (end.z - start.z) * t;
        return new Vector3f(x, y, z);
    }

    public static Quaternionf lerp(Quaternionf a, Quaternionf b, float t) {

        Quaternionf result = new Quaternionf();

        final float dot = a.w * b.w + a.x * b.x + a.y * b.y + a.z * b.z;

        final float blendI = 1.0f - t;

        if(dot < 0) {
            result.w = blendI * a.w + t * -b.w;
            result.x = blendI * a.x + t * -b.x;
            result.y = blendI * a.y + t * -b.y;
            result.z = blendI * a.z + t * -b.z;
        } else {
            result.w = blendI * a.w + t * b.w;
            result.x = blendI * a.x + t * b.x;
            result.y = blendI * a.y + t * b.y;
            result.z = blendI * a.z + t * b.z;
        }

        return result.normalize();
    }
}
