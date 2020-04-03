package naitsirc98.beryl.lights;

import naitsirc98.beryl.util.types.ByteSize;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.nio.ByteBuffer;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.Asserts.assertTrue;
import static naitsirc98.beryl.util.Maths.radians;
import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;

@ByteSize.Static(Light.SIZEOF)
public class SpotLight extends Light<SpotLight> implements IPointLight<SpotLight>, IDirectionalLight<SpotLight> {

    private final Vector3f position;
    private final Vector3f direction;
    private float constant;
    private float linear;
    private float quadratic;
    private float cutOff;
    private float outerCutOff;

    public SpotLight() {
        position = new Vector3f();
        direction = new Vector3f();
        constant = 1.0f;
        linear = 0.09f;
        quadratic = 0.032f;
        cutOff = radians(12.5f);
        outerCutOff = radians(15.0f);
    }

    @Override
    public Vector3f direction() {
        return direction;
    }

    @Override
    public SpotLight direction(Vector3fc direction) {
        this.direction.set(requireNonNull(direction));
        return this;
    }

    @Override
    public SpotLight direction(float x, float y, float z) {
        this.direction.set(requireNonNull(direction));
        return this;
    }

    @Override
    public Vector3f position() {
        return position;
    }

    @Override
    public SpotLight position(Vector3fc position) {
        this.position.set(requireNonNull(position));
        return this;
    }

    @Override
    public SpotLight position(float x, float y, float z) {
        this.position.set(x, y, z);
        return this;
    }

    @Override
    public float constant() {
        return constant;
    }

    @Override
    public SpotLight constant(float constant) {
        this.constant = constant;
        return this;
    }

    @Override
    public float linear() {
        return linear;
    }

    @Override
    public SpotLight linear(float linear) {
        this.linear = linear;
        return this;
    }

    @Override
    public float quadratic() {
        return quadratic;
    }

    @Override
    public SpotLight quadratic(float quadratic) {
        this.quadratic = quadratic;
        return this;
    }

    public float cutOff() {
        return cutOff;
    }

    public SpotLight cutOff(float cutOff) {
        this.cutOff = cutOff;
        return this;
    }

    public float outerCutOff() {
        return outerCutOff;
    }

    public SpotLight outerCutOff(float outerCutOff) {
        this.outerCutOff = outerCutOff;
        return this;
    }

    @Override
    protected SpotLight self() {
        return this;
    }

    @Override
    public ByteBuffer get(int offset, ByteBuffer buffer) {
        assertTrue(buffer.remaining() >= SIZEOF);

        color().getRGBA(offset + COLOR_OFFSET, buffer);

        position.get(offset + POSITION_OFFSET, buffer);

        direction.get(offset + DIRECTION_OFFSET, buffer);

        buffer.putFloat(offset + CONSTANT_OFFSET, constant);
        buffer.putFloat(offset + LINEAR_OFFSET, linear);
        buffer.putFloat(offset + QUADRATIC_OFFSET, quadratic);

        buffer.putFloat(offset + CUTOFF_OFFSET, cutOff);
        buffer.putFloat(offset + OUTER_CUTOFF_OFFSET, outerCutOff);

        buffer.putInt(offset + TYPE_OFFSET, type());

        return buffer;
    }

    @Override
    public int type() {
        return LIGHT_TYPE_SPOT;
    }
}
