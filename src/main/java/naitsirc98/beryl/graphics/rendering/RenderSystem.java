package naitsirc98.beryl.graphics.rendering;

import naitsirc98.beryl.core.BerylSystem;
import naitsirc98.beryl.graphics.GraphicsAPI;
import naitsirc98.beryl.graphics.opengl.rendering.GLRenderer;
import naitsirc98.beryl.graphics.vulkan.rendering.VulkanRenderer;
import naitsirc98.beryl.util.types.Singleton;

import static naitsirc98.beryl.graphics.GraphicsAPI.VULKAN;
import static naitsirc98.beryl.util.types.TypeUtils.newInstance;

public final class RenderSystem extends BerylSystem {

    @Singleton
    private static RenderSystem instance;

    public static Renderer renderer() {
        return instance.renderer;
    }

    private Renderer renderer;

    private RenderSystem() {

    }

    @Override
    protected void init() {
        Class<? extends Renderer> rendererClass = GraphicsAPI.get() == VULKAN ? VulkanRenderer.class : GLRenderer.class;
        renderer = newInstance(rendererClass);
    }

    @Override
    protected void terminate() {
        renderer.release();
        renderer = null;
    }
}
