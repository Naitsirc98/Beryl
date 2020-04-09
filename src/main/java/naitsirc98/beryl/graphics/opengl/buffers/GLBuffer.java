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
import static org.lwjgl.system.MemoryUtil.memCallocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;

public abstract class GLBuffer implements GLObject, GraphicsBuffer, GraphicsMappableBuffer {

    private int handle;
    private boolean allocated;
    private final PointerBuffer mappedPtr;

    public GLBuffer() {
        handle = glCreateBuffers();
        allocated = false;
        mappedPtr = memCallocPointer(1);
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
        if(!allocated) {
            Log.warning("Trying to map buffer " + toString() + ", but it has not been allocated yet");
        } else if(mappedPtr.get(0) == NULL) {
            mappedPtr.put(0, nglMapNamedBufferRange(handle(), offset, size() - offset, mapFlags()));
        }
        return mappedPtr;
    }

    @Override
    public void flush() {
        glFlushMappedNamedBufferRange(handle, 0, size());
    }

    @Override
    public void unmapMemory() {
        if(mappedPtr.get(0) != NULL) {
            glUnmapNamedBuffer(handle());
            mappedPtr.put(0, NULL);
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
        memFree(mappedPtr);
        handle = NULL;
    }

    protected void recreate() {
        release();
        handle = glCreateBuffers();
    }
}
