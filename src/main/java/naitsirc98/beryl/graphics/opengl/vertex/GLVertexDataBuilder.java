package naitsirc98.beryl.graphics.opengl.vertex;

import naitsirc98.beryl.graphics.opengl.GLBuffer;
import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;

import java.nio.ByteBuffer;

public final class GLVertexDataBuilder extends VertexData.Builder {

    private ByteBuffer[] vertices;
    private ByteBuffer indices;

    public GLVertexDataBuilder(VertexLayout layout) {
        super(layout);
        vertices = new ByteBuffer[layout.bindings()];
    }

    @Override
    public GLVertexDataBuilder vertices(int binding, ByteBuffer vertices) {
        this.vertices[binding] = vertices;
        return this;
    }

    @Override
    public GLVertexDataBuilder indices(ByteBuffer indices) {
        this.indices = indices;
        return this;
    }

    @Override
    public GLVertexData build() {

        GLBuffer indexBuffer = null;

        if(indices != null && indices.hasRemaining()) {
            indexBuffer = new GLBuffer();
            indexBuffer.storage(indices);
        }

        final int indexCount = indices == null ? 0 : indices.remaining();

        return new GLVertexData(layout, firstVertex, getVertexCount(vertices), indexCount, createVertexBuffers(), indexBuffer);
    }


    private GLBuffer[] createVertexBuffers() {
        GLBuffer[] vertexBuffers = new GLBuffer[layout.bindings()];
        for(int i = 0;i < vertexBuffers.length;i++) {
            vertexBuffers[i] = new GLBuffer();
            vertexBuffers[i].storage(vertices[i]);
        }
        return vertexBuffers;
    }
}
