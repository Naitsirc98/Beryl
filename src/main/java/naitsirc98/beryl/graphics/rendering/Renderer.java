package naitsirc98.beryl.graphics.rendering;

import org.lwjgl.system.NativeResource;

public interface Renderer extends NativeResource {

    boolean begin();

    void end();

}
