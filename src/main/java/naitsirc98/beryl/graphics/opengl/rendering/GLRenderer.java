package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.graphics.rendering.Renderer;
import naitsirc98.beryl.graphics.window.DisplayMode;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.util.geometry.Sizec;

import static naitsirc98.beryl.graphics.Graphics.opengl;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

public final class GLRenderer implements Renderer {

    private static final int DEFAULT_FRAMEBUFFER = 0;

    private final long glContext;

    private GLRenderer() {
        glContext = opengl().handle();
    }

    @Override
    public boolean begin() {
        Sizec framebufferSize = Window.get().framebufferSize();
        glViewport(0, 0, framebufferSize.width(), framebufferSize.height());
        glBindFramebuffer(GL_FRAMEBUFFER, DEFAULT_FRAMEBUFFER);
        return true;
    }

    @Override
    public void end() {
        glFinish();
        glfwSwapBuffers(glContext);
    }

    @Override
    public void release() {

    }
}
