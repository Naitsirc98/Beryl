package naitsirc98.beryl.graphics.rendering;

import naitsirc98.beryl.graphics.rendering.renderers.StaticMeshRenderer;
import naitsirc98.beryl.graphics.rendering.renderers.TerrainRenderer;
import naitsirc98.beryl.scenes.Scene;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class APIRenderSystem {

    private final Map<Class<? extends Renderer>, Renderer> renderers;

    public APIRenderSystem() {
        this.renderers = new LinkedHashMap<>();
    }

    final void init() {
        renderers.put(StaticMeshRenderer.class, getStaticMeshRenderer());
        // TODO...
        renderers.values().forEach(Renderer::init);
    }

    final void terminate() {
        renderers.values().forEach(Renderer::terminate);
    }

    public final <T extends Renderer> T renderer(Class<T> rendererClass) {
        return rendererClass.cast(renderers.get(rendererClass));
    }

    public abstract void begin();

    public abstract void prepare(Scene scene);

    public abstract void render(Scene scene);

    public abstract void end();

    // Renderers
    protected abstract StaticMeshRenderer getStaticMeshRenderer();
    protected abstract TerrainRenderer getTerrainMeshRenderer();
    // TODO...
}
