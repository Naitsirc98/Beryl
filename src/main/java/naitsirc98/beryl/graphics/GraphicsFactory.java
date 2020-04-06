package naitsirc98.beryl.graphics;

import naitsirc98.beryl.graphics.rendering.PrimitiveTopology;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import naitsirc98.beryl.resources.Resource;

public interface GraphicsFactory extends Resource {

    static GraphicsFactory get() {
        return Graphics.get().graphicsFactory();
    }

    VertexData.Builder newVertexDataBuilder(VertexLayout layout, PrimitiveTopology primitiveTopology);

    Texture2D newTexture2D();

    Texture2D blankTexture2D();

    Texture2D newTexture2D(String imagePath, PixelFormat pixelFormat);
}
