package naitsirc98.beryl.util;

public class Rect implements Rectc {

    private int left;
    private int right;
    private int top;
    private int bottom;

    public Rect() {
    }

    public Rect(int left, int right, int top, int bottom) {
        set(left, right, top, bottom);
    }

    @Override
    public int left() {
        return left;
    }

    public Rect left(int left) {
        this.left = left;
        return this;
    }

    @Override
    public int right() {
        return right;
    }

    public Rect right(int right) {
        this.right = right;
        return this;
    }

    @Override
    public int top() {
        return top;
    }

    public Rect top(int top) {
        this.top = top;
        return this;
    }

    @Override
    public int bottom() {
        return bottom;
    }

    public Rect bottom(int bottom) {
        this.bottom = bottom;
        return this;
    }

    public Rect set(int left, int right, int top, int bottom) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
        return this;
    }
}
