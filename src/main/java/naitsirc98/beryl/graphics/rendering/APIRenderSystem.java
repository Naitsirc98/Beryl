package naitsirc98.beryl.graphics.rendering;

import naitsirc98.beryl.graphics.rendering.renderers.AnimMeshRenderer;
import naitsirc98.beryl.graphics.rendering.renderers.SkyboxRenderer;
import naitsirc98.beryl.graphics.rendering.renderers.StaticMeshRenderer;
import naitsirc98.beryl.graphics.rendering.renderers.WaterRenderer;
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
        renderers.put(AnimMeshRenderer.class, getAnimMeshRenderer());
        renderers.put(SkyboxRenderer.class, getSkyboxRenderer());
        renderers.put(WaterRenderer.class, getWaterRenderer());
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
    protected abstract AnimMeshRenderer getAnimMeshRenderer();
    protected abstract SkyboxRenderer getSkyboxRenderer();
    protected abstract WaterRenderer getWaterRenderer();
    // TODO...
}
