package naitsirc98.beryl.graphics.rendering;

import naitsirc98.beryl.core.BerylSystem;
import naitsirc98.beryl.graphics.opengl.rendering.GLRenderSystem;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.util.types.Singleton;

import static naitsirc98.beryl.util.types.TypeUtils.initSingleton;
import static naitsirc98.beryl.util.types.TypeUtils.newInstance;

public final class RenderSystem extends BerylSystem {

    @Singleton
    private static RenderSystem instance;

    private static APIRenderSystem apiRenderSystem;

    public static boolean shadowsEnabled() {
        return apiRenderSystem.shadowsEnabled();
    }

    public static void shadowsEnabled(boolean shadowsEnabled) {
        apiRenderSystem.shadowsEnabled(shadowsEnabled);
    }

    public static void prepare(Scene scene) {
        apiRenderSystem.prepare(scene);
    }

    public static void render(Scene scene) {
        apiRenderSystem.render(scene);
    }

    private RenderSystem() {

    }

    public APIRenderSystem apiRenderSystem() {
        return apiRenderSystem;
    }

    @Override
    protected void init() {
        // Only supporting OPENGL for now
        Class<? extends APIRenderSystem> apiRenderSystemClass = GLRenderSystem.class;
        apiRenderSystem = newInstance(apiRenderSystemClass);
        apiRenderSystem.init();
    }

    @Override
    protected void terminate() {
        apiRenderSystem.terminate();
        apiRenderSystem = null;
    }
}
