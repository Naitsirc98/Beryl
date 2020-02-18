package naitsirc98.beryl.graphics.window;

import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.util.LongHandle;
import naitsirc98.beryl.util.Size;
import naitsirc98.beryl.util.Sizec;
import org.lwjgl.glfw.GLFWVidMode;

import static naitsirc98.beryl.graphics.window.DisplayMode.FULLSCREEN;
import static naitsirc98.beryl.graphics.window.DisplayMode.WINDOWED;
import static naitsirc98.beryl.util.Asserts.assertNonNull;
import static naitsirc98.beryl.util.Asserts.assertNotEquals;
import static naitsirc98.beryl.util.LongHandle.NULL;
import static naitsirc98.beryl.util.TypeUtils.initSingleton;
import static org.lwjgl.glfw.GLFW.*;

public abstract class WindowFactory {

    private static final int DEFAULT_WIDTH = 1280;
    private static final int DEFAULT_HEIGHT = 720;

    protected WindowFactory() {

    }

    public Window newWindow() {

        setWindowHints();

        String title = BerylConfiguration.WINDOW_TITLE.get("");
        DisplayMode displayMode = BerylConfiguration.WINDOW_DISPLAY_MODE.get(WINDOWED);
        Sizec defaultSize = BerylConfiguration.WINDOW_SIZE.get(new Size(DEFAULT_WIDTH, DEFAULT_HEIGHT));

        Window window = new Window(createGLFWHandle(title, displayMode, defaultSize), title, displayMode, defaultSize);

        initSingleton(Window.class, window);

        return window;
    }

    protected abstract void setWindowHints();

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
