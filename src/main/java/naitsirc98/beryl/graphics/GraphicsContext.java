package naitsirc98.beryl.graphics;

import naitsirc98.beryl.graphics.rendering.RenderingPath;
import naitsirc98.beryl.resources.Resource;

import java.util.Map;

public interface GraphicsContext extends Resource {

    void init();

    boolean vsync();

    void vsync(boolean vsync);

    GraphicsMapper mapper();

    Map<Integer, RenderingPath> renderingPaths();

    GraphicsFactory graphicsFactory();
}
