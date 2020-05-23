package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.events.EventManager;
import naitsirc98.beryl.events.window.WindowResizedEvent;
import naitsirc98.beryl.graphics.opengl.GLShadingPipelineManager;
import naitsirc98.beryl.graphics.opengl.rendering.renderers.GLMeshRenderer;
import naitsirc98.beryl.graphics.opengl.rendering.renderers.GLSkyboxRenderer;
import naitsirc98.beryl.graphics.opengl.rendering.renderers.GLWaterRenderer;
import naitsirc98.beryl.graphics.opengl.rendering.shadows.GLShadowRenderer;
import naitsirc98.beryl.graphics.opengl.swapchain.GLFramebuffer;
import naitsirc98.beryl.graphics.opengl.swapchain.GLRenderbuffer;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2DMSAA;
import naitsirc98.beryl.graphics.rendering.APIRenderSystem;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.SceneRenderInfo;
import naitsirc98.beryl.scenes.environment.SceneEnvironment;
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
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER_SRGB;

public final class GLRenderSystem implements APIRenderSystem {

    private static final int DEFAULT_FRAMEBUFFER = 0;

    private final long glContext;

    private GLFramebuffer mainFramebuffer;

    // Renderers
    private final GLSkyboxRenderer skyboxRenderer;
    private final GLShadowRenderer shadowRenderer;
    private final GLMeshRenderer meshRenderer;
    private final GLWaterRenderer waterRenderer;
    // Shading Pipelines
    private final GLShadingPipelineManager shadingPipelineManager;
    private GLShadingPipeline currentShadingPipeline;

    private GLRenderSystem() {

        glContext = opengl().handle();

        createMainFramebuffer();

        skyboxRenderer = new GLSkyboxRenderer();
        shadowRenderer = new GLShadowRenderer();
        meshRenderer = new GLMeshRenderer(shadowRenderer);
        waterRenderer = new GLWaterRenderer(meshRenderer, skyboxRenderer);

        shadingPipelineManager = new GLShadingPipelineManager();

        EventManager.addEventCallback(WindowResizedEvent.class, this::recreateFramebuffer);
    }

    @Override
    public void init() {
        meshRenderer.init();
        skyboxRenderer.init();
        waterRenderer.init();
        shadowRenderer.init();
        shadingPipelineManager.init();
    }

    @Override
    public void terminate() {
        shadowRenderer.terminate();
        waterRenderer.terminate();
        skyboxRenderer.terminate();
        meshRenderer.terminate();
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

        currentShadingPipeline = getCurrentShadingPipeline(scene.renderInfo());

        meshRenderer.prepare(scene);

        if(currentShadingPipeline.areShadowsEnabled()) {
            shadowRenderer.render(scene, meshRenderer);
        }

        waterRenderer.bakeWaterTextures(scene, currentShadingPipeline);

        meshRenderer.preComputeFrustumCulling(scene);
    }

    @Override
    public void render(Scene scene) {

        SceneEnvironment environment = scene.environment();

        mainFramebuffer.bind();

        clear(environment.clearColor());

        meshRenderer.renderPreComputedVisibleObjects(scene, currentShadingPipeline);

        waterRenderer.render(scene);

        if(environment.skybox() != null) {
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

    private void copyFramebufferToScreen() {
        Sizec windowSize = Window.get().size();
        GLFramebuffer.blit(mainFramebuffer.handle(), DEFAULT_FRAMEBUFFER, windowSize.width(), windowSize.height(), GL_COLOR_BUFFER_BIT, GL_NEAREST);
    }

    private void createMainFramebuffer() {

        glEnable(GL_FRAMEBUFFER_SRGB);

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

    private GLShadingPipeline getCurrentShadingPipeline(SceneRenderInfo renderInfo) {
        return shadingPipelineManager.getShadingPipeline(renderInfo);
    }
}
