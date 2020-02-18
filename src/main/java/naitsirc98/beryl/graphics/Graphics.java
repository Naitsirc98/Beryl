package naitsirc98.beryl.graphics;

import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.core.BerylSystem;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.graphics.window.WindowFactory;
import naitsirc98.beryl.util.Singleton;

import static naitsirc98.beryl.util.TypeUtils.initSingleton;
import static naitsirc98.beryl.util.TypeUtils.newInstance;

public final class Graphics extends BerylSystem {

    @Singleton
    private static Graphics instance;

    private GraphicsContext graphicsContext;
    private Window window;

    private Graphics() {

    }

    @Override
    protected void init() {
        initSingleton(GraphicsAPI.class, BerylConfiguration.GRAPHICS_API.get(GraphicsAPI.OPENGL));
        window = newInstance(WindowFactory.class).newWindow();
    }

    @Override
    protected void terminate() {
        // TODO: release graphics resources
        window.destroy();
    }

}
