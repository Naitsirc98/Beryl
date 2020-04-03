package naitsirc98.beryl.graphics.opengl.vertex;

import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.buffers.GLVertexBuffer;
import naitsirc98.beryl.graphics.rendering.PrimitiveTopology;
import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;

public final class GLVertexData extends VertexData {

    private GLVertexArray vertexArray;
    private GLVertexBuffer[] vertexBuffers;
    private GLBuffer indexBuffer;

    protected GLVertexData(VertexLayout layout, PrimitiveTopology primitiveTopology,
                           int firstVertex, int vertexCount, int indexCount,
                           GLVertexBuffer[] vertexBuffers, GLBuffer indexBuffer) {

        super(layout, primitiveTopology, firstVertex, vertexCount, indexCount);
        this.vertexBuffers = vertexBuffers;
        this.indexBuffer = indexBuffer;
        vertexArray = new GLVertexArray();

        for(int i = 0;i < layout.bindings();i++) {
            vertexArray.addVertexBuffer(i, layout.attributeList(i), vertexBuffers[i]);
        }

        if(indexBuffer != null) {
            vertexArray.setIndexBuffer(indexBuffer);
        }
    }

    public void bind() {
        vertexArray.bind();
    }

    @Override
    protected void free() {

        vertexArray.release();

        for(int i = 0;i < vertexBuffers.length;i++) {
            vertexBuffers[i].release();
            vertexBuffers[i] = null;
        }

        if(indexBuffer != null) {
            indexBuffer.release();
        }

        vertexArray = null;
        vertexBuffers = null;
        indexBuffer = null;
    }

}
