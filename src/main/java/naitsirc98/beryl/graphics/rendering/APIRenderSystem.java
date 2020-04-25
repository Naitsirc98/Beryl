package naitsirc98.beryl.graphics.rendering;

import naitsirc98.beryl.graphics.rendering.renderers.*;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.util.types.Singleton;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class APIRenderSystem {

    @Singleton
    private static APIRenderSystem instance;

    public static APIRenderSystem get() {
        return instance;
    }

    private final Map<Class<? extends Renderer>, Renderer> renderers;

    protected APIRenderSystem() {
        this.renderers = new LinkedHashMap<>();
    }

    final void init() {
        renderers.put(StaticMeshRenderer.class, getStaticMeshRenderer());
        renderers.put(AnimMeshRenderer.class, getAnimMeshRenderer());
        renderers.put(SkyboxRenderer.class, getSkyboxRenderer());
        renderers.put(WaterRenderer.class, getWaterRenderer());
        renderers.put(ShadowRenderer.class, getShadowRenderer());
        // TODO...
        renderers.values().forEach(Renderer::init);
    }

    final void terminate() {
        renderers.values().forEach(Renderer::terminate);
    }

    public abstract void begin();

    public abstract void prepare(Scene scene);

    public abstract void render(Scene scene);

    public abstract void end();

    // Renderers
    public abstract StaticMeshRenderer getStaticMeshRenderer();
    public abstract AnimMeshRenderer getAnimMeshRenderer();
    public abstract SkyboxRenderer getSkyboxRenderer();
    public abstract WaterRenderer getWaterRenderer();
    public abstract ShadowRenderer getShadowRenderer();
    // TODO...
}
