package naitsirc98.beryl.graphics;

import org.lwjgl.system.NativeResource;

public interface GraphicsContext extends NativeResource {

    void init();

    Renderer renderer();
}
