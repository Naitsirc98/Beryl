package naitsirc98.beryl.lights;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import static java.util.Objects.requireNonNull;

public class DirectionalLight extends Light<DirectionalLight> implements IDirectionalLight<DirectionalLight> {

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
}
