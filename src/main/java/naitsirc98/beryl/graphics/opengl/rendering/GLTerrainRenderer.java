package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.graphics.opengl.vertex.GLVertexArray;
import naitsirc98.beryl.graphics.rendering.renderers.TerrainRenderer;
import naitsirc98.beryl.scenes.Scene;

import static naitsirc98.beryl.meshes.vertices.VertexLayout.VERTEX_LAYOUT_3D;

public final class GLTerrainRenderer extends TerrainRenderer {

    private GLVertexArray vertexArray;

    GLTerrainRenderer() {

    }

    @Override
    protected void init() {

        vertexArray = new GLVertexArray();

        vertexArray.setVertexAttributes(0, VERTEX_LAYOUT_3D.attributeList(0));
    }

    @Override
    protected void terminate() {
        vertexArray.release();
    }

    @Override
    public void prepare(Scene scene) {




    }

    @Override
    public void render(Scene scene) {




    }
}
