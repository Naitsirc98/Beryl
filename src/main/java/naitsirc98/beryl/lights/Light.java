package naitsirc98.beryl.lights;

import naitsirc98.beryl.util.Color;

import static java.util.Objects.requireNonNull;

public abstract class Light<SELF extends Light<SELF>> {

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

    protected abstract SELF self();
}
