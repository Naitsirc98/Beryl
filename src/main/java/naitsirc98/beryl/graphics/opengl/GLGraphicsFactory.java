package naitsirc98.beryl.graphics.opengl;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexDataBuilder;
import naitsirc98.beryl.graphics.textures.Texture;
import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;

public class GLGraphicsFactory implements GraphicsFactory {

    @Override
    public VertexData.Builder newVertexDataBuilder(VertexLayout layout) {
        return new GLVertexDataBuilder(layout);
    }
}
