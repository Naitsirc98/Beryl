package naitsirc98.beryl.graphics.opengl.buffers;

import static org.lwjgl.opengl.GL44.*;

public class GLStorageBuffer extends GLBuffer {

    public void bind(int binding) {
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, binding, handle());
    }

    public void bind(int binding, long offset, long size) {
        glBindBufferRange(GL_SHADER_STORAGE_BUFFER, binding, handle(), offset, size);
    }

    public void bindIndirect() {
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, handle());
    }

    @Override
    public Type type() {
        return Type.STORAGE_BUFFER;
    }
}
