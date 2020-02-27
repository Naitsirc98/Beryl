package naitsirc98.beryl.graphics.opengl;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.graphics.GraphicsContext;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexData;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexDataBuilder;
import naitsirc98.beryl.graphics.rendering.Renderer;
import naitsirc98.beryl.graphics.opengl.rendering.GLRenderer;
import naitsirc98.beryl.graphics.rendering.RenderingPath;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import naitsirc98.beryl.util.LongHandle;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import java.util.Map;

import static naitsirc98.beryl.graphics.opengl.GLDebugMessenger.newGLDebugMessenger;
import static naitsirc98.beryl.util.TypeUtils.newInstance;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;

public class GLContext implements GraphicsContext, LongHandle {

    public static final boolean OPENGL_DEBUG_MESSAGES_ENABLED = BerylConfiguration.OPENGL_ENABLE_DEBUG_MESSAGES.get(Beryl.DEBUG);

    private long glContext;
    private GLDebugMessenger debugMessenger;
    private GLCapabilities capabilities;
    private GLRenderer renderer;

    private GLContext() {

    }

    @Override
    public void init() {
        glContext = Window.get().handle();
        makeCurrent();
        capabilities = GL.createCapabilities();
        debugMessenger = newGLDebugMessenger();
        renderer = newInstance(GLRenderer.class, glContext);
    }

    @Override
    public Renderer renderer() {
        return renderer;
    }

    @Override
    public Map<Integer, RenderingPath> renderingPaths() {
        return null;
    }

    @Override
    public GLVertexDataBuilder newVertexDataBuilder(VertexLayout layout) {
        return new GLVertexDataBuilder(layout);
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

        renderer.free();

        if(OPENGL_DEBUG_MESSAGES_ENABLED) {
            debugMessenger.free();
        }

        GL.destroy();
    }
}
