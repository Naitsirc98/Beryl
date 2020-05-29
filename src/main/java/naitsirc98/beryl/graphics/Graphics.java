package naitsirc98.beryl.graphics;

import naitsirc98.beryl.core.BerylSystem;
import naitsirc98.beryl.core.BerylSystemManager;
import naitsirc98.beryl.graphics.opengl.GLContext;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.graphics.window.WindowFactory;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.util.types.Singleton;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.core.BerylConfigConstants.GRAPHICS_API;
import static naitsirc98.beryl.graphics.GraphicsAPI.OPENGL;
import static naitsirc98.beryl.util.types.TypeUtils.initSingleton;
import static naitsirc98.beryl.util.types.TypeUtils.newInstance;

public final class Graphics extends BerylSystem {

    @Singleton
    private static Graphics instance;

    public static GraphicsContext graphicsContext() {
        return instance.graphicsContext;
    }


    private GraphicsContext graphicsContext;
    private Window window;

    private Graphics(BerylSystemManager systemManager) {
        super(systemManager);
    }

    @Override
    protected void init() {

        GraphicsAPI chosenGraphicsAPI = GRAPHICS_API;

        if(chosenGraphicsAPI != OPENGL) {
            Log.fatal("Beryl does not support " + chosenGraphicsAPI + " at the moment. Use OPENGL instead");
            return;
        }

        initSingleton(GraphicsAPI.class, chosenGraphicsAPI);

        Log.info("Using " + chosenGraphicsAPI + " as the Graphics API");

        Log.info("Creating window...");

        window = requireNonNull(newInstance(WindowFactory.class)).newWindow();

        Log.info("Window created");

        Log.info("Creating Graphics Context...");

        graphicsContext = createGraphicsContext();
        graphicsContext.init();

        Log.info(GraphicsAPI.get()  + " Context created");
    }

    private GraphicsContext createGraphicsContext() {
        return newInstance(GLContext.class);
    }

    @Override
    protected void terminate() {
        if(graphicsContext != null) {
            graphicsContext.release();
        }
        window.destroy();
    }

}
