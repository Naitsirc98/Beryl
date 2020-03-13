package naitsirc98.beryl.lights;

import org.joml.Vector3f;
import org.joml.Vector3fc;

public interface IPointLight<SELF> {

    Vector3f position();
    SELF position(Vector3fc position);

    float constant();
    SELF constant(float constant);

    float linear();
    SELF linear(float linear);

    float quadratic();
    SELF quadratic(float quadratic);
}
