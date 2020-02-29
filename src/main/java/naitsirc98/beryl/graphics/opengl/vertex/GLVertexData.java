package naitsirc98.beryl.graphics.opengl.vertex;

import naitsirc98.beryl.graphics.opengl.GLBuffer;
import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;

public final class GLVertexData extends VertexData {

    private GLVertexArray vertexArray;
    private GLBuffer[] vertexBuffers;
    private GLBuffer indexBuffer;

    protected GLVertexData(VertexLayout layout, int firstVertex, int vertexCount, int indexCount,
                           GLBuffer[] vertexBuffers, GLBuffer indexBuffer) {

        super(layout, firstVertex, vertexCount, indexCount);
        this.vertexBuffers = vertexBuffers;
        this.indexBuffer = indexBuffer;
        vertexArray = new GLVertexArray();

        for(int i = 0;i < layout.bindings();i++) {
            vertexArray.addVertexBuffer(i, layout.attributes(i), vertexBuffers[i]);
        }

        if(indexBuffer != null) {
            vertexArray.setIndexBuffer(indexBuffer);
        }
    }

    public void bind() {
        vertexArray.bind();
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

}
