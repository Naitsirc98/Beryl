package naitsirc98.beryl.meshes.vertices;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.nio.ByteBuffer;

import static naitsirc98.beryl.util.Asserts.assertTrue;
import static naitsirc98.beryl.util.DataType.FLOAT32;

public final class Vertex3D implements Vertex {

    public static final int POSITION_OFFSET = 0;
    public static final int NORMAL_OFFSET = 3 * FLOAT32.sizeof();
    public static final int TEXCOORDS_OFFSET = (3 + 3) * FLOAT32.sizeof();

    public static final int SIZEOF = (3 + 3 + 2) * FLOAT32.sizeof();

    private final Vector3f position;
    private final Vector3f normal;
    private final Vector2f texCoords;

    public Vertex3D() {
        position = new Vector3f();
        normal = new Vector3f();
        texCoords = new Vector2f();
    }

    public Vector3fc position() {
        return position;
    }

    public Vertex3D position(float x, float y, float z) {
        position.set(x, y, z);
        return this;
    }

    public Vector3fc normal() {
        return normal;
    }

    public Vertex3D normal(float x, float y, float z) {
        normal.set(x, y, z);
        return this;
    }

    public Vector2fc texCoords() {
        return texCoords;
    }

    public Vertex3D texCoords(float u, float v) {
        texCoords.set(u, v);
        return this;
    }

    @Override
    public Vertex3D set(int offset, ByteBuffer data) {
        assertTrue(offset * SIZEOF + SIZEOF < data.limit());

        position.set((offset * SIZEOF) + POSITION_OFFSET, data);
        normal.set((offset * SIZEOF) + NORMAL_OFFSET, data);
        texCoords.set((offset * SIZEOF) + TEXCOORDS_OFFSET, data);

        return this;
    }

    @Override
    public Vertex3D get(int offset, ByteBuffer data) {
        assertTrue(offset * SIZEOF + SIZEOF < data.limit());

        position.get((offset * SIZEOF) + POSITION_OFFSET, data);
        normal.get((offset * SIZEOF) + NORMAL_OFFSET, data);
        texCoords.get((offset * SIZEOF) + TEXCOORDS_OFFSET, data);

        return this;
    }

    @Override
    public Vertex3D clone() {
        Vertex3D clone = new Vertex3D();
        clone.position.set(position);
        clone.normal.set(normal);
        clone.texCoords.set(texCoords);
        return clone;
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @Override
    public String toString() {
        return "Vertex3D{" +
                "position=" + position +
                ", normal=" + normal +
                ", texCoords=" + texCoords +
                '}';
    }
}
