package naitsirc98.beryl.graphics.opengl;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.logging.Log.Level;
import naitsirc98.beryl.resources.Resource;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLDebugMessageCallback;

import static naitsirc98.beryl.graphics.opengl.GLContext.OPENGL_DEBUG_MESSAGES_ENABLED;
import static org.lwjgl.opengl.GL43.GL_DEBUG_OUTPUT;
import static org.lwjgl.opengl.GL43.glEnable;
import static org.lwjgl.opengl.GL43C.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GLDebugMessenger implements Resource {

    public static String getGLErrorName(int errorCode) {
        switch (errorCode) {
            case GL_INVALID_ENUM:
                return "GL_INVALID_ENUM";
            case GL_INVALID_VALUE:
                return "GL_INVALID_VALUE";
            case GL_INVALID_OPERATION:
                return "GL_INVALID_OPERATION";
            case GL_STACK_OVERFLOW:
                return "GL_STACK_OVERFLOW";
            case GL_STACK_UNDERFLOW:
                return "GL_STACK_UNDERFLOW";
            case GL_OUT_OF_MEMORY:
                return "GL_OUT_OF_MEMORY";
            case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                return "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT";
            case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                return "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT";
            case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
                return "GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER";
            case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
                return "GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER";
            case GL_FRAMEBUFFER_UNSUPPORTED:
                return "GL_FRAMEBUFFER_UNSUPPORTED";
            case GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE:
                return "GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE";
            case GL_FRAMEBUFFER_UNDEFINED:
                return "GL_FRAMEBUFFER_UNDEFINED";
        }
        return "GL_UNKNOWN_ERROR";
    }

    static GLDebugMessenger newGLDebugMessenger() {
        return OPENGL_DEBUG_MESSAGES_ENABLED ? new GLDebugMessenger() : null;
    }

    private final GLDebugMessageCallback debugCallback;

    private GLDebugMessenger() {
        glEnable(GL_DEBUG_OUTPUT);
        debugCallback = GLDebugMessageCallback.create(this::callback);
        glDebugMessageCallback(debugCallback, NULL);
        glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);
    }

    @Override
    public void release() {
        debugCallback.free();
    }

    private void callback(int source, int type, int id, int severity, int length, long message, long userParam) {

        Log.log(asLogLevel(severity), String.format("[OPENGL][%s|%s|%s]: %s",
                hex(id),
                sourceAsString(source),
                typeAsString(type),
                GLDebugMessageCallback.getMessage(length, message)));
    }

    private String hex(int id) {
        return String.format("0x%X", id);
    }

    private static String sourceAsString(int source) {
        switch (source) {
            case GL_DEBUG_SOURCE_API:
                return "API";
            case GL_DEBUG_SOURCE_WINDOW_SYSTEM:
                return "WINDOW SYSTEM";
            case GL_DEBUG_SOURCE_SHADER_COMPILER:
                return "SHADER COMPILER";
            case GL_DEBUG_SOURCE_THIRD_PARTY:
                return "THIRD PARTY";
            case GL_DEBUG_SOURCE_APPLICATION:
                return "APPLICATION";
            case GL_DEBUG_SOURCE_OTHER:
                return "OTHER";
            default:
                return "UNKNOWN";
        }
    }

    private static String typeAsString(int type) {
        switch (type) {
            case GL_DEBUG_TYPE_ERROR:
                return "ERROR";
            case GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR:
                return "DEPRECATED BEHAVIOR";
            case GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR:
                return "UNDEFINED BEHAVIOR";
            case GL_DEBUG_TYPE_PORTABILITY:
                return "PORTABILITY";
            case GL_DEBUG_TYPE_PERFORMANCE:
                return "PERFORMANCE";
            case GL_DEBUG_TYPE_OTHER:
                return "OTHER";
            case GL_DEBUG_TYPE_MARKER:
                return "MARKER";
            default:
                return "UNKNOWN";
        }
    }

    private Level asLogLevel(int severity) {
        switch (severity) {
            case GL_DEBUG_SEVERITY_HIGH:
                return Level.ERROR;
            case GL_DEBUG_SEVERITY_MEDIUM:
            case GL_DEBUG_SEVERITY_LOW:
                return Level.WARNING;
            default:
                return Level.TRACE;
        }
    }

    @Override
    public boolean released() {
        return false; // Not used
    }
}
