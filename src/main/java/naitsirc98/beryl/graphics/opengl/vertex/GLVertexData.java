package naitsirc98.beryl.graphics.opengl.vertex;

import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;

public final class GLVertexData extends VertexData {

    private GLVertexArray vertexArray;
    private GLBuffer[] vertexBuffers;
    private GLBuffer indexBuffer;

    protected GLVertexData(VertexLayout layout, GLBuffer[] vertexBuffers, GLBuffer indices) {
        super(layout);
    }

    @Override
    public void free() {

        vertexArray.free();

        for(int i = 0;i < vertexBuffers.length;i++) {
            vertexBuffers[i].free();
            vertexBuffers[i] = null;
        }

        if(indexBuffer != null) {
            indexBuffer.free();
        }

        vertexArray = null;
        vertexBuffers = null;
        indexBuffer = null;
    }

    @Override
    public void bind() {
        vertexArray.bind();
    }
}
