package naitsirc98.beryl.graphics.opengl;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.buffers.IndexBuffer;
import naitsirc98.beryl.graphics.buffers.StorageBuffer;
import naitsirc98.beryl.graphics.buffers.UniformBuffer;
import naitsirc98.beryl.graphics.buffers.VertexBuffer;
import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.textures.GLCubemap;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2DMSAA;
import naitsirc98.beryl.graphics.textures.Cubemap;
import naitsirc98.beryl.graphics.textures.Cubemap.Face;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.graphics.textures.Texture2DMSAA;
import naitsirc98.beryl.images.Image;
import naitsirc98.beryl.images.ImageFactory;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.logging.Log;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.graphics.Graphics.opengl;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.opengl.GL30.GL_RGB16F;
import static org.lwjgl.opengl.GL42C.glTexStorage2D;
import static org.lwjgl.opengl.GL45C.glTextureStorage2D;
import static org.lwjgl.opengl.GL45C.glTextureSubImage2D;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memByteBuffer;

public class GLGraphicsFactory implements GraphicsFactory {

    private Texture2D blankTexture2D;

    @Override
    public Texture2D newTexture2D() {
        return new GLTexture2D();
    }

    @Override
    public Texture2D blankTexture2D() {
        if(blankTexture2D == null) {
            blankTexture2D = newBlankTexture2D();
        }
        return blankTexture2D;
    }

    @Override
    public Texture2D newTexture2D(String imagePath, PixelFormat pixelFormat) {

        Texture2D texture = newTexture2D();

        try(Image image = ImageFactory.newImage(imagePath, pixelFormat)) {
            texture.pixels(requireNonNull(image));
        }

        return texture;
    }

    @Override
    public Texture2D newTexture2DFloat(String imagePath, PixelFormat pixelFormat) {

        GLTexture2D texture = new GLTexture2D();

        try(MemoryStack stack = stackPush()) {

            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);
            int desiredChannels = pixelFormat == null ? STBI_default : pixelFormat.channels();

            stbi_set_flip_vertically_on_load(true);

            FloatBuffer pixelsf = stbi_loadf(imagePath, width, height, channels, desiredChannels);

            if(pixelsf == null) {
                Log.error("Failed to load image " + imagePath + ": " + stbi_failure_reason());
                return null;
            }

            glBindTexture(GL_TEXTURE_2D, texture.handle());

            glTextureStorage2D(texture.handle(), 1, GL_RGB16F, width.get(0), height.get(0));
            glTextureSubImage2D(texture.handle(), 0, 0, 0, width.get(0), height.get(0), GL_RGB, GL_FLOAT, pixelsf);

            // glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, width.get(0), height.get(0), 0, GL_RGB, GL_FLOAT, pixelsf);

            glBindTexture(GL_TEXTURE_2D, 0);
            // texture.pixels(1, width.get(0), height.get(0), pixelFormat, pixelsf);

        } catch(Throwable e) {
            Log.error("Failed to load image " + imagePath + ": " + stbi_failure_reason(), e);
        }

        return texture;
    }

    @Override
    public Texture2DMSAA newTexture2DMSAA() {
        return new GLTexture2DMSAA();
    }

    @Override
    public Cubemap newCubemap() {
        return new GLCubemap();
    }

    @Override
    public StorageBuffer newStorageBuffer() {
        return new GLBuffer();
    }

    @Override
    public VertexBuffer newVertexBuffer() {
        return new GLBuffer();
    }

    @Override
    public IndexBuffer newIndexBuffer() {
        return new GLBuffer();
    }

    @Override
    public UniformBuffer newUniformBuffer() {
        return new GLBuffer();
    }

    private Texture2D newBlankTexture2D() {

        Texture2D texture = newTexture2D();

        try(Image image = ImageFactory.newBlankImage(PixelFormat.RGBA)) {
            texture.pixels(image);
        }

        texture.makeResident();

        return texture;
    }

    @Override
    public void release() {
        if(blankTexture2D != null) {
            blankTexture2D.release();
        }
    }
}
