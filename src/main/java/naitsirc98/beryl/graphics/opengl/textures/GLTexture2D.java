package naitsirc98.beryl.graphics.opengl.textures;

import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.images.Image;
import naitsirc98.beryl.images.PixelFormat;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL45C.*;

public final class GLTexture2D extends GLTexture implements Texture2D {

    private PixelFormat imageFormat;

    public GLTexture2D() {
        super(GL_TEXTURE_2D);
    }

    @Override
    public Type type() {
        return Type.TEXTURE_2D;
    }

    @Override
    public void generateMipmaps() {
        glGenerateTextureMipmap(handle());
    }

    @Override
    public int width() {
        return glGetTextureLevelParameteri(handle, 0, GL_TEXTURE_WIDTH);
    }

    @Override
    public int height() {
        return glGetTextureLevelParameteri(handle, 0, GL_TEXTURE_HEIGHT);
    }

    @Override
    public PixelFormat internalFormat() {
        return mapFromAPI(PixelFormat.class, glGetTextureLevelParameteri(handle, 0, GL_TEXTURE_INTERNAL_FORMAT));
    }

    @Override
    public PixelFormat format() {
        return imageFormat;
    }

    @Override
    public void allocate(int mipLevels, int width, int height, PixelFormat internalFormat) {
        glTextureStorage2D(handle, mipLevels, /*mapToAPI(internalFormat)*/GL_RGBA8, width, height);
    }

    @Override
    public void pixels(int mipLevels, Image image) {
        pixels(mipLevels, image.width(), image.height(), image.pixelFormat(), image.pixelsi());
    }

    @Override
    public void pixels(int mipLevels, int width, int height, PixelFormat format, ByteBuffer pixels) {
        allocate(mipLevels, width, height, format);
        update(0, 0, 0, width, height, format, pixels);
    }

    @Override
    public void update(int mipLevel, int xOffset, int yOffset, int width, int height, PixelFormat format, ByteBuffer pixels) {
        glTextureSubImage2D(handle, mipLevel, xOffset, yOffset, width, height, mapToAPI(format), mapToAPI(format.dataType()), pixels);
        this.imageFormat = format;
    }

    @Override
    public void update(int mipLevel, int xOffset, int yOffset, int width, int height, PixelFormat format, FloatBuffer pixels) {
        glTextureSubImage2D(handle, mipLevel, xOffset, yOffset, width, height, mapToAPI(format), mapToAPI(format.dataType()), pixels);
        this.imageFormat = format;
    }
}
