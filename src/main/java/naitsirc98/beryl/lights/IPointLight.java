package naitsirc98.beryl.lights;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import static org.joml.Math.*;

public interface IPointLight<SELF extends IPointLight<SELF>> {

    float ATTENUATION_THRESHOLD = 0.01f;

    default SELF range(LightRange range) {
        return constant(range.constant())
                .linear(range.linear())
                .quadratic(range.quadratic());
    }

    default float radius() {
        // qd^2 + ld + (c - 1/AT) = 0
        final float a = quadratic();
        final float b = linear();
        final float c = constant() - 1.0f / ATTENUATION_THRESHOLD;
        return (float) ((-b + sqrt(b * b - 4 * a * c)) / (2 * a));
    }

    default float attenuation(float distance) {
        return 1.0f /
                (constant() + distance * linear() + distance * distance * quadratic());
    }

    Vector3f position();
    SELF position(Vector3fc position);
    SELF position(float x, float y, float z);

    float constant();
    SELF constant(float constant);

    float linear();
    SELF linear(float linear);

    float quadratic();
    SELF quadratic(float quadratic);
}
