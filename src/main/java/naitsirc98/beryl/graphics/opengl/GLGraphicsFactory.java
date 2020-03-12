package naitsirc98.beryl.graphics.opengl;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexDataBuilder;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.images.Image;
import naitsirc98.beryl.images.ImageFactory;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;

import static naitsirc98.beryl.graphics.textures.Sampler.*;

public class GLGraphicsFactory implements GraphicsFactory {

    private Texture2D blankTexture2D;

    @Override
    public VertexData.Builder newVertexDataBuilder(VertexLayout layout) {
        return new GLVertexDataBuilder(layout);
    }

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

    private Texture2D newBlankTexture2D() {

        Texture2D texture = newTexture2D();

        texture.sampler().wrapModeS(WrapMode.REPEAT);
        texture.sampler().wrapModeT(WrapMode.REPEAT);
        texture.sampler().wrapModeR(WrapMode.REPEAT);
        texture.sampler().magFilter(MagFilter.LINEAR);
        texture.sampler().minFilter(MinFilter.LINEAR_MIPMAP_LINEAR);

        try(Image image = ImageFactory.newBlankImage(1, 1, PixelFormat.RGBA)) {
            texture.pixels(1, image);
        }

        return texture;
    }

    @Override
    public void free() {
        if(blankTexture2D != null) {
            blankTexture2D.free();
        }
    }
}
