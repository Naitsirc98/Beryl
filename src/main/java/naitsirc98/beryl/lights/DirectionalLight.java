package naitsirc98.beryl.lights;

import naitsirc98.beryl.util.types.ByteSize;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.nio.ByteBuffer;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.Asserts.assertTrue;

@ByteSize.Static(Light.SIZEOF)
public class DirectionalLight extends Light<DirectionalLight> implements IDirectionalLight<DirectionalLight> {

    private final Vector3f direction;

    public DirectionalLight() {
        direction = new Vector3f();
    }

    @Override
    public Vector3fc direction() {
        return direction;
    }

    @Override
    public DirectionalLight direction(Vector3fc direction) {
        this.direction.set(requireNonNull(direction));
        return this;
    }

    @Override
    public DirectionalLight direction(float x, float y, float z) {
        this.direction.set(x, y, z);
        return this;
    }

    @Override
    protected DirectionalLight self() {
        return this;
    }

    @Override
    public ByteBuffer get(int offset, ByteBuffer buffer) {
        assertTrue(buffer.remaining() >= SIZEOF);

        color().getRGBA(offset + COLOR_OFFSET, buffer);

        direction.get(offset + DIRECTION_OFFSET, buffer);

        buffer.putInt(offset + TYPE_OFFSET, type());

        return buffer;
    }

    @Override
    public int type() {
        return LIGHT_TYPE_DIRECTIONAL;
    }
}
