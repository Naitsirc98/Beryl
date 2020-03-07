package naitsirc98.beryl.util.geometry;

import java.util.Objects;

public class Size implements Sizec {

    private int width;
    private int height;

    public Size() {
    }

    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public int width() {
        return width;
    }

    public Size width(int width) {
        this.width = width;
        return this;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public float aspect() {
        return width / Math.max(height, 1.0f);
    }

    public Size height(int height) {
        this.height = height;
        return this;
    }

    public Size set(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Size size = (Size) o;
        return width == size.width &&
                height == size.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height);
    }

    @Override
    public String toString() {
        return "Size{" +
                "width=" + width +
                ", height=" + height +
                '}';
    }
}
