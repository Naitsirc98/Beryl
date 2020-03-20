package naitsirc98.beryl.graphics.rendering;

import naitsirc98.beryl.resources.Resource;

public interface Renderer extends Resource {

    @SuppressWarnings("unchecked")
    static <T extends Renderer> T get() {
        return (T) RenderSystem.renderer();
    }

    boolean begin();

    void end();

}
