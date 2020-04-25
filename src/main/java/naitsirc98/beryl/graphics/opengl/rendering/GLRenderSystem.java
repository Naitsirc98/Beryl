package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.events.EventManager;
import naitsirc98.beryl.events.window.WindowResizedEvent;
import naitsirc98.beryl.graphics.opengl.rendering.shadows.GLShadowRenderer;
import naitsirc98.beryl.graphics.opengl.swapchain.GLFramebuffer;
import naitsirc98.beryl.graphics.opengl.swapchain.GLRenderbuffer;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2DMSAA;
import naitsirc98.beryl.graphics.rendering.APIRenderSystem;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.util.Color;
import naitsirc98.beryl.util.geometry.Sizec;

import static java.lang.StrictMath.max;
import static naitsirc98.beryl.core.BerylConfiguration.MSAA_SAMPLES;
import static naitsirc98.beryl.graphics.Graphics.opengl;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.glFinish;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL30.*;

public final class GLRenderSystem extends APIRenderSystem {

    private static final int DEFAULT_FRAMEBUFFER = 0;

    private final long glContext;

    private GLFramebuffer mainFramebuffer;

    private final GLMeshRenderer meshRenderer;
    private final GLSkyboxRenderer skyboxRenderer;
    private final GLWaterRenderer waterRenderer;
    private final GLShadowRenderer shadowRenderer;

    private GLRenderSystem() {

        glContext = opengl().handle();

        createMainFramebuffer();

        skyboxRenderer = new GLSkyboxRenderer();
        waterRenderer = new GLWaterRenderer();
        shadowRenderer = new GLShadowRenderer();
        meshRenderer = new GLMeshRenderer(shadowRenderer);

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

        meshRenderer.prepare(scene);

        shadowRenderer.render(scene);

        waterRenderer.bakeWaterTextures(scene);

        meshRenderer.preComputeFrustumCulling(scene);
    }

    @Override
    public void render(Scene scene) {

        mainFramebuffer.bind();

        clear(scene.environment().clearColor());

        meshRenderer.renderPreComputedVisibleObjects(scene);

        waterRenderer.render(scene);

        if(scene.environment().skybox() != null) {
            skyboxRenderer.render(scene);
        }
    }

    @Override
    public void end() {

        glFinish();

        copyFramebufferToScreen();

        glfwSwapBuffers(glContext);
    }

    private void clear(Color color) {
        glClearColor(color.red(), color.green(), color.blue(), color.alpha());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public GLStaticMeshRenderer getStaticMeshRenderer() {
        return meshRenderer.staticMeshRenderer();
    }

    @Override
    public GLAnimMeshRenderer getAnimMeshRenderer() {
        return meshRenderer.animMeshRenderer();
    }

    @Override
    public GLSkyboxRenderer getSkyboxRenderer() {
        return skyboxRenderer;
    }

    @Override
    public GLWaterRenderer getWaterRenderer() {
        return waterRenderer;
    }

    @Override
    public GLShadowRenderer getShadowRenderer() {
        return shadowRenderer;
    }

    private void copyFramebufferToScreen() {
        Sizec windowSize = Window.get().size();
        GLFramebuffer.blit(mainFramebuffer.handle(), DEFAULT_FRAMEBUFFER, windowSize.width(), windowSize.height(), GL_COLOR_BUFFER_BIT, GL_NEAREST);
    }

    private void createMainFramebuffer() {

        final int width = max(Window.get().width(), 1);
        final int height = max(Window.get().height(), 1);

        mainFramebuffer = new GLFramebuffer();

        GLTexture2DMSAA colorBuffer = new GLTexture2DMSAA();
        colorBuffer.allocate(MSAA_SAMPLES.get(), width, height, PixelFormat.RGBA);

        GLRenderbuffer depthStencilBuffer = new GLRenderbuffer();
        depthStencilBuffer.storageMultisample(width, height, GL_DEPTH24_STENCIL8, MSAA_SAMPLES.get());

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
