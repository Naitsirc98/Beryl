package naitsirc98.beryl.graphics.rendering;

import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.camera.Camera;

public abstract class RenderingPath {


    boolean initialized;

    protected abstract void init();

    protected abstract void terminate();

    // TODO
    public abstract void render(Camera camera, Scene scene);
}
