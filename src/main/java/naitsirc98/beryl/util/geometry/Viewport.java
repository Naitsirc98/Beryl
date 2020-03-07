package naitsirc98.beryl.util.geometry;

import java.util.Objects;

public final class Viewport implements Viewportc {

    private int x;
    private int y;
    private int width;
    private int height;

    public Viewport(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Viewport(int width, int height) {
        this(0, 0, width, height);
    }

    @Override
    public int x() {
        return x;
    }

    public Viewport x(int x) {
        this.x = x;
        return this;
    }

    @Override
    public int y() {
        return y;
    }

    public Viewport y(int y) {
        this.y = y;
        return this;
    }

    @Override
    public int width() {
        return width;
    }

    public Viewport width(int width) {
        this.width = width;
        return this;
    }

    @Override
    public int height() {
        return height;
    }

    public Viewport height(int height) {
        this.height = height;
        return this;
    }

    public Viewport set(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Viewport viewport = (Viewport) o;
        return x == viewport.x &&
                y == viewport.y &&
                width == viewport.width &&
                height == viewport.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height);
    }

    @Override
    public String toString() {
        return "Viewport{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
