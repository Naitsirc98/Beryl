package naitsirc98.beryl.graphics.opengl.buffers;

import naitsirc98.beryl.graphics.buffers.GraphicsBuffer;
import naitsirc98.beryl.graphics.buffers.GraphicsMappableBuffer;
import naitsirc98.beryl.graphics.opengl.GLObject;
import org.lwjgl.PointerBuffer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL45.*;

public abstract class GLBuffer implements GLObject, GraphicsBuffer, GraphicsMappableBuffer {

    private int handle;
    private boolean allocated;

    public GLBuffer() {
        handle = glCreateBuffers();
        allocated = false;
    }

    @Override
    public int handle() {
        return handle;
    }

    public long size() {
        return glGetNamedBufferParameteri64(handle(), GL_BUFFER_SIZE);
    }

    @Override
    public void allocate(long size) {
        if(allocated) {
            recreate();
            allocated = false;
        }
        glNamedBufferStorage(handle, size, storageFlags());
    }

    @Override
    public void data(ByteBuffer data) {
        if(allocated) {
            recreate();
            allocated = false;
        }
        glNamedBufferStorage(handle, data, storageFlags());
    }

    @Override
    public void data(FloatBuffer data) {
        if(allocated) {
            recreate();
            allocated = false;
        }
        glNamedBufferStorage(handle, data, storageFlags());
    }

    @Override
    public void data(IntBuffer data) {
        if(allocated) {
            recreate();
            allocated = false;
        }
        glNamedBufferStorage(handle, data, storageFlags());
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
    public PointerBuffer mapMemory(long offset) {
        return PointerBuffer.allocateDirect(1)
                .put(nglMapNamedBufferRange(handle(), offset, size() - offset, mapFlags()));
    }

    @Override
    public void unmapMemory() {
        glUnmapNamedBuffer(handle());
    }

    protected int mapFlags() {
        return GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT;
    }

    @Override
    public void release() {
        glDeleteBuffers(handle);
        handle = NULL;
    }

    protected void recreate() {
        release();
        handle = glCreateBuffers();
    }

    protected abstract int storageFlags();
}
