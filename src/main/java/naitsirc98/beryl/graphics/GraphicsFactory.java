package naitsirc98.beryl.graphics;

import naitsirc98.beryl.graphics.textures.Texture;
import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;

public interface GraphicsFactory {

    static GraphicsFactory get() {
        return Graphics.graphicsContext().graphicsFactory();
    }

    VertexData.Builder newVertexDataBuilder(VertexLayout layout);

}
