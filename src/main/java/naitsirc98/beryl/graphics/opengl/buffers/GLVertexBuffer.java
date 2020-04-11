package naitsirc98.beryl.graphics.opengl.buffers;

import naitsirc98.beryl.graphics.buffers.VertexBuffer;

public class GLVertexBuffer extends GLBuffer implements VertexBuffer {

    @Override
    public Type type() {
        return Type.VERTEX_BUFFER;
    }
}
