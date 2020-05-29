package naitsirc98.beryl.graphics.opengl.rendering.renderers;

import naitsirc98.beryl.graphics.opengl.GLContext;
import naitsirc98.beryl.graphics.rendering.Renderer;

public abstract class GLRenderer implements Renderer {

    private final GLContext context;

    public GLRenderer(GLContext context) {
        this.context = context;
    }

    public GLContext context() {
        return context;
    }
}
