package naitsirc98.beryl.graphics.opengl;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.logging.Log.Level;
import naitsirc98.beryl.util.types.Destructor;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.system.NativeResource;

import static naitsirc98.beryl.graphics.opengl.GLContext.OPENGL_DEBUG_MESSAGES_ENABLED;
import static org.lwjgl.opengl.GL43.GL_DEBUG_OUTPUT;
import static org.lwjgl.opengl.GL43.glEnable;
import static org.lwjgl.opengl.GL43C.*;
import static org.lwjgl.system.MemoryUtil.NULL;

@Destructor
public class GLDebugMessenger implements NativeResource {

    static GLDebugMessenger newGLDebugMessenger() {
        return OPENGL_DEBUG_MESSAGES_ENABLED ? new GLDebugMessenger() : null;
    }

    private final GLDebugMessageCallback debugCallback;

    private GLDebugMessenger() {
        glEnable(GL_DEBUG_OUTPUT);
        debugCallback = GLDebugMessageCallback.create(this::callback);
        glDebugMessageCallback(debugCallback, NULL);
    }

    @Override
    public void free() {
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
        switch(severity) {
            case GL_DEBUG_SEVERITY_HIGH:
                return Level.ERROR;
            case GL_DEBUG_SEVERITY_MEDIUM:
            case GL_DEBUG_SEVERITY_LOW:
                return Level.WARNING;
            default:
                return Level.TRACE;
        }
    }
}
