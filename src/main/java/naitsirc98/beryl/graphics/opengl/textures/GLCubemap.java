package naitsirc98.beryl.graphics.opengl.textures;

import naitsirc98.beryl.graphics.opengl.GLObject;
import naitsirc98.beryl.graphics.textures.Cubemap;
import naitsirc98.beryl.images.PixelFormat;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_HEIGHT;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_INTERNAL_FORMAT;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_WIDTH;
import static org.lwjgl.opengl.GL45.glBindTexture;
import static org.lwjgl.opengl.GL45.glTexSubImage2D;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.opengl.GL45C.glGetTextureLevelParameteri;
import static org.lwjgl.opengl.GL45C.glTextureStorage2D;

public class GLCubemap extends GLTexture implements GLObject, Cubemap {

    private PixelFormat imageFormat;

    public GLCubemap() {
        super(GL_TEXTURE_CUBE_MAP);
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
    public Type type() {
        return null;
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
    public void generateMipmaps() {

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

        glBindTexture(GL_TEXTURE_CUBE_MAP, handle);

        final int lastFace = GL_TEXTURE_CUBE_MAP_POSITIVE_X + 6;

        for(int face = GL_TEXTURE_CUBE_MAP_POSITIVE_X;face < lastFace;face++) {
            glTexSubImage2D(face, mipLevel, xOffset, yOffset, width, height, mapToAPI(format), mapToAPI(format.dataType()), pixels);
        }

        this.imageFormat = format;

        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
    }

    @Override
    public void update(int mipLevel, int xOffset, int yOffset, int width, int height, PixelFormat format, FloatBuffer pixels) {

        glBindTexture(GL_TEXTURE_CUBE_MAP, handle);

        final int lastFace = GL_TEXTURE_CUBE_MAP_POSITIVE_X + 6;

        for(int face = GL_TEXTURE_CUBE_MAP_POSITIVE_X;face < lastFace;face++) {
            glTexSubImage2D(face, mipLevel, xOffset, yOffset, width, height, mapToAPI(format), mapToAPI(format.dataType()), pixels);
        }

        this.imageFormat = format;

        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
    }

    @Override
    public void update(Face face, int mipLevel, int xOffset, int yOffset, int width, int height, PixelFormat format, ByteBuffer pixels) {

        glBindTexture(GL_TEXTURE_CUBE_MAP, handle);

        glTexSubImage2D(mapToAPI(face), mipLevel, xOffset, yOffset, width, height, mapToAPI(format), mapToAPI(format.dataType()), pixels);

        this.imageFormat = format;

        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
    }

    @Override
    public void update(Face face, int mipLevel, int xOffset, int yOffset, int width, int height, PixelFormat format, FloatBuffer pixels) {

        glBindTexture(GL_TEXTURE_CUBE_MAP, handle);

        glTexSubImage2D(mapToAPI(face), mipLevel, xOffset, yOffset, width, height, mapToAPI(format), mapToAPI(format.dataType()), pixels);

        this.imageFormat = format;

        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
    }
}
