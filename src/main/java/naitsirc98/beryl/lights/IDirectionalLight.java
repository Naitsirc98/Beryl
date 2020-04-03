package naitsirc98.beryl.lights;

import org.joml.Vector3fc;

public interface IDirectionalLight<SELF> {

    Vector3fc direction();

    SELF direction(Vector3fc direction);

    SELF direction(float x, float y, float z);
}
