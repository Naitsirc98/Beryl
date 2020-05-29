package naitsirc98.beryl.graphics.textures;

import naitsirc98.beryl.images.Image;
import naitsirc98.beryl.images.PixelFormat;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public interface Cubemap extends Texture {

    int faceSize();

    default void allocate(int size, PixelFormat internalFormat) {
        allocate(Texture.calculateMipLevels(size, size), size, internalFormat);
    }

    void allocate(int mipLevels, int size, PixelFormat internalFormat);

    default void pixels(Image image) {
        final int size = image.width();
        pixels(Texture.calculateMipLevels(size, size), image);
    }

    default void pixels(int mipLevels, Image image) {
        final int size = image.width();
        pixels(mipLevels, size, image.pixelFormat(), image.pixels());
    }

    default void pixels(int size, PixelFormat format, ByteBuffer pixels) {
        pixels(Texture.calculateMipLevels(size, size), size, format, pixels);
    }

    default void pixels(int size, PixelFormat format, FloatBuffer pixels) {
        pixels(Texture.calculateMipLevels(size, size), size, format, pixels);
    }

    void pixels(int mipLevels, int size, PixelFormat format, ByteBuffer pixels);

    void pixels(int mipLevels, int size, PixelFormat format, FloatBuffer pixels);

    // All faces at once

    void update(int mipLevel, int xOffset, int yOffset, int size, PixelFormat format, ByteBuffer pixels);

    void update(int mipLevel, int xOffset, int yOffset, int size, PixelFormat format, FloatBuffer pixels);

    // Individual faces

    void update(Face face, int mipLevel, int xOffset, int yOffset, int size, PixelFormat format, ByteBuffer pixels);

    void update(Face face, int mipLevel, int xOffset, int yOffset, int size, PixelFormat format, FloatBuffer pixels);


    enum Face {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
        FRONT,
        BACK
    }

}
