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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static java.util.Objects.requireNonNull;

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
