package naitsirc98.beryl.graphics.opengl.vertex;

import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public final class GLVertexDataBuilder extends VertexData.Builder {

    private final GLBuffer[] vertexBuffers;
    private ByteBuffer indices;

    public GLVertexDataBuilder(VertexLayout layout) {
        super(layout);
        vertexBuffers = createVertexBuffers();
    }

    @Override
    public GLVertexDataBuilder vertices(int binding, ByteBuffer vertices) {
        vertexBuffers[binding].data(vertices, GL_DYNAMIC_DRAW);
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
            indexBuffer.data(indices, GL_STATIC_DRAW);
        }

        return new GLVertexData(layout, vertexBuffers, indexBuffer);
    }


    private GLBuffer[] createVertexBuffers() {
        GLBuffer[] vertexBuffers = new GLBuffer[layout.bindings()];
        for(int i = 0;i < vertexBuffers.length;i++) {
            vertexBuffers[i] = new GLBuffer();
        }
        return vertexBuffers;
    }
}
