package naitsirc98.beryl.graphics.opengl;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.graphics.GraphicsContext;
import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.graphics.window.WindowFactory;
import naitsirc98.beryl.util.handles.LongHandle;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import static naitsirc98.beryl.graphics.opengl.GLDebugMessenger.newGLDebugMessenger;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER_SRGB;

public class GLContext implements GraphicsContext, LongHandle {

    public static final boolean OPENGL_DEBUG_MESSAGES_ENABLED = BerylConfiguration.OPENGL_ENABLE_DEBUG_MESSAGES.get(Beryl.DEBUG);

    private static final boolean INITIAL_VSYNC = BerylConfiguration.VSYNC.get(false);

    private long glContext;
    private GLDebugMessenger debugMessenger;
    private GLCapabilities capabilities;
    private GLGraphicsFactory graphicsFactory;
    private GLMapper mapper;
    private boolean vsync;

    private GLContext() {

    }

    @Override
    public void init() {

        glContext = Window.get().handle();
        makeCurrent();

        capabilities = GL.createCapabilities();
        debugMessenger = newGLDebugMessenger();
        graphicsFactory = new GLGraphicsFactory();
        mapper = new GLMapper();

        vsync(INITIAL_VSYNC);

        if(WindowFactory.MULTISAMPLE_ENABLE) {
            glEnable(GL_MULTISAMPLE);
        } else {
            glDisable(GL_MULTISAMPLE);
        }

        glEnable(GL_FRAMEBUFFER_SRGB);
    }

    @Override
    public boolean vsync() {
        return vsync;
    }

    @Override
    public void vsync(boolean vsync) {
        glfwSwapInterval(vsync ? 1 : 0);
        this.vsync = vsync;
    }

    @Override
    public GLMapper mapper() {
        return mapper;
    }

    @Override
    public GraphicsFactory graphicsFactory() {
        return graphicsFactory;
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
    public void release() {

        graphicsFactory.release();

        if(OPENGL_DEBUG_MESSAGES_ENABLED) {
            debugMessenger.release();
        }

        GL.destroy();
    }
}
