package naitsirc98.beryl.graphics;

import naitsirc98.beryl.graphics.buffers.IndexBuffer;
import naitsirc98.beryl.graphics.buffers.StorageBuffer;
import naitsirc98.beryl.graphics.buffers.UniformBuffer;
import naitsirc98.beryl.graphics.buffers.VertexBuffer;
import naitsirc98.beryl.graphics.textures.Cubemap;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.graphics.textures.Texture2DMSAA;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.resources.Resource;

public interface GraphicsFactory extends Resource {

    static GraphicsFactory get() {
        return Graphics.get().graphicsFactory();
    }

    Texture2D newTexture2D();

    Texture2D whiteTexture();

    Texture2D blackTexture2D();

    Texture2D newTexture2D(String imagePath, PixelFormat pixelFormat);

    Texture2D newTexture2DFloat(String imagePath, PixelFormat pixelFormat);

    Texture2DMSAA newTexture2DMSAA();

    Cubemap newCubemap();

    StorageBuffer newStorageBuffer();

    VertexBuffer newVertexBuffer();

    IndexBuffer newIndexBuffer();

    UniformBuffer newUniformBuffer();
}
