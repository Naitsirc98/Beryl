package naitsirc98.beryl.util.geometry;

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

}
