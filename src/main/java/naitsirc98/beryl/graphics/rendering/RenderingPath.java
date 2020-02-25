package naitsirc98.beryl.graphics.rendering;

public abstract class RenderingPath {

    boolean initialized;

    protected abstract void init();

    protected abstract void terminate();

    // TODO
    public abstract void render();
}
