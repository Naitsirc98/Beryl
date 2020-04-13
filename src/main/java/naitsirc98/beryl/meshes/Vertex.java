package naitsirc98.beryl.meshes;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

import static naitsirc98.beryl.meshes.Mesh.*;
import static naitsirc98.beryl.meshes.StaticMesh.VERTEX_DATA_SIZE;

public final class Vertex {

    private final ByteBuffer buffer;

    public Vertex(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public Vector3f position(int index, Vector3f dest) {
        return dest.set((index * VERTEX_DATA_SIZE) + VERTEX_POSITION_OFFSET, buffer);
    }

    public Vertex position(float x, float y, float z) {
        buffer.putFloat(x).putFloat(y).putFloat(z);
        return this;
    }

    public Vertex normal(float x, float y, float z) {
        buffer.putFloat(x).putFloat(y).putFloat(z);
        return this;
    }

    public Vector3f normal(int index, Vector3f dest) {
        return dest.set((index * VERTEX_DATA_SIZE) + VERTEX_NORMAL_OFFSET, buffer);
    }

    public Vertex texCoords(float x, float y) {
        buffer.putFloat(x).putFloat(y);
        return this;
    }

    public Vector2f texCoords(int index, Vector2f dest) {
        return dest.set((index * VERTEX_DATA_SIZE) + VERTEX_TEXCOORDS_OFFSET, buffer);
    }

    public ByteBuffer buffer() {
        return buffer;
    }
}
