package naitsirc98.beryl.lights;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import static java.util.Objects.requireNonNull;

public class SpotLight extends Light<SpotLight> implements IPointLight<SpotLight>, IDirectionalLight<SpotLight> {

    private final Vector3f position;
    private final Vector3f direction;
    private float constant;
    private float linear;
    private float quadratic;
    private float cutOff;
    private float cutOffAngle;

    public SpotLight() {
        position = new Vector3f();
        direction = new Vector3f();
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
    public Vector3f position() {
        return position;
    }

    @Override
    public SpotLight position(Vector3fc position) {
        this.position.set(requireNonNull(position));
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

    public float cutOffAngle() {
        return cutOffAngle;
    }

    public SpotLight cutOffAngle(float cutOffAngle) {
        this.cutOffAngle = cutOff;
        return this;
    }

    @Override
    protected SpotLight self() {
        return this;
    }
}
