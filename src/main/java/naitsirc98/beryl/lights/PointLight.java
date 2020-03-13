package naitsirc98.beryl.lights;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import static java.util.Objects.requireNonNull;

public class PointLight extends Light<PointLight> implements IPointLight<PointLight> {

    private final Vector3f position;
    private float constant;
    private float linear;
    private float quadratic;

    public PointLight() {
        position = new Vector3f();
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
}
