package naitsirc98.beryl.scenes;

import naitsirc98.beryl.util.Color;
import naitsirc98.beryl.util.IColor;
import naitsirc98.beryl.util.types.ByteSize;

import java.nio.ByteBuffer;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.Asserts.assertTrue;

@ByteSize.Static(Fog.SIZEOF)
public final class Fog implements ByteSize {

    public static final int SIZEOF = 20;

    public static final float DEFAULT_FOG_DENSITY = 0.16f;

    private final Color color;
    private float density;

    public Fog() {
        color = Color.colorWhite();
        density = DEFAULT_FOG_DENSITY;
    }

    public Color color() {
        return color;
    }

    public Fog color(IColor color) {
        this.color.set(requireNonNull(color));
        return this;
    }

    public float density() {
        return density;
    }

    public Fog density(float density) {
        this.density = density;
        return this;
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    public ByteBuffer get(int offset, ByteBuffer buffer) {
        assertTrue(buffer.position() + offset + SIZEOF <= buffer.limit());

        color.getRGBA(offset, buffer);
        buffer.putFloat(offset + Color.SIZEOF, density);

        return buffer;
    }
}
