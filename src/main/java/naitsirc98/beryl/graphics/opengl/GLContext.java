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

    private final long glContextHandle;
    private final GLDebugMessenger debugMessenger;
    private final GLCapabilities capabilities;

    public GLContext() {
        glContextHandle = Window.get().handle();
        makeCurrent();
        capabilities = GL.createCapabilities();
        debugMessenger = newGLDebugMessenger();
    }

    @Override
    public long handle() {
        return glContextHandle;
    }

    private void makeCurrent() {
        glfwMakeContextCurrent(glContextHandle);
    }

    @Override
    public void free() {

        if(OPENGL_DEBUG_MESSAGES_ENABLED) {
            debugMessenger.free();
        }

        GL.destroy();
    }
}
