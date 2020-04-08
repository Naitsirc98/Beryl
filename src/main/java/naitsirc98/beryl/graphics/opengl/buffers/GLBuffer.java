package naitsirc98.beryl.graphics.opengl.buffers;

import naitsirc98.beryl.graphics.buffers.GraphicsBuffer;
import naitsirc98.beryl.graphics.buffers.GraphicsMappableBuffer;
import naitsirc98.beryl.graphics.opengl.GLObject;
import naitsirc98.beryl.logging.Log;
import org.lwjgl.PointerBuffer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL45.*;

public abstract class GLBuffer implements GLObject, GraphicsBuffer, GraphicsMappableBuffer {

    private int handle;
    private boolean allocated;
    private long mappedPtr = NULL;

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
        allocated = true;
    }

    @Override
    public void data(ByteBuffer data) {
        if(allocated) {
            recreate();
            allocated = false;
        }
        glNamedBufferStorage(handle, data, storageFlags());
        allocated = true;
    }

    @Override
    public void data(FloatBuffer data) {
        if(allocated) {
            recreate();
            allocated = false;
        }
        glNamedBufferStorage(handle, data, storageFlags());
        allocated = true;
    }

    @Override
    public void data(IntBuffer data) {
        if(allocated) {
            recreate();
            allocated = false;
        }
        glNamedBufferStorage(handle, data, storageFlags());
        allocated = true;
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

        if(mappedPtr != NULL) {
            Log.fatal("Buffer " + toString() + " has been already mapped!");
        }

        mappedPtr = nglMapNamedBufferRange(handle(), offset, size() - offset, mapFlags());

        return PointerBuffer.allocateDirect(1).put(mappedPtr);
    }

    @Override
    public void unmapMemory() {
        if(mappedPtr != NULL) {
            glUnmapNamedBuffer(handle());
            mappedPtr = NULL;
        } else {
            Log.warning("Trying to unmap buffer " + toString() + ", but it has not been mapped yet");
        }
    }

    protected int mapFlags() {
        return GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT;
    }

    protected int storageFlags() {
        return GL_DYNAMIC_STORAGE_BIT | mapFlags();
    }

    @Override
    public void release() {
        unmapMemory();
        glDeleteBuffers(handle);
        handle = NULL;
    }

    protected void recreate() {
        release();
        handle = glCreateBuffers();
    }
}
