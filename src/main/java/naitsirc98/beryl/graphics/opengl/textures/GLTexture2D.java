package naitsirc98.beryl.graphics.opengl.textures;

import naitsirc98.beryl.images.Image;

import java.nio.ByteBuffer;

import static naitsirc98.beryl.graphics.opengl.GLUtils.toGL;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL45C.*;

public final class GLTexture2D extends GLTexture {

    public GLTexture2D() {
        super(GL_TEXTURE_2D);
    }

    public void image(int internalFormat, Image image) {
        glBindTexture(GL_TEXTURE_2D, handle());
        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, image.width(), image.height(), 0,
                toGL(image.pixelFormat()), toGL(image.pixelFormat().dataType()), image.pixelsi());
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void storage(int levels, int internalFormat, int width, int height) {
        glTextureStorage2D(handle(), levels, internalFormat, width, height);
    }

    public void update(int level, int width, int height, int format, int type, ByteBuffer pixels) {
        update(level, 0, 0, width, height, format, type, pixels);
    }

    public void update(int level, int xoffset, int yoffset, int width, int height, int format, int type, ByteBuffer pixels) {
        glTextureSubImage2D(handle(), level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public void generateMipmaps() {
        glGenerateTextureMipmap(handle());
    }

    public void bind(int unit) {
        glBindTextureUnit(unit, handle());
    }

}
