package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.graphics.rendering.Renderer;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.util.Sizec;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

public final class GLRenderer implements Renderer {

    private static final int DEFAULT_FRAMEBUFFER = 0;

    private final long glContext;

    private GLRenderer(Long glContext) {
        this.glContext = glContext;
        glfwSwapInterval(0);
    }

    @Override
    public void begin(MemoryStack stack) {
        Sizec framebufferSize = Window.get().framebufferSize();
        glViewport(0, 0, framebufferSize.width(), framebufferSize.height());
    }

    @Override
    public void end(MemoryStack stack) {
        glBindFramebuffer(GL_FRAMEBUFFER, DEFAULT_FRAMEBUFFER);
        glfwSwapBuffers(glContext);
    }

    @Override
    public void free() {

    }
}
