package naitsirc98.beryl.graphics;

import naitsirc98.beryl.graphics.rendering.Renderer;
import org.lwjgl.system.NativeResource;

public interface GraphicsContext extends NativeResource {

    void init();

    Renderer renderer();
}
