package naitsirc98.beryl.graphics.rendering;

import org.lwjgl.system.NativeResource;

public interface Renderer extends NativeResource {

    @SuppressWarnings("unchecked")
    static <T extends Renderer> T get() {
        return (T) RenderSystem.renderer();
    }

    boolean begin();

    void end();

}
