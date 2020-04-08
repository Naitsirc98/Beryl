package naitsirc98.beryl.graphics.opengl;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.graphics.GraphicsContext;
import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.opengl.rendering.Rendering;
import naitsirc98.beryl.graphics.rendering.RenderingPath;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.util.handles.LongHandle;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import java.util.HashMap;
import java.util.Map;

import static naitsirc98.beryl.graphics.opengl.GLDebugMessenger.newGLDebugMessenger;
import static naitsirc98.beryl.graphics.rendering.RenderingPaths.RPATH_PHONG;
import static naitsirc98.beryl.util.types.TypeUtils.newInstance;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;

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
        glfwSwapInterval(INITIAL_VSYNC ? 1 : 0);
        // glEnable(GL_MULTISAMPLE);
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

        // renderingPaths.put(RPATH_SIMPLE3D, newInstance(GLSimpleRenderingPath.class));
        // renderingPaths.put(RPATH_PHONG, newInstance(GLPhongRenderingPath.class));
        // renderingPaths.put(100, newInstance(GLCascadedShadowMaps.class));
        renderingPaths.put(RPATH_PHONG, new Rendering());
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
    public void release() {

        graphicsFactory.release();

        if(OPENGL_DEBUG_MESSAGES_ENABLED) {
            debugMessenger.release();
        }

        GL.destroy();
    }
}
