package naitsirc98.beryl.lights;

import naitsirc98.beryl.util.Color;
import naitsirc98.beryl.util.IColor;
import naitsirc98.beryl.util.types.ByteSize;

import java.nio.ByteBuffer;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;

@ByteSize.Static(Light.SIZEOF)
public abstract class Light<SELF extends Light<SELF>> implements ByteSize {

    public static final int LIGHT_TYPE_DIRECTIONAL = 1;
    public static final int LIGHT_TYPE_POINT = 2;
    public static final int LIGHT_TYPE_SPOT = 3;

    /*
    struct Light {

        vec4 color;

        vec4 position;
        vec4 direction;

        float constant;
        float linear;
        float quadratic;

        float cutOff;
        float outerCutOff;

        float nearPlane;
        float farPlane;

        int type;
    };
    */

    /** Size in bytes of the Light struct defined in GLSL */
    public static final int SIZEOF = (5 * 4) * FLOAT32_SIZEOF;

    public static final int COLOR_OFFSET = 0;
    public static final int POSITION_OFFSET = 4 * FLOAT32_SIZEOF;
    public static final int DIRECTION_OFFSET = 8 * FLOAT32_SIZEOF;
    public static final int CONSTANT_OFFSET = 12 * FLOAT32_SIZEOF;
    public static final int LINEAR_OFFSET = 13 * FLOAT32_SIZEOF;
    public static final int QUADRATIC_OFFSET = 14 * FLOAT32_SIZEOF;
    public static final int CUTOFF_OFFSET = 15 * FLOAT32_SIZEOF;
    public static final int OUTER_CUTOFF_OFFSET = 16 * FLOAT32_SIZEOF;
    public static final int NEAR_PLANE_OFFSET = 17 * FLOAT32_SIZEOF;
    public static final int FAR_PLANE_OFFSET = 18 * FLOAT32_SIZEOF;
    public static final int TYPE_OFFSET = 19 * FLOAT32_SIZEOF;

    private final Color color;
    private float nearPlane;
    private float farPlane;

    public Light() {
        color = Color.colorWhite();
    }

    public Color color() {
        return color;
    }

    public SELF color(IColor color) {
        this.color.set(requireNonNull(color));
        return self();
    }

    public float nearPlane() {
        return nearPlane;
    }

    public Light<SELF> nearPlane(float nearPlane) {
        this.nearPlane = nearPlane;
        return this;
    }

    public float farPlane() {
        return farPlane;
    }

    public Light<SELF> farPlane(float farPlane) {
        this.farPlane = farPlane;
        return this;
    }

    /**
     * Stores this light's instance data into the supplied {@link ByteBuffer}. This method does not modify the position
     * of the given buffer.
     *
     * @param offset the start offset in the buffer
     * @param buffer the buffer to store this light's data
     * @return the given buffer
     */
    public abstract ByteBuffer get(int offset, ByteBuffer buffer);

    public abstract int type();

    protected abstract SELF self();

    @Override
    public int sizeof() {
        return SIZEOF;
    }
}
