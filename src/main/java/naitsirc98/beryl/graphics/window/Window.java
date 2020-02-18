package naitsirc98.beryl.graphics.window;

import naitsirc98.beryl.util.*;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static naitsirc98.beryl.util.Asserts.assertNonNull;
import static naitsirc98.beryl.util.Asserts.assertNotEquals;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public final class Window implements LongHandle {

    @Singleton
    private static Window instance;

    public static Window get() {
        return instance;
    }

    private final long handle;
    private final int defaultWidth;
    private final int defaultHeight;

    private String title;
    private Vector2i position;
    private Size size;
    private Size framebufferSize;
    private Rect rect;
    private DisplayMode displayMode;

    {
        if(instance != null) {
            throw new ExceptionInInitializerError("Window has been already created");
        }
    }

    Window(long handle, String title, DisplayMode displayMode, Sizec defaultSize) {

        this.handle = handle;
        this.title = title;
        this.displayMode = displayMode;
        this.defaultWidth = Math.max(defaultSize.width(), 1);
        this.defaultHeight = Math.max(defaultSize.height(), 1);

        position = new Vector2i();
        this.size = new Size();
        framebufferSize = new Size();
        rect = new Rect();
    }

    @Override
    public long handle() {
        return handle;
    }

    public String title() {
        return title;
    }

    public Window title(String title) {
        glfwSetWindowTitle(handle, title);
        return this;
    }

    public int x() {
        return position().x();
    }

    public int y() {
        return position().y();
    }

    public Vector2ic position() {
        try(MemoryStack stack = stackPush()) {
            IntBuffer x = stack.mallocInt(1);
            IntBuffer y = stack.mallocInt(1);
            glfwGetWindowPos(handle, x, y);
            return position.set(x.get(0), y.get(0));
        }
    }

    public Window position(int x, int y) {
        glfwSetWindowPos(handle, x, y);
        return this;
    }

    public Window center() {

        GLFWVidMode vmode = assertNonNull(glfwGetVideoMode(glfwGetPrimaryMonitor()));

        Sizec size = size();

        return position(centerX(vmode.width(), size.width()), centerY(vmode.height(), size.height()));
    }

    public int width() {
        return size().width();
    }

    public int height() {
        return size().height();
    }

    public float aspect() {
        return size().aspect();
    }

    public Sizec size() {
        try(MemoryStack stack = stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            glfwGetWindowSize(handle, width, height);
            return size.set(width.get(0), height.get(0));
        }
    }

    public Window size(int width, int height) {
        glfwSetWindowSize(handle, width, height);
        return this;
    }

    public Sizec framebufferSize() {
        try(MemoryStack stack = stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            glfwGetFramebufferSize(handle, width, height);
            return framebufferSize.set(width.get(0), height.get(0));
        }
    }

    public Rectc rect() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer left = stack.ints(0);
            IntBuffer top = stack.ints(0);
            IntBuffer right = stack.ints(0);
            IntBuffer bottom =stack.ints(0);
            glfwGetWindowFrameSize(handle, left, top, right, bottom);
            return rect.set(left.get(0), right.get(0), top.get(0), bottom.get(0));
        }
    }

    public CursorType cursorType() {
        return CursorType.of(glfwGetInputMode(handle, GLFW_CURSOR));
    }

    public Window cursorType(CursorType cursorType) {
        glfwSetInputMode(handle, GLFW_CURSOR, cursorType.glfwInputMode());
        return this;
    }

    public String clipboard() {
        return glfwGetClipboardString(handle);
    }

    public Window clipboard(String clipboard) {
        glfwSetClipboardString(handle, clipboard);
        return this;
    }

    public Window clipboard(ByteBuffer clipboard) {
        glfwSetClipboardString(handle, clipboard);
        return this;
    }

    public DisplayMode displayMode() {
        return displayMode;
    }

    public Window displayMode(DisplayMode displayMode) {
        switch(displayMode) {
            case FULLSCREEN:
                return fullscreen();
            case MAXIMIZED:
                return maximized();
            case WINDOWED:
                return windowed();
        }
        throw new IllegalArgumentException();
    }

    public Window fullscreen() {
        displayMode = DisplayMode.FULLSCREEN;
        long monitor = assertNotEquals(glfwGetPrimaryMonitor(), NULL);
        GLFWVidMode vmode = assertNonNull(glfwGetVideoMode(monitor));
        glfwWindowHint(GLFW_RED_BITS, vmode.redBits());
        glfwWindowHint(GLFW_GREEN_BITS, vmode.greenBits());
        glfwWindowHint(GLFW_BLUE_BITS, vmode.blueBits());
        return changeDisplayMode(monitor, vmode.refreshRate(), 0, 0, vmode.width(), vmode.height());
    }

    public Window maximized() {
        windowed();

        displayMode = DisplayMode.MAXIMIZED;

        GLFWVidMode vmode = assertNonNull(glfwGetVideoMode(glfwGetPrimaryMonitor()));

        Rectc rect = rect();

        return changeDisplayMode(NULL, vmode.refreshRate(), rect.left(), rect.top(),
                vmode.width() - rect.right(), vmode.height() - rect.bottom());
    }

    public Window windowed() {

        displayMode = DisplayMode.WINDOWED;

        GLFWVidMode vmode = assertNonNull(glfwGetVideoMode(glfwGetPrimaryMonitor()));

        return changeDisplayMode(NULL, vmode.refreshRate(),
                centerX(vmode.width(), defaultWidth),
                centerY(vmode.height(), defaultHeight),
                defaultWidth,
                defaultHeight);
    }

    public Window show() {
        glfwShowWindow(handle);
        return this;
    }

    public Window hide() {
        glfwHideWindow(handle);
        return this;
    }

    public boolean open() {
        return !shouldClose();
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }

    public Window shouldClose(boolean shouldClose) {
        glfwSetWindowShouldClose(handle, shouldClose);
        return this;
    }

    private int centerY(int monitorHeight, int windowHeight) {
        return centerPos(monitorHeight, windowHeight);
    }

    private int centerX(int monitorWidth, int windowWidth) {
        return centerPos(monitorWidth, windowWidth);
    }

    private int centerPos(int monitorSize, int windowSize) {
        return (monitorSize - windowSize) / 2;
    }

    private Window changeDisplayMode(long monitor, int refreshRate, int x, int y, int width, int height) {
        glfwSetWindowMonitor(
                handle,
                monitor,
                x,
                y,
                width,
                height,
                refreshRate);
        return this;
    }

}
