package naitsirc98.beryl.graphics.rendering.renderers;

import naitsirc98.beryl.graphics.rendering.Renderer;
import naitsirc98.beryl.scenes.Scene;

public abstract class SkyboxRenderer extends Renderer {

    public abstract void prepare(Scene scene);

    public abstract void render(Scene scene);

}
