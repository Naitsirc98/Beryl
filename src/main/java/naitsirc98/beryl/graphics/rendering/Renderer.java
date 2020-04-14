package naitsirc98.beryl.graphics.rendering;

import naitsirc98.beryl.scenes.Scene;

public abstract class Renderer {

    protected abstract void init();

    protected abstract void terminate();

    public abstract void prepare(Scene scene);

    public abstract void render(Scene scene);
}
