package naitsirc98.beryl.graphics.opengl;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.buffers.IndexBuffer;
import naitsirc98.beryl.graphics.buffers.StorageBuffer;
import naitsirc98.beryl.graphics.buffers.UniformBuffer;
import naitsirc98.beryl.graphics.buffers.VertexBuffer;
import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.textures.GLCubemap;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;
import naitsirc98.beryl.graphics.textures.Cubemap;
import naitsirc98.beryl.graphics.textures.Cubemap.Face;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.images.Image;
import naitsirc98.beryl.images.ImageFactory;
import naitsirc98.beryl.images.PixelFormat;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.Asserts.assertEquals;
import static naitsirc98.beryl.util.Asserts.assertTrue;

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
    public Cubemap newCubemap() {
        return new GLCubemap();
    }

    @Override
    public Cubemap newCubemap(String skyboxFolder, PixelFormat pixelFormat) {

        Cubemap cubemap = newCubemap();

        Image[] images = new Image[6];

        Face[] faces = Face.values();

        String[] faceNames = {"left.png", "right.png", "top.png", "bottom.png", "front.png", "back.png"};

        Path folder = Paths.get(skyboxFolder);

        for(int i = 0;i < 6;i++) {

            Image image = images[i] = ImageFactory.newImage(folder.resolve(faceNames[i]).toString(), pixelFormat);

            if(i == 0) {
                cubemap.allocate(image.width(), image.height(), pixelFormat);
            }

            cubemap.update(faces[i], 0, 0, 0, image.width(), image.height(), pixelFormat, image.pixelsi());
        }

        Arrays.stream(images).forEach(Image::release);

        return cubemap;
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
