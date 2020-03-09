package naitsirc98.beryl.graphics.buffers;

import org.lwjgl.system.NativeResource;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public interface GraphicsBuffer extends NativeResource {

    Type type();

    void allocate(long bytes);

    void data(ByteBuffer data);
    void data(IntBuffer data);
    void data(FloatBuffer data);

    void update(long offset, ByteBuffer data);
    void update(long offset, IntBuffer data);
    void update(long offset, FloatBuffer data);

    enum Type {
        VERTEX_BUFFER,
        INDEX_BUFFER,
        UNIFORM_BUFFER
    }

}
