package naitsirc98.beryl.util.geometry;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.nio.ByteBuffer;
import java.util.Objects;

import static naitsirc98.beryl.util.Asserts.assertTrue;
import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;

public class Sphere implements ISphere {

    public final Vector3f center;
    public float radius;

    public Sphere() {
        center = new Vector3f();
    }

    public Sphere(Vector3f center, float radius) {
        this.center = center;
        this.radius = radius;
    }

    @Override
    public float centerX() {
        return center.x;
    }

    @Override
    public float centerY() {
        return center.y;
    }

    @Override
    public float centerZ() {
        return center.z;
    }

    @Override
    public Vector3fc center() {
        return center;
    }

    @Override
    public float radius() {
        return radius;
    }

    @Override
    public ByteBuffer get(int offset, ByteBuffer buffer) {
        assertTrue(buffer.remaining() - offset <= SIZEOF);

       center.get(offset, buffer);
       buffer.putFloat(offset + 3 * FLOAT32_SIZEOF, radius);

        return buffer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sphere sphere = (Sphere) o;
        return Float.compare(sphere.radius, radius) == 0 &&
                Objects.equals(center, sphere.center);
    }

    @Override
    public int hashCode() {
        return Objects.hash(center, radius);
    }

    @Override
    public String toString() {
        return "Sphere{" +
                "center=" + center +
                ", radius=" + radius +
                '}';
    }
}
