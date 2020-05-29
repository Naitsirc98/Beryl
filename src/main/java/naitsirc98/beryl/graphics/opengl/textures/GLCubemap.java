package naitsirc98.beryl.graphics.opengl.textures;

import naitsirc98.beryl.graphics.opengl.GLContext;
import naitsirc98.beryl.graphics.textures.Cubemap;
import naitsirc98.beryl.images.PixelFormat;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static naitsirc98.beryl.core.BerylConfigConstants.DEBUG;
import static naitsirc98.beryl.util.Asserts.assertTrue;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_INTERNAL_FORMAT;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.opengl.GL45C.glGetTextureLevelParameteri;

public class GLCubemap extends GLTexture implements Cubemap {

    private PixelFormat imageFormat;

    public GLCubemap(GLContext context) {
        super(context, GL_TEXTURE_CUBE_MAP);
    }

    @Override
    public int faceSize() {
        return width();
    }

    @Override
    public PixelFormat internalFormat() {
        return mapFromAPI(PixelFormat.class, glGetTextureLevelParameteri(handle(), 0, GL_TEXTURE_INTERNAL_FORMAT));
    }

    @Override
    public PixelFormat format() {
        return imageFormat;
    }

    @Override
    public void allocate(int mipLevels, int size, PixelFormat internalFormat) {
        glTextureStorage2D(handle(), mipLevels, context().mapper().mapToSizedInternalFormat(internalFormat), size, size);
    }

    @Override
    public void pixels(int mipLevels, int size, PixelFormat format, ByteBuffer pixels) {
        allocate(mipLevels, size, format);
        update(0, 0, 0, size, format, pixels);
    }

    @Override
    public void pixels(int mipLevels, int size, PixelFormat format, FloatBuffer pixels) {
        allocate(mipLevels, size, format);
        update(0, 0, 0, size, format, pixels);
    }

    @Override
    public void update(int mipLevel, int xOffset, int yOffset, int size, PixelFormat format, ByteBuffer pixels) {

        bind();

        final int lastFace = GL_TEXTURE_CUBE_MAP_POSITIVE_X + 6;

        for(int face = GL_TEXTURE_CUBE_MAP_POSITIVE_X;face < lastFace;face++) {
            glTexSubImage2D(face, mipLevel, xOffset, yOffset, size, size, mapToAPI(format), mapToAPI(format.dataType()), pixels);
        }

        this.imageFormat = format;

        unbind();
    }

    @Override
    public void update(int mipLevel, int xOffset, int yOffset, int size, PixelFormat format, FloatBuffer pixels) {

        bind();

        final int lastFace = GL_TEXTURE_CUBE_MAP_POSITIVE_X + 6;

        for(int face = GL_TEXTURE_CUBE_MAP_POSITIVE_X;face < lastFace;face++) {
            glTexSubImage2D(face, mipLevel, xOffset, yOffset, size, size, mapToAPI(format), mapToAPI(format.dataType()), pixels);
        }

        this.imageFormat = format;

        unbind();
    }

    @Override
    public void update(Face face, int mipLevel, int xOffset, int yOffset, int size, PixelFormat format, ByteBuffer pixels) {

        bind();

        glTexSubImage2D(mapToAPI(face), mipLevel, xOffset, yOffset, size, size, mapToAPI(format), mapToAPI(format.dataType()), pixels);

        this.imageFormat = format;

        unbind();
    }

    @Override
    public void update(Face face, int mipLevel, int xOffset, int yOffset, int size, PixelFormat format, FloatBuffer pixels) {

        bind();

        glTexSubImage2D(mapToAPI(face), mipLevel, xOffset, yOffset, size, size, mapToAPI(format), mapToAPI(format.dataType()), pixels);

        this.imageFormat = format;

        unbind();
    }

    @Override
    public void bind(int unit) {
        glActiveTexture(GL_TEXTURE0 + unit);
        bind();
    }

    @Override
    public void unbind(int unit) {
        glActiveTexture(GL_TEXTURE0 + unit);
        unbind();
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_CUBE_MAP, handle());
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
    }

    private int getCubemapFace(int index) {
        if(DEBUG) {
            assertTrue(index >= 0 && index < 6);
        }
        return GL_TEXTURE_CUBE_MAP_POSITIVE_X + index;
    }
}
