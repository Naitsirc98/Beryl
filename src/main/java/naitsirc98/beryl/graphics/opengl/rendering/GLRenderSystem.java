package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.graphics.rendering.APIRenderSystem;
import naitsirc98.beryl.graphics.rendering.renderers.StaticMeshRenderer;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.util.geometry.Sizec;

import static naitsirc98.beryl.graphics.Graphics.opengl;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.glFinish;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

public final class GLRenderSystem extends APIRenderSystem {

    private static final int DEFAULT_FRAMEBUFFER = 0;

    private final long glContext;

    private final GLStaticMeshRenderer staticMeshRenderer;

    private GLRenderSystem() {
        glContext = opengl().handle();
        staticMeshRenderer = new GLStaticMeshRenderer();
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
    }

    @Override
    public void render(Scene scene) {
        staticMeshRenderer.render(scene);
    }

    @Override
    public void end() {
        glFinish();
        glfwSwapBuffers(glContext);
    }

    @Override
    protected StaticMeshRenderer getStaticMeshRenderer() {
        return staticMeshRenderer;
    }
}