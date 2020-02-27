package naitsirc98.beryl.graphics.opengl.vertex;

import naitsirc98.beryl.graphics.opengl.GLObject;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL45.glCreateBuffers;
import static org.lwjgl.opengl.GL45.glNamedBufferData;

public abstract class GLBuffer implements GLObject {

    private final int handle;

    protected GLBuffer() {
        handle = glCreateBuffers();
    }

    @Override
    public int handle() {
        return handle;
    }

    public void data(ByteBuffer data, int usage) {
        glNamedBufferData(handle, data, usage);
    }

    @Override
    public void free() {
        glDeleteBuffers(handle);
    }
}
