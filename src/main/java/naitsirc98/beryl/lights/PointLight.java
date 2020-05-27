package naitsirc98.beryl.lights;

import naitsirc98.beryl.util.types.ByteSize;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.nio.ByteBuffer;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.Asserts.assertTrue;

@ByteSize.Static(Light.SIZEOF)
public class PointLight extends Light<PointLight> implements IPointLight<PointLight> {

    private final Vector3f position;
    private float constant;
    private float linear;
    private float quadratic;

    public PointLight() {
        position = new Vector3f();
        constant = DEFAULT_CONSTANT;
        linear = DEFAULT_LINEAR;
        quadratic = DEFAULT_QUADRATIC;
    }

    @Override
    public Vector3f position() {
        return position;
    }

    @Override
    public PointLight position(Vector3fc position) {
        this.position.set(requireNonNull(position));
        return this;
    }

    @Override
    public PointLight position(float x, float y, float z) {
        this.position.set(x, y, z);
        return this;
    }

    @Override
    protected PointLight self() {
        return this;
    }

    @Override
    public float constant() {
        return constant;
    }

    @Override
    public PointLight constant(float constant) {
        this.constant = constant;
        return this;
    }

    @Override
    public float linear() {
        return linear;
    }

    @Override
    public PointLight linear(float linear) {
        this.linear = linear;
        return this;
    }

    @Override
    public float quadratic() {
        return quadratic;
    }

    @Override
    public PointLight quadratic(float quadratic) {
        this.quadratic = quadratic;
        return this;
    }

    @Override
    public ByteBuffer get(int offset, ByteBuffer buffer) {
        assertTrue(buffer.remaining() >= SIZEOF);

        color().getRGBA(offset + COLOR_OFFSET, buffer);

        position.get(offset + POSITION_OFFSET, buffer);

        buffer.putFloat(offset + CONSTANT_OFFSET, constant);
        buffer.putFloat(offset + LINEAR_OFFSET, linear);
        buffer.putFloat(offset + QUADRATIC_OFFSET, quadratic);

        buffer.putInt(offset + TYPE_OFFSET, type());

        return buffer;
    }

    @Override
    public int type() {
        return LIGHT_TYPE_POINT;
    }

}
