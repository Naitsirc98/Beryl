package naitsirc98.beryl.graphics.textures;

import naitsirc98.beryl.images.Image;
import naitsirc98.beryl.images.PixelFormat;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public interface Cubemap extends Texture {

    int width();
    int height();

    default void allocate(int width, int height, PixelFormat internalFormat) {
        allocate(Texture.calculateMipLevels(width, height), width, height, internalFormat);
    }

    void allocate(int mipLevels, int width, int height, PixelFormat internalFormat);

    default void pixels(Image image) {
        pixels(Texture.calculateMipLevels(image.width(), image.height()), image);
    }

    default void pixels(int mipLevels, Image image) {
        if(image.pixelFormat().dataType().decimal()) {
            pixels(mipLevels, image.width(), image.height(), image.pixelFormat(), image.pixelsf());
        } else {
            pixels(mipLevels, image.width(), image.height(), image.pixelFormat(), image.pixelsi());
        }
    }

    default void pixels(int width, int height, PixelFormat format, ByteBuffer pixels) {
        pixels(Texture.calculateMipLevels(width, height), width, height, format, pixels);
    }

    default void pixels(int width, int height, PixelFormat format, FloatBuffer pixels) {
        pixels(Texture.calculateMipLevels(width, height), width, height, format, pixels);
    }

    void pixels(int mipLevels, int width, int height, PixelFormat format, ByteBuffer pixels);

    void pixels(int mipLevels, int width, int height, PixelFormat format, FloatBuffer pixels);

    // All faces at once

    void update(int mipLevel, int xOffset, int yOffset, int width, int height, PixelFormat format, ByteBuffer pixels);

    void update(int mipLevel, int xOffset, int yOffset, int width, int height, PixelFormat format, FloatBuffer pixels);

    // Individual faces

    void update(Face face, int mipLevel, int xOffset, int yOffset, int width, int height, PixelFormat format, ByteBuffer pixels);

    void update(Face face, int mipLevel, int xOffset, int yOffset, int width, int height, PixelFormat format, FloatBuffer pixels);


    enum Face {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
        FRONT,
        BACK
    }

}
