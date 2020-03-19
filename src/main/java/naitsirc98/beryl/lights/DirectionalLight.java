package naitsirc98.beryl.lights;

import naitsirc98.beryl.util.types.ByteSize;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.Asserts.assertTrue;
import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;

@ByteSize.Static(DirectionalLight.SIZEOF)
public class DirectionalLight extends Light<DirectionalLight> implements IDirectionalLight<DirectionalLight>, ByteSize {

    public static final int SIZEOF = (3 + 4) * FLOAT32_SIZEOF;

    private final Vector3f direction;

    public DirectionalLight() {
        direction = new Vector3f();
    }

    @Override
    public Vector3f direction() {
        return direction;
    }

    @Override
    public DirectionalLight direction(Vector3fc direction) {
        this.direction.set(requireNonNull(direction));
        return this;
    }

    @Override
    protected DirectionalLight self() {
        return this;
    }

    @Override
    public ByteBuffer get(ByteBuffer buffer) {
        assertTrue(buffer.remaining() >= SIZEOF);

        direction.get(buffer).position(buffer.position() + 3 * FLOAT32_SIZEOF);

        color().getRGBA(buffer);

        return buffer;
    }

    @Override
    public float type() {
        return LIGHT_TYPE_DIRECTIONAL;
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }
}
