package naitsirc98.beryl.graphics.window;

import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.events.EventManager;
import naitsirc98.beryl.events.window.WindowMovedEvent;
import naitsirc98.beryl.events.window.WindowResizedEvent;
import naitsirc98.beryl.graphics.GraphicsAPI;
import naitsirc98.beryl.util.Size;
import naitsirc98.beryl.util.Sizec;
import org.lwjgl.glfw.*;
import org.lwjgl.system.Platform;

import static naitsirc98.beryl.graphics.GraphicsAPI.OPENGL;
import static naitsirc98.beryl.graphics.window.DisplayMode.FULLSCREEN;
import static naitsirc98.beryl.graphics.window.DisplayMode.WINDOWED;
import static naitsirc98.beryl.util.Asserts.assertNonNull;
import static naitsirc98.beryl.util.Asserts.assertNotEquals;
import static naitsirc98.beryl.util.LongHandle.NULL;
import static naitsirc98.beryl.util.TypeUtils.initSingleton;
import static org.lwjgl.glfw.GLFW.*;

public final class WindowFactory {

    private static final int DEFAULT_WIDTH = 1280;
    private static final int DEFAULT_HEIGHT = 720;

    private WindowFactory() {}

    public Window newWindow() {

        setWindowHints();

        String title = BerylConfiguration.WINDOW_TITLE.get("");
        DisplayMode displayMode = BerylConfiguration.WINDOW_DISPLAY_MODE.get(WINDOWED);
        Sizec defaultSize = BerylConfiguration.WINDOW_SIZE.get(new Size(DEFAULT_WIDTH, DEFAULT_HEIGHT));

        Window window = new Window(createGLFWHandle(title, displayMode, defaultSize), title, displayMode, defaultSize);

        initSingleton(Window.class, window);

        return window;
    }

    private void setWindowHints() {
        glfwWindowHint(GLFW_VISIBLE, asGLFWBoolean(BerylConfiguration.WINDOW_VISIBLE.get(true)));
        glfwWindowHint(GLFW_RESIZABLE, asGLFWBoolean(BerylConfiguration.WINDOW_RESIZABLE.get(true)));
        glfwWindowHint(GLFW_FOCUS_ON_SHOW, asGLFWBoolean(BerylConfiguration.WINDOW_FOCUS_ON_SHOW.get(true)));

        setGraphicsAPIDependentWindowHints();
    }

    private void setGraphicsAPIDependentWindowHints() {

        switch(GraphicsAPI.get()) {
            case VULKAN:
                setVulkanWindowHints();
                break;
            case OPENGL:
                setOpenGLWindowHints();
                break;
        }
    }

    private void setVulkanWindowHints() {

        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);

    }

    private void setOpenGLWindowHints() {

        glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API);

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, OPENGL.versionMajor());
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, OPENGL.versionMinor());

        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        if(Platform.get() == Platform.MACOSX) {
            glfwWindowHint(GLFW_OPENGL_COMPAT_PROFILE, GLFW_TRUE);
        }
    }

    private int asGLFWBoolean(boolean value) {
        return value ? GLFW_TRUE : GLFW_FALSE;
    }

    private long createGLFWHandle(String title, DisplayMode displayMode, Sizec size) {

        int width = size.width();
        int height = size.height();

        long monitor = glfwGetPrimaryMonitor();

        if(displayMode != WINDOWED) {
            GLFWVidMode vidMode = assertNonNull(glfwGetVideoMode(monitor));
            width = vidMode.width();
            height = vidMode.height();
        }

        if(displayMode != FULLSCREEN) {
            monitor = NULL;
        }

        return assertNotEquals(glfwCreateWindow(width, height, assertNonNull(title), monitor, NULL), NULL);
    }

}
