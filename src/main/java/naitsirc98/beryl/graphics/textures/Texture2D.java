package naitsirc98.beryl.graphics.textures;

import naitsirc98.beryl.images.Image;
import naitsirc98.beryl.images.PixelFormat;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public interface Texture2D extends Texture {

    int width();

    int height();

    void allocate(int mipLevels, int width, int height, PixelFormat internalFormat);

    void pixels(int mipLevels, Image image);

    void pixels(int mipLevels, int width, int height, PixelFormat format, ByteBuffer pixels);

    void update(int mipLevel, int xOffset, int yOffset, int width, int height, PixelFormat format, ByteBuffer pixels);

    void update(int mipLevel, int xOffset, int yOffset, int width, int height, PixelFormat format, FloatBuffer pixels);
}
