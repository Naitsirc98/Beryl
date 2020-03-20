package naitsirc98.beryl.graphics.opengl;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.graphics.GraphicsContext;
import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.opengl.rendering.GLPhongRenderingPath;
import naitsirc98.beryl.graphics.opengl.rendering.GLSimpleRenderingPath;
import naitsirc98.beryl.graphics.rendering.RenderingPath;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.util.handles.LongHandle;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import java.util.HashMap;
import java.util.Map;

import static naitsirc98.beryl.graphics.opengl.GLDebugMessenger.newGLDebugMessenger;
import static naitsirc98.beryl.graphics.rendering.RenderingPaths.RPATH_PHONG;
import static naitsirc98.beryl.graphics.rendering.RenderingPaths.RPATH_SIMPLE3D;
import static naitsirc98.beryl.util.types.TypeUtils.newInstance;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;

public class GLContext implements GraphicsContext, LongHandle {

    public static final boolean OPENGL_DEBUG_MESSAGES_ENABLED = BerylConfiguration.OPENGL_ENABLE_DEBUG_MESSAGES.get(Beryl.DEBUG);

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
        glfwSwapInterval(0);
    }

    @Override
    public boolean vsync() {
        return vsync;
    }

    @Override
    public void vsync(boolean vsync) {
        if(this.vsync != vsync) {
            this.vsync = vsync;
            glfwSwapInterval(vsync ? 1 : 0);
        }
    }

    @Override
    public GLMapper mapper() {
        return mapper;
    }

    @Override
    public Map<Integer, RenderingPath> renderingPaths() {

        Map<Integer, RenderingPath> renderingPaths = new HashMap<>();

        renderingPaths.put(RPATH_SIMPLE3D, newInstance(GLSimpleRenderingPath.class));
        renderingPaths.put(RPATH_PHONG, newInstance(GLPhongRenderingPath.class));
        // ...

        return renderingPaths;
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
    public void free() {

        if(OPENGL_DEBUG_MESSAGES_ENABLED) {
            debugMessenger.free();
        }

        GL.destroy();
    }
}
