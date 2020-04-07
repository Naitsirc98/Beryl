package naitsirc98.beryl.graphics.textures;

import naitsirc98.beryl.images.Image;
import naitsirc98.beryl.images.PixelFormat;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static java.lang.Math.max;
import static naitsirc98.beryl.util.Maths.log2;

public interface Texture2D extends Texture {

    static int calculateMipLevels(int width, int height) {
        return log2(max(width, height) + 1);
    }

    int width();

    int height();

    default void allocate(int width, int height, PixelFormat internalFormat) {
        allocate(calculateMipLevels(width, height), width, height, internalFormat);
    }

    void allocate(int mipLevels, int width, int height, PixelFormat internalFormat);

    default void pixels(Image image) {
        pixels(calculateMipLevels(image.width(), image.height()), image);
    }

    default void pixels(int mipLevels, Image image) {
        if(image.pixelFormat().dataType().decimal()) {
            pixels(mipLevels, image.width(), image.height(), image.pixelFormat(), image.pixelsf());
        } else {
            pixels(mipLevels, image.width(), image.height(), image.pixelFormat(), image.pixelsi());
        }
    }

    default void pixels(int width, int height, PixelFormat format, ByteBuffer pixels) {
        pixels(calculateMipLevels(width, height), width, height, format, pixels);
    }

    default void pixels(int width, int height, PixelFormat format, FloatBuffer pixels) {
        pixels(calculateMipLevels(width, height), width, height, format, pixels);
    }

    void pixels(int mipLevels, int width, int height, PixelFormat format, ByteBuffer pixels);

    void pixels(int mipLevels, int width, int height, PixelFormat format, FloatBuffer pixels);

    void update(int mipLevel, int xOffset, int yOffset, int width, int height, PixelFormat format, ByteBuffer pixels);

    void update(int mipLevel, int xOffset, int yOffset, int width, int height, PixelFormat format, FloatBuffer pixels);
}
