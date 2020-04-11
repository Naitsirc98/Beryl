package naitsirc98.beryl.graphics.opengl;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.buffers.StorageBuffer;
import naitsirc98.beryl.graphics.opengl.buffers.GLStorageBuffer;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.images.Image;
import naitsirc98.beryl.images.ImageFactory;
import naitsirc98.beryl.images.PixelFormat;

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
    public StorageBuffer newStorageBuffer() {
        return new GLStorageBuffer();
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
