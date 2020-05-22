package naitsirc98.beryl.graphics.rendering;

import naitsirc98.beryl.core.BerylSystem;
import naitsirc98.beryl.core.BerylSystemManager;
import naitsirc98.beryl.graphics.opengl.rendering.GLRenderSystem;
import naitsirc98.beryl.util.types.Singleton;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.types.TypeUtils.initSingleton;
import static naitsirc98.beryl.util.types.TypeUtils.newInstance;

public final class RenderSystem extends BerylSystem {

    @Singleton
    private static RenderSystem instance;

    private static APIRenderSystem apiRenderSystem;

    private RenderSystem(BerylSystemManager systemManager) {
        super(systemManager);
    }

    public APIRenderSystem getAPIRenderSystem() {
        return apiRenderSystem;
    }

    @Override
    protected void init() {
        // Only supporting OPENGL for now
        Class<? extends APIRenderSystem> apiRenderSystemClass = GLRenderSystem.class;
        apiRenderSystem = requireNonNull(newInstance(apiRenderSystemClass));
        apiRenderSystem.init();
    }

    @Override
    protected void terminate() {
        apiRenderSystem.terminate();
        apiRenderSystem = null;
    }
}
