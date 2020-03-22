package naitsirc98.beryl.graphics;

import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import naitsirc98.beryl.resources.Resource;

public interface GraphicsFactory extends Resource {

    static GraphicsFactory get() {
        return Graphics.get().graphicsFactory();
    }

    VertexData.Builder newVertexDataBuilder(VertexLayout layout);

    Texture2D newTexture2D();

    Texture2D blankTexture2D();
}
