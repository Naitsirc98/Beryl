package naitsirc98.beryl.util;

import naitsirc98.beryl.util.types.ByteSize;

import java.nio.ByteBuffer;

@ByteSize.Static(IColor.SIZEOF)
public final class Color implements IColor {

    public static Color colorBlackTransparent() {
        return new Color(0.0f, 0.0f, 0.0f, 0.0f);
    }

    public static Color colorWhiteTransparent() {
        return new Color(1.0f, 1.0f, 1.0f, 0.0f);
    }

    public static Color colorBlack() {
        return new Color(0.0f, 0.0f, 0.0f);
    }

    public static Color colorWhite() {
        return new Color(1.0f, 1.0f, 1.0f);
    }

    public static Color colorRed() {
        return new Color(1.0f, 0.0f, 0.0f);
    }

    public static Color colorGreen() {
        return new Color(0.0f, 1.0f, 0.0f);
    }

    public static Color colorBlue() {
        return new Color(0.0f, 0.0f, 1.0f);
    }


    private float red;
    private float green;
    private float blue;
    private float alpha;

    public Color() {
        this(1, 1, 1);
    }

    public Color(IColor other) {
        this.red = other.red();
        this.green = other.green();
        this.blue = other.blue();
        this.alpha = other.alpha();
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

    public Color red(float red) {
        this.red = red;
        return this;
    }

    public float green() {
        return green;
    }

    public Color green(float green) {
        this.green = green;
        return this;
    }

    public float blue() {
        return blue;
    }

    public Color blue(float blue) {
        this.blue = blue;
        return this;
    }

    public float alpha() {
        return alpha;
    }

    public Color alpha(float alpha) {
        this.alpha = alpha;
        return this;
    }

    public Color set(IColor color) {
        return set(color.red(), color.green(), color.blue(), color.alpha());
    }

    public Color set(float rgba) {
        return set(rgba, rgba, rgba, rgba);
    }

    public Color set(float rgb, float alpha) {
        return set(rgb, rgb, rgb, alpha);
    }

    public Color set(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        return this;
    }

    public Color intensify(float factor) {
        red *= factor;
        green *= factor;
        blue *= factor;
        return this;
    }

    @Override
    public ByteBuffer getRGB(ByteBuffer buffer) {
        return buffer.putFloat(red).putFloat(green).putFloat(blue);
    }

    @Override
    public ByteBuffer getRGB(int pos, ByteBuffer buffer) {
        return buffer.putFloat(pos, red).putFloat(pos + 4, green).putFloat(pos + 8, blue);
    }

    @Override
    public ByteBuffer getRGBA(ByteBuffer buffer) {
        return buffer.putFloat(red).putFloat(green).putFloat(blue).putFloat(alpha);
    }

    @Override
    public ByteBuffer getRGBA(int pos, ByteBuffer buffer) {
        return buffer.putFloat(pos, red).putFloat(pos + 4, green).putFloat(pos + 8, blue).putFloat(pos + 12, alpha);
    }

    @Override
    public String toString() {
        return "Color{" +
                "red=" + red +
                ", green=" + green +
                ", blue=" + blue +
                ", alpha=" + alpha +
                '}';
    }
}
