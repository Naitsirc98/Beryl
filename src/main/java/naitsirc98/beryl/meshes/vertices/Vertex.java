package naitsirc98.beryl.meshes.vertices;

import naitsirc98.beryl.util.types.ByteSize;

import java.nio.ByteBuffer;

public interface Vertex extends ByteSize, Cloneable {

    default Vertex set(ByteBuffer data) {
        return set(data.position(), data);
    }

    Vertex set(int offset, ByteBuffer data);

    default Vertex get(ByteBuffer data) {
        return get(data.position(), data);
    }

    Vertex get(int offset, ByteBuffer data);

    Vertex clone();
}
