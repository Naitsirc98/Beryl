package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.graphics.opengl.vertex.GLVertexArray;
import naitsirc98.beryl.graphics.rendering.renderers.TerrainRenderer;
import naitsirc98.beryl.scenes.Scene;

public final class GLTerrainRenderer extends TerrainRenderer {

    private GLVertexArray vertexArray;

    GLTerrainRenderer() {

    }

    @Override
    protected void init() {

        vertexArray = new GLVertexArray();


    }

    @Override
    protected void terminate() {
        vertexArray.release();
    }

    @Override
    public void render(Scene scene) {




    }
}
