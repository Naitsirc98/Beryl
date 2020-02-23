package naitsirc98.beryl.graphics.opengl;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.graphics.GraphicsContext;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.util.LongHandle;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import static naitsirc98.beryl.graphics.opengl.GLDebugMessenger.newGLDebugMessenger;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;

public class GLContext implements GraphicsContext, LongHandle {

    public static final boolean OPENGL_DEBUG_MESSAGES_ENABLED = BerylConfiguration.OPENGL_ENABLE_DEBUG_MESSAGES.get(Beryl.DEBUG);

    private long glContext;
    private GLDebugMessenger debugMessenger;
    private GLCapabilities capabilities;

    private GLContext() {

    }

    @Override
    public void init() {
        glContext = Window.get().handle();
        makeCurrent();
        capabilities = GL.createCapabilities();
        debugMessenger = newGLDebugMessenger();
    }

    @Override
    public long handle() {
        return glContext;
    }

    public GLCapabilities capabilities() {
        return capabilities;
    }

    private void makeCurrent() {
        glfwMakeContextCurrent(glContext);
    }

    @Override
    public void free() {

        if(OPENGL_DEBUG_MESSAGES_ENABLED) {
            debugMessenger.free();
        }

        GL.destroy();
    }
}
