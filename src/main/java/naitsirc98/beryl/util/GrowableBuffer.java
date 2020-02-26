package naitsirc98.beryl.util;

import org.lwjgl.system.NativeResource;

import java.nio.ByteBuffer;

import static naitsirc98.beryl.util.Asserts.assertThat;
import static naitsirc98.beryl.util.Asserts.assertTrue;
import static naitsirc98.beryl.util.DataType.*;
import static org.lwjgl.system.MemoryUtil.*;

public final class GrowableBuffer implements NativeResource {

    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_RESIZE_FACTOR = 1.5f;

    public static GrowableBuffer makeGrowable(ByteBuffer buffer) {
        return new GrowableBuffer(buffer);
    }

    public static GrowableBuffer malloc() {
        return malloc(DEFAULT_INITIAL_CAPACITY);
    }

    public static GrowableBuffer malloc(int initialCapacity) {
        return new GrowableBuffer(memAlloc(initialCapacity));
    }

    public static GrowableBuffer calloc() {
        return calloc(DEFAULT_INITIAL_CAPACITY);
    }

    public static GrowableBuffer calloc(int initialCapacity) {
        return new GrowableBuffer(memCalloc(initialCapacity));
    }

    private ByteBuffer data;
    private float resizeFactor;

    private GrowableBuffer(ByteBuffer data) {
        this.data = data;
        this.resizeFactor = DEFAULT_RESIZE_FACTOR;
    }

    public float resizeFactor() {
        return resizeFactor;
    }

    public GrowableBuffer resizeFactor(float resizeFactor) {
        this.resizeFactor = assertThat(resizeFactor, resizeFactor > 1);
        return this;
    }

    public ByteBuffer data() {
        return data;
    }

    public int capacity() {
        return data.capacity();
    }
    
    public int limit() {
        return data.limit();
    }
    
    public int size() {
        return data.position();
    }

    public GrowableBuffer fill(int value) {
        memSet(data, value);
        return this;
    }

    public GrowableBuffer fill(int from, int to, int value) {
        final int pos = data.position();
        final int limit = data.limit();
        data.position(from).limit(to);
        memSet(data, value);
        data.position(pos).limit(limit);
        return this;
    }

    public GrowableBuffer trim() {
        if(size() < capacity()) {
            resize(size(), 0);
        }
        return this;
    }

    public GrowableBuffer reserve(int bytes) {
        if(bytes > capacity()) {
            realloc(bytes);
        }
        return this;
    }

    public GrowableBuffer resize(int newCapacity, int value) {
        assertTrue(newCapacity > 0);
        final int oldCapacity = capacity();
        realloc(newCapacity);
        if(newCapacity > oldCapacity) {
            fill(oldCapacity, newCapacity, value);
        }
        return this;
    }
    
    public byte get() {
        return data.get();
    }
    
    public GrowableBuffer put(byte b) {
        checkCapacityByte();
        data.put(b);
        return this;
    }
    
    public byte get(int index) {
        return data.get(index);
    }

    public GrowableBuffer put(int index, byte b) {
        data.put(index, b);
        return this;
    }

    public char getChar() {
        return data.getChar();
    }

    public GrowableBuffer putChar(char value) {
        checkCapacityChar();
        data.putChar(value);
        return this;
    }
    
    public char getChar(int index) {
        return data.getChar(index);
    }
    
    public GrowableBuffer putChar(int index, char value) {
        data.putChar(index, value);
        return this;
    }
    
    public short getShort() {
        return data.getShort();
    }
    
    public GrowableBuffer putShort(short value) {
        checkCapacityShort();
        data.putShort(value);
        return this;
    }

    
    public short getShort(int index) {
        return data.getShort(index);
    }
    
    public GrowableBuffer putShort(int index, short value) {
        data.putShort(index, value);
        return this;
    }

    public int getInt() {
        return data.getInt();
    }
    
    public GrowableBuffer putInt(int value) {
        checkCapacityInt();
        data.putInt(value);
        return this;
    }
    
    public int getInt(int index) {
        return data.getInt(index);
    }

    public GrowableBuffer putInt(int index, int value) {
        data.putInt(index, value);
        return this;
    }
    
    public long getLong() {
        return data.getLong();
    }
    
    public GrowableBuffer putLong(long value) {
        checkCapacityLong();
        data.putLong(value);
        return this;
    }
    
    public long getLong(int index) {
        return data.getLong(index);
    }

    
    public GrowableBuffer putLong(int index, long value) {
        data.putLong(index, value);
        return this;
    }
    
    public float getFloat() {
        return data.getFloat();
    }
    
    public GrowableBuffer putFloat(float value) {
        checkCapacityFloat();
        data.putFloat(value);
        return this;
    }

    public float getFloat(int index) {
        return data.getFloat(index);
    }
    
    public GrowableBuffer putFloat(int index, float value) {
        data.putFloat(index, value);
        return this;
    }

    public double getDouble() {
        return data.getDouble();
    }
    
    public GrowableBuffer putDouble(double value) {
        checkCapacityDouble();
        data.putDouble(value);
        return this;
    }

    public double getDouble(int index) {
        return data.getDouble(index);
    }
    
    public GrowableBuffer putDouble(int index, double value) {
        data.putDouble(index, value);
        return this;
    }

    public boolean isNull() {
        return data == null;
    }

    @Override
    public void free() {
        memFree(data);
        data = null;
    }

    private void checkCapacityByte() {
        if(size() >= capacity()) {
            reallocToFit(INT8.sizeof());
        }
    }

    private void checkCapacityChar() {
        if(size() + INT16.sizeof() >= capacity()) {
            reallocToFit(INT16.sizeof());
        }
    }

    private void checkCapacityShort() {
        if(size() + INT16.sizeof() >= capacity()) {
            reallocToFit(INT16.sizeof());
        }
    }

    private void checkCapacityInt() {
        if(size() + INT32.sizeof() >= capacity()) {
            reallocToFit(INT32.sizeof());
        }
    }

    private void checkCapacityLong() {
        if(size() + INT64.sizeof() >= capacity()) {
            reallocToFit(INT64.sizeof());
        }
    }

    private void checkCapacityFloat() {
        if(size() + FLOAT32.sizeof() >= capacity()) {
            reallocToFit(FLOAT32.sizeof());
        }
    }

    private void checkCapacityDouble() {
        if(size() + DOUBLE.sizeof() >= capacity()) {
            reallocToFit(DOUBLE.sizeof());
        }
    }

    private void reallocToFit(int elementSize) {
        int newCapacity = Math.round(capacity() * resizeFactor);
        if(size() + elementSize >= newCapacity) {
            newCapacity += elementSize;
        }
        realloc(newCapacity);
    }

    private void realloc(int newCapacity) {
        data = memRealloc(data, newCapacity);
    }
}
