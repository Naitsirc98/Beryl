package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.events.EventManager;
import naitsirc98.beryl.events.window.WindowResizedEvent;
import naitsirc98.beryl.graphics.opengl.swapchain.GLFramebuffer;
import naitsirc98.beryl.graphics.opengl.swapchain.GLRenderbuffer;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2DMSAA;
import naitsirc98.beryl.graphics.rendering.APIRenderSystem;
import naitsirc98.beryl.graphics.rendering.renderers.AnimMeshRenderer;
import naitsirc98.beryl.graphics.rendering.renderers.SkyboxRenderer;
import naitsirc98.beryl.graphics.rendering.renderers.StaticMeshRenderer;
import naitsirc98.beryl.graphics.rendering.renderers.WaterRenderer;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.util.Color;
import naitsirc98.beryl.util.geometry.Sizec;

import static naitsirc98.beryl.core.BerylConfiguration.MSAA_SAMPLES;
import static naitsirc98.beryl.graphics.Graphics.opengl;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.glFinish;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.glClearColor;
import static org.lwjgl.opengl.GL30.*;

public final class GLRenderSystem extends APIRenderSystem {

    private static final int DEFAULT_FRAMEBUFFER = 0;

    private final long glContext;

    private GLFramebuffer mainFramebuffer;

    private final GLStaticMeshRenderer staticMeshRenderer;
    private final GLAnimMeshRenderer animMeshRenderer;
    private final GLSkyboxRenderer skyboxRenderer;
    private final GLWaterRenderer waterRenderer;

    private GLRenderSystem() {
        glContext = opengl().handle();
        createMainFramebuffer();
        staticMeshRenderer = new GLStaticMeshRenderer();
        animMeshRenderer = new GLAnimMeshRenderer();
        skyboxRenderer = new GLSkyboxRenderer();
        waterRenderer = new GLWaterRenderer();
        EventManager.addEventCallback(WindowResizedEvent.class, this::recreateFramebuffer);
    }

    public GLFramebuffer mainFramebuffer() {
        return mainFramebuffer;
    }

    @Override
    public void begin() {
        Sizec framebufferSize = Window.get().framebufferSize();
        glViewport(0, 0, framebufferSize.width(), framebufferSize.height());
        glBindFramebuffer(GL_FRAMEBUFFER, DEFAULT_FRAMEBUFFER);
    }

    @Override
    public void prepare(Scene scene) {

        staticMeshRenderer.prepare(scene);

        animMeshRenderer.prepare(scene);

        waterRenderer.bakeWaterTextures(scene, staticMeshRenderer, skyboxRenderer);
    }

    @Override
    public void render(Scene scene) {

        mainFramebuffer.bind();

        clear(scene.environment().clearColor());

        animMeshRenderer.render(scene, animMeshRenderer.performCullingPassCPU(scene, false));

        staticMeshRenderer.render(scene, staticMeshRenderer.performCullingPassCPU(scene, false));

        waterRenderer.render(scene);

        if(scene.environment().skybox() != null) {
            skyboxRenderer.render(scene);
        }
    }

    private void clear(Color color) {
        glClearColor(color.red(), color.green(), color.blue(), color.alpha());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }

    @Override
    public void end() {
        glFinish();
        copyFramebufferToScreen();
        glfwSwapBuffers(glContext);
    }

    @Override
    protected StaticMeshRenderer getStaticMeshRenderer() {
        return staticMeshRenderer;
    }

    @Override
    protected AnimMeshRenderer getAnimMeshRenderer() {
        return animMeshRenderer;
    }

    @Override
    protected SkyboxRenderer getSkyboxRenderer() {
        return skyboxRenderer;
    }

    @Override
    protected WaterRenderer getWaterRenderer() {
        return waterRenderer;
    }

    private void copyFramebufferToScreen() {
        Sizec windowSize = Window.get().size();
        GLFramebuffer.blit(mainFramebuffer.handle(), DEFAULT_FRAMEBUFFER, windowSize.width(), windowSize.height(), GL_COLOR_BUFFER_BIT, GL_NEAREST);
    }

    private void createMainFramebuffer() {

        mainFramebuffer = new GLFramebuffer();

        GLTexture2DMSAA colorBuffer = new GLTexture2DMSAA();
        colorBuffer.allocate(MSAA_SAMPLES.get(), Window.get().width(), Window.get().height(), PixelFormat.RGBA);

        GLRenderbuffer depthStencilBuffer = new GLRenderbuffer();
        depthStencilBuffer.storageMultisample(Window.get().width(), Window.get().height(), GL_DEPTH24_STENCIL8, MSAA_SAMPLES.get());

        mainFramebuffer.attach(GL_COLOR_ATTACHMENT0, colorBuffer, 0);
        mainFramebuffer.attach(GL_DEPTH_STENCIL_ATTACHMENT, depthStencilBuffer);

        mainFramebuffer.freeAttachmentsOnRelease(true);

        mainFramebuffer.ensureComplete();
    }

    private void recreateFramebuffer(WindowResizedEvent e) {
        if(mainFramebuffer != null) {
            mainFramebuffer.release();
            createMainFramebuffer();
        }
    }
}
