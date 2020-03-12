package naitsirc98.beryl.graphics;

import naitsirc98.beryl.graphics.rendering.RenderingPath;
import org.lwjgl.system.NativeResource;

import java.util.Map;

public interface GraphicsContext extends NativeResource {

    void init();

    GraphicsMapper mapper();

    Map<Integer, RenderingPath> renderingPaths();

    GraphicsFactory graphicsFactory();
}
