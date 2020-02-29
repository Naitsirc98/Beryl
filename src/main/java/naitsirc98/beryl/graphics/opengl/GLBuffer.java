package naitsirc98.beryl.graphics.opengl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL45.*;

public class GLBuffer implements GLObject {

    private final int handle;

    public GLBuffer() {
        handle = glCreateBuffers();
    }

    @Override
    public int handle() {
        return handle;
    }

    public void storage(long size) {
        glNamedBufferStorage(handle, size, GL_DYNAMIC_STORAGE_BIT);
    }

    public void data(ByteBuffer data, int usage) {
        storage(data.remaining());
        update(data);
    }

    public void data(FloatBuffer data, int usage) {
        storage(data.remaining());
        update(data);
    }

    public void data(IntBuffer data, int usage) {
        storage(data.remaining());
        update(data);
    }

    public void update(ByteBuffer data) {
        glNamedBufferSubData(handle, 0, data);
    }

    public void update(FloatBuffer data) {
        glNamedBufferSubData(handle, 0, data);
    }

    public void update(IntBuffer data) {
        glNamedBufferSubData(handle, 0, data);
    }

    public void update(long offset, ByteBuffer data) {
        glNamedBufferSubData(handle, offset, data);
    }

    public void update(long offset, FloatBuffer data) {
        glNamedBufferSubData(handle, offset, data);
    }

    public void update(long offset, IntBuffer data) {
        glNamedBufferSubData(handle, offset, data);
    }

    @Override
    public void free() {
        glDeleteBuffers(handle);
    }
}
