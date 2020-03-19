package naitsirc98.beryl.lights;

import naitsirc98.beryl.util.Color;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static java.util.Objects.requireNonNull;

public abstract class Light<SELF extends Light<SELF>> {

    public static final float LIGHT_TYPE_DIRECTIONAL = 0.0f;
    public static final float LIGHT_TYPE_POINT = 1.0f;
    public static final float LIGHT_TYPE_SPOT = 2.0f;

    private Color color;

    public Light() {
        color = Color.WHITE;
    }

    public Color color() {
        return color;
    }

    public SELF color(Color color) {
        this.color = requireNonNull(color);
        return self();
    }

    public abstract ByteBuffer get(ByteBuffer buffer);

    public abstract float type();

    protected abstract SELF self();
}
