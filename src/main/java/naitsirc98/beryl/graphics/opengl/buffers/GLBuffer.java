package naitsirc98.beryl.graphics.opengl.buffers;

import naitsirc98.beryl.graphics.buffers.*;
import naitsirc98.beryl.graphics.opengl.GLObject;
import naitsirc98.beryl.logging.Log;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;
import static naitsirc98.beryl.util.types.DataType.INT32_SIZEOF;
import static naitsirc98.beryl.util.types.TypeUtils.getOrElse;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.libc.LibCString.nmemcpy;
import static org.lwjgl.system.libc.LibCString.nmemset;

public class GLBuffer implements GLObject, MappedGraphicsBuffer, VertexBuffer, IndexBuffer, StorageBuffer, UniformBuffer {

    private final String name;
    private int handle;
    private long size;
    private long memoryPtr;

    public GLBuffer(String name) {
        this.name = getOrElse(name, "UNNAMED_GL_BUFFER");
        handle = glCreateBuffers();
    }

    public GLBuffer() {
        this(null);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int handle() {
        return handle;
    }

    @Override
    public long size() {
        return size;
    }

    public long nsize() {
        return glGetNamedBufferParameteri64(handle(), GL_BUFFER_SIZE);
    }

    public void bind(int target) {
        glBindBuffer(target, handle);
    }

    public void bind(int target, int binding) {
        glBindBufferBase(target, binding, handle);
    }

    public void bind(int target, int binding, long offset, long size) {
        glBindBufferRange(target, binding, handle, offset, size);
    }

    public void unbind(int target, int index) {
        glBindBufferBase(target, index, 0);
    }

    public void unbind(int target) {
        glBindBuffer(target, 0);
    }

    @Override
    public void allocate(long size) {
        if(allocated()) {
            Log.fatal("This buffer is already allocated. Use reallocate instead");
            return;
        }
        glNamedBufferStorage(handle, size, storageFlags());
        this.size = size;
    }

    @Override
    public void reallocate(long newSize) {
        if(size != newSize) {
            recreate();
            allocate(newSize);
        }
    }

    @Override
    public void data(ByteBuffer data) {
        glNamedBufferStorage(handle, data, storageFlags());
        this.size = data.remaining();
    }

    @Override
    public void data(FloatBuffer data) {
        glNamedBufferStorage(handle, data, storageFlags());
        this.size = data.remaining() * FLOAT32_SIZEOF;
    }

    @Override
    public void data(IntBuffer data) {
        glNamedBufferStorage(handle, data, storageFlags());
        this.size = data.remaining() * INT32_SIZEOF;
    }

    @Override
    public void clear(int value) {
        if(mapped()) {
            set(0, 0);
        } else {
            mapMemory();
            set(0, 0);
            unmapMemory();
        }
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
    public ByteBuffer get(long offset, ByteBuffer buffer) {
        glGetNamedBufferSubData(handle, offset, buffer);
        return buffer;
    }

    @Override
    public void update(long offset, IntBuffer data) {
        glNamedBufferSubData(handle, offset, data);
    }

    @Override
    public long nmappedMemoryPtr() {
        return memoryPtr;
    }

    @Override
    public void mapMemory(long offset, long size) {
        if(!allocated()) {
            Log.fatal("Buffer " + this + " has not been allocated");
            return;
        }
        if(mapped()) {
            Log.fatal("Buffer " + this + " is already mapped");
            return;
        }

        memoryPtr = nglMapNamedBufferRange(handle, offset, size, mapFlags());
    }

    @Override
    public void copy(long offset, long srcAddress, long size) {

        if(invalidMemoryRange(offset, size)) {
            return;
        }

        nmemcpy(memoryPtr + offset, srcAddress, size);
    }

    @Override
    public void set(long offset, int value, long size) {

        if(invalidMemoryRange(offset, size)) {
            return;
        }

        nmemset(memoryPtr + offset, value, size);
    }

    @Override
    public void unmapMemory() {
        if(mapped()) {
           glUnmapNamedBuffer(handle);
        }
    }

    @Override
    public void release() {
        unmapMemory();
        glDeleteBuffers(handle);
        handle = NULL;
        size = 0;
    }

    protected int mapFlags() {
        return GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT;
    }

    protected int storageFlags() {
        return GL_DYNAMIC_STORAGE_BIT | mapFlags();
    }

    protected void recreate() {
        release();
        handle = glCreateBuffers();
    }

    private boolean invalidMemoryRange(long offset, long size) {

        if(!allocated()) {
            Log.fatal("Buffer " + this + " is not allocated");
            return true;
        }

        if(!mapped()) {
            Log.fatal("Buffer " + this + " is not mapped");
            return true;
        }

        final long dstAddress = memoryPtr + offset;

        if(dstAddress + size > mappedMemoryEndPtr()) {
            Log.fatal("Memory region is out of range: " + dstAddress + size + " > " + size());
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return "GLBuffer{" +
                "name='" + name + '\'' +
                ", handle=" + handle +
                ", size=" + size +
                ", memoryPtr=" + (memoryPtr == 0 ? "NULL" : memoryPtr) +
                '}';
    }
}
