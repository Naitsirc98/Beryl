package naitsirc98.beryl.graphics;

import naitsirc98.beryl.graphics.rendering.Renderer;
import naitsirc98.beryl.graphics.rendering.RenderingPath;
import org.lwjgl.system.NativeResource;

import java.util.Map;

public interface GraphicsContext extends NativeResource {

    void init();

    Renderer renderer();

    Map<Integer, RenderingPath> renderingPaths();
}
