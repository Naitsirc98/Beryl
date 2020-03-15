package naitsirc98.beryl.util;

import naitsirc98.beryl.util.types.ByteSize;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;

@ByteSize.Static(Color.SIZEOF)
public final class Color implements Cloneable, ByteSize {

    public static final int SIZEOF = 4 * FLOAT32_SIZEOF;

    public static final Color NONE = new Color(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
    public static final Color WHITE = new Color(1, 1, 1);
    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color RED = new Color(1, 0, 0);
    public static final Color GREEN = new Color(0, 1, 0);
    public static final Color BLUE = new Color(0, 0, 1);
    // TODO: more...

    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;

    public Color() {
        this(Color.WHITE);
    }

    public Color(Color other) {
        this.red = other.red;
        this.green = other.green;
        this.blue = other.blue;
        this.alpha = other.alpha;
    }

    public Color(float rgba) {
        red = rgba;
        green = rgba;
        blue = rgba;
        alpha = rgba;
    }

    public Color(float r, float g, float b) {
        this(r, g, b, 1.0f);
    }

    public Color(float r, float g, float b, float a) {
        this.red = r;
        this.green = g;
        this.blue = b;
        this.alpha = a;
    }

    public float red() {
        return red;
    }

    public float green() {
        return green;
    }

    public float blue() {
        return blue;
    }

    public float alpha() {
        return alpha;
    }

    public Color intensify(float factor) {
        return new Color(red * factor, green * factor, blue * factor, alpha);
    }

    @Override
    public int sizeof() {
        return 4 * Float.BYTES;
    }

    @Override
    public Color clone() {
        return new Color(this);
    }

    public FloatBuffer getRGB(FloatBuffer buffer) {
        return buffer.put(red).put(green).put(blue);
    }

    public ByteBuffer getRGB(ByteBuffer buffer) {
        return buffer.putFloat(red).putFloat(green).putFloat(blue);
    }

    public FloatBuffer getRGBA(FloatBuffer buffer) {
        return buffer.put(red).put(green).put(blue).put(alpha);
    }

    public ByteBuffer getRGBA(ByteBuffer buffer) {
        return buffer.putFloat(red).putFloat(green).putFloat(blue).putFloat(alpha);
    }

}