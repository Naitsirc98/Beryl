package naitsirc98.beryl.graphics.buffers;

import naitsirc98.beryl.logging.Log;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;
import static naitsirc98.beryl.util.types.DataType.INT32_SIZEOF;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAddress;

public interface MappedGraphicsBuffer extends GraphicsBuffer {

    long nmappedMemoryPtr();

    default long mappedMemoryPtr() {
        final long ptr = nmappedMemoryPtr();
        if(ptr == NULL) {
            Log.fatal("Buffer " + this + " is not mapped");
        }
        return ptr;
    }

    default long mappedMemoryEndPtr() {
        return mappedMemoryPtr() + size();
    }

    default boolean mapped() {
        return nmappedMemoryPtr() != NULL;
    }

    default void mapMemory() {
        mapMemory(0, size());
    }

    default void mapMemory(long offset) {
        mapMemory(offset, size());
    }

    void mapMemory(long offset, long size);

    default void copy(long offset, ByteBuffer buffer) {
        copy(offset, memAddress(buffer), buffer.remaining());
    }

    default void copy(long offset, IntBuffer buffer) {
        copy(offset, memAddress(buffer), buffer.remaining() * INT32_SIZEOF);
    }

    default void copy(long offset, FloatBuffer buffer) {
        copy(offset, memAddress(buffer), buffer.remaining() * FLOAT32_SIZEOF);
    }

    void copy(long offset, long srcAddress, long size);

    default void set(long offset, int value) {
        set(offset, value, size());
    }

    void set(long offset, int value, long size);

    void unmapMemory();
}
