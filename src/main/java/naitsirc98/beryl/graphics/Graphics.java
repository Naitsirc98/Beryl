package naitsirc98.beryl.graphics;

import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.core.BerylSystem;
import naitsirc98.beryl.graphics.opengl.GLContext;
import naitsirc98.beryl.graphics.rendering.Renderer;
import naitsirc98.beryl.graphics.vulkan.VulkanContext;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.graphics.window.WindowFactory;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.util.Singleton;

import static naitsirc98.beryl.graphics.GraphicsAPI.VULKAN;
import static naitsirc98.beryl.util.TypeUtils.*;

public final class Graphics extends BerylSystem {

    @Singleton
    private static Graphics instance;

    public static VulkanContext vulkan() {
        return (VulkanContext) instance.graphicsContext;
    }

    public static GLContext opengl() {
        return (GLContext) instance.graphicsContext;
    }

    private GraphicsContext graphicsContext;
    private Window window;

    private Graphics() {

    }

    public Renderer renderer() {
        return graphicsContext.renderer();
    }

    @Override
    protected void init() {

        Log.trace("Initializing Graphics...");

        initSingleton(GraphicsAPI.class, BerylConfiguration.GRAPHICS_API.get(VULKAN));

        Log.trace("Using " + GraphicsAPI.get() + " as the Graphics API");

        Log.trace("Creating window...");

        window = newInstance(WindowFactory.class).newWindow();

        Log.trace("Window created");

        Log.trace("Creating Graphics Context...");

        graphicsContext = createGraphicsContext();
        graphicsContext.init();

        Log.trace(GraphicsAPI.get()  + " Context created");
    }

    private GraphicsContext createGraphicsContext() {
        return GraphicsAPI.get() == VULKAN ? newInstance(VulkanContext.class) : newInstance(GLContext.class);
    }

    @Override
    protected void terminate() {
        graphicsContext.free();
        delete(window);
    }

}
