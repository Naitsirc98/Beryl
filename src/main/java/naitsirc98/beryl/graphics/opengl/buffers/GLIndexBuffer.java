package naitsirc98.beryl.graphics.opengl.buffers;

import naitsirc98.beryl.graphics.buffers.IndexBuffer;

public class GLIndexBuffer extends GLBuffer implements IndexBuffer {

    @Override
    public Type type() {
        return Type.INDEX_BUFFER;
    }
}
