package naitsirc98.beryl.lights;

import org.joml.Vector3f;
import org.joml.Vector3fc;

public interface IDirectionalLight<SELF> {

    Vector3f direction();
    SELF direction(Vector3fc direction);
}
