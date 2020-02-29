package naitsirc98.beryl.graphics.rendering;

import naitsirc98.beryl.scenes.components.camera.Camera;
import naitsirc98.beryl.scenes.components.meshes.MeshView;

import java.util.List;

public abstract class RenderingPath {

    boolean initialized;

    protected abstract void init();

    protected abstract void terminate();

    // TODO
    public abstract void render(Camera camera, List<MeshView> meshViews);
}
