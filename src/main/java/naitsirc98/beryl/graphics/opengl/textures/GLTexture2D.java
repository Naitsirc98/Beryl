package naitsirc98.beryl.graphics.opengl.textures;

import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.images.PixelFormat;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL45C.glTextureStorage2D;
import static org.lwjgl.opengl.GL45C.glTextureSubImage2D;

public final class GLTexture2D extends GLTexture implements Texture2D {

    public GLTexture2D() {
        super(GL_TEXTURE_2D);
    }

    public void allocate(int mipLevels, int width, int height, int internalFormat) {
        glTextureStorage2D(handle, mipLevels, internalFormat, width, height);
    }

    @Override
    public void allocate(int mipLevels, int width, int height, PixelFormat internalFormat) {
        glTextureStorage2D(handle, mipLevels, mapper().mapToSizedInternalFormat(internalFormat), width, height);
    }

    @Override
    public void pixels(int mipLevels, int width, int height, PixelFormat format, ByteBuffer pixels) {
        allocate(mipLevels, width, height, format);
        update(0, 0, 0, width, height, format, pixels);
    }

    @Override
    public void pixels(int mipLevels, int width, int height, PixelFormat format, FloatBuffer pixels) {
        allocate(mipLevels, width, height, format);
        update(0, 0, 0, width, height, format, pixels);
    }

    @Override
    public void update(int mipLevel, int xOffset, int yOffset, int width, int height, PixelFormat format, ByteBuffer pixels) {
        glTextureSubImage2D(handle, mipLevel, xOffset, yOffset, width, height, mapper().mapToFormat(format), mapToAPI(format.dataType()), pixels);
        this.imageFormat = format;
    }

    @Override
    public void update(int mipLevel, int xOffset, int yOffset, int width, int height, PixelFormat format, FloatBuffer pixels) {
        glTextureSubImage2D(handle, mipLevel, xOffset, yOffset, width, height, mapper().mapToFormat(format), mapToAPI(format.dataType()), pixels);
        this.imageFormat = format;
    }
}
