package naitsirc98.beryl.graphics.opengl.buffers;

import naitsirc98.beryl.graphics.buffers.GraphicsBuffer;
import naitsirc98.beryl.graphics.opengl.GLObject;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL45.*;

public abstract class GLBuffer implements GLObject, GraphicsBuffer {

    private int handle;

    public GLBuffer() {
        handle = glCreateBuffers();
    }

    @Override
    public int handle() {
        return handle;
    }

    @Override
    public void allocate(long size) {
        glNamedBufferStorage(handle, size, GL_DYNAMIC_STORAGE_BIT);
    }

    @Override
    public void data(ByteBuffer data) {
        glNamedBufferStorage(handle, data, GL_DYNAMIC_STORAGE_BIT);
    }

    @Override
    public void data(FloatBuffer data) {
        glNamedBufferStorage(handle, data, GL_DYNAMIC_STORAGE_BIT);
    }

    @Override
    public void data(IntBuffer data) {
        glNamedBufferStorage(handle, data, GL_DYNAMIC_STORAGE_BIT);
    }

    @Override
    public void update(long offset, ByteBuffer data) {
        glNamedBufferSubData(handle, offset, data);
    }

    @Override
    public void update(long offset, FloatBuffer data) {
        glNamedBufferSubData(handle, offset, data);
    }

    @Override
    public void update(long offset, IntBuffer data) {
        glNamedBufferSubData(handle, offset, data);
    }

    @Override
    public void free() {
        glDeleteBuffers(handle);
        handle = NULL;
    }
}
