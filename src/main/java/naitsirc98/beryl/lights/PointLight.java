package naitsirc98.beryl.lights;

import naitsirc98.beryl.util.types.ByteSize;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.nio.FloatBuffer;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.Asserts.assertTrue;
import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;

@ByteSize.Static(PointLight.SIZEOF)
public class PointLight extends Light<PointLight> implements IPointLight<PointLight>, ByteSize {

    public static final int SIZEOF = (3 + 4 + 3) * FLOAT32_SIZEOF;

    private final Vector3f position;
    private float constant;
    private float linear;
    private float quadratic;

    public PointLight() {
        position = new Vector3f();
        constant = 1.0f;
        linear = 0.09f;
        quadratic = 0.032f;
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
    public FloatBuffer get(FloatBuffer buffer) {
        assertTrue(buffer.remaining() >= SIZEOF / FLOAT32_SIZEOF);

        position.get(buffer).position(buffer.position() + 3);

        color().getRGBA(buffer);

        buffer.put(constant).put(linear).put(quadratic);

        return buffer;
    }

    @Override
    public float type() {
        return LIGHT_TYPE_POINT;
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }
}
