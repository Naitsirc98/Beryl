package naitsirc98.beryl.util.geometry;

import naitsirc98.beryl.util.types.ByteSize;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

import static naitsirc98.beryl.util.Asserts.assertTrue;
import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;

@ByteSize.Static(Bounds.SIZEOF)
public final class Bounds implements IBounds {

    public static final int SIZEOF = (3 + 3) * FLOAT32_SIZEOF;

    public final Vector3f min;
    public final Vector3f max;

    public Bounds() {
        this.min = new Vector3f(Float.POSITIVE_INFINITY);
        this.max = new Vector3f(-Float.NEGATIVE_INFINITY);
    }

    @Override
    public Vector3f min() {
        return min;
    }

    @Override
    public Vector3f max() {
        return max;
    }

    @Override
    public float centerX() {
        return (min.x + Math.max(max.x, 1.0f)) / 2.0f;
    }

    @Override
    public float centerY() {
        return (min.y + Math.max(max.y, 1.0f)) / 2.0f;
    }

    @Override
    public float centerZ() {
        return (min.z + Math.max(max.z, 1.0f)) / 2.0f;
    }

    @Override
    public ByteBuffer get(int offset, ByteBuffer buffer) {
        assertTrue(buffer.position() + offset < buffer.limit());

        min.get(offset, buffer);
        max.get(offset + 3 * FLOAT32_SIZEOF, buffer);

        return buffer;
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }
}
