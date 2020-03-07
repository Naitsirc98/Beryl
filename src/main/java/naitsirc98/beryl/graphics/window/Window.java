package naitsirc98.beryl.graphics.window;

import naitsirc98.beryl.images.Image;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.util.geometry.Rect;
import naitsirc98.beryl.util.geometry.Rectc;
import naitsirc98.beryl.util.geometry.Size;
import naitsirc98.beryl.util.geometry.Sizec;
import naitsirc98.beryl.util.handles.LongHandle;
import naitsirc98.beryl.util.types.Singleton;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.List;

import static naitsirc98.beryl.util.Asserts.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;

/**
 * A class to represent the window. This is basically a wrapper of a {@code GLFWWindow} handle
 */
public final class Window implements LongHandle {

    @Singleton
    private static Window instance;

    /**
     * Returns the application window
     *
     * @return the window
     */
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
    private final CallbackManager callbacks;

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

        callbacks = new CallbackManager().setup(handle);
    }

    /**
     * Returns the native handle of this window
     *
     * @return the {@code GLFWWindow} handle
     * */
    @Override
    public long handle() {
        return handle;
    }

    /**
     * Returns the title of this window
     *
     * @return the title
     */
    public String title() {
        return title;
    }

    /**
     * Sets the title of this window
     *
     * @param title the title
     * @return this window
     */
    public Window title(String title) {
        glfwSetWindowTitle(handle, title);
        return this;
    }

    /**
     * Returns the position x coordinate
     *
     * @return the x coordinate
     */
    public int x() {
        return position().x();
    }

    /**
     * Returns the position y coordinate
     *
     * @return the y coordinate
     */
    public int y() {
        return position().y();
    }

    /**
     * Returns the position of this window
     *
     * @return the position
     */
    public Vector2ic position() {
        try(MemoryStack stack = stackPush()) {
            IntBuffer x = stack.mallocInt(1);
            IntBuffer y = stack.mallocInt(1);
            glfwGetWindowPos(handle, x, y);
            return position.set(x.get(0), y.get(0));
        }
    }

    /**
     * Sets the position of this window
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return this window
     */
    public Window position(int x, int y) {
        glfwSetWindowPos(handle, x, y);
        return this;
    }

    /**
     * Center the window.
     *
     * @return this window
     */
    public Window center() {

        GLFWVidMode vmode = assertNonNull(glfwGetVideoMode(glfwGetPrimaryMonitor()));

        Sizec size = size();

        return position(centerX(vmode.width(), size.width()), centerY(vmode.height(), size.height()));
    }

    /**
     * Returns the width of this window
     *
     * @return the width
     */
    public int width() {
        return size().width();
    }

    /**
     * Returns the height of this window
     *
     * @return the height
     */
    public int height() {
        return size().height();
    }

    /**
     * Returns the aspect ratio of this window
     *
     * @return the aspect ratio
     */
    public float aspect() {
        return size().aspect();
    }

    /**
     * Returns the size of this window
     *
     * @return the size of the window
     */
    public Sizec size() {
        try(MemoryStack stack = stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            glfwGetWindowSize(handle, width, height);
            return size.set(width.get(0), height.get(0));
        }
    }

    /**
     * Sets the size of this window
     *
     * @param width  the width
     * @param height the height
     * @return this window
     */
    public Window size(int width, int height) {
        glfwSetWindowSize(handle, width, height);
        return this;
    }

    /**
     * Returns the framebuffer's size of this window
     *
     * @return the framebuffer's size
     */
    public Sizec framebufferSize() {
        try(MemoryStack stack = stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            glfwGetFramebufferSize(handle, width, height);
            return framebufferSize.set(width.get(0), height.get(0));
        }
    }

    /**
     * Retrieves the size, in screen coordinates, of each edge of the frame of this window.
     * This size includes the title bar, if the window has one.
     *
     * @return the frame size
     */
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

    /**
     * Returns the current cursor type
     *
     * @return the cursor type
     */
    public CursorType cursorType() {
        return CursorType.of(glfwGetInputMode(handle, GLFW_CURSOR));
    }

    /**
     * Sets the cursor type of this window
     *
     * @param cursorType the cursor type
     * @return this window
     */
    public Window cursorType(CursorType cursorType) {
        glfwSetInputMode(handle, GLFW_CURSOR, cursorType.glfwInputMode());
        return this;
    }

    /**
     * Sets the icon of this window. The image must be RGBA
     *
     * @param icon the image icon. Must be RGBA
     * @return this window
     * */
    public Window icon(Image icon) {
        return icons(Collections.singletonList(icon));
    }

    /**
     * Sets the icons of this window. The images must be RGBA
     *
     * @param icons a collection of icon images. Must be RGBA
     * @return this window
     * */
    public Window icons(List<Image> icons) {
        try(MemoryStack stack = stackPush()) {
            GLFWImage.Buffer images = GLFWImage.mallocStack(icons.size(), stack);

            for(int i = 0;i < icons.size();i++) {
                Image icon = icons.get(i);
                assertEquals(icon.pixelFormat(), PixelFormat.RGBA);

                GLFWImage image = images.get(i);

                image.width(icon.width());
                image.height(icon.height());
                image.pixels(icon.pixelsi());
            }

            glfwSetWindowIcon(handle, images);
        }
        return this;
    }

    /**
     * Request the focus for this window
     *
     * @return this window
     * */
    public Window focus() {
        glfwFocusWindow(handle);
        return this;
    }

    /**
     * Returns a native handle to the clipboard
     *
     * @return a handle to the clipboard
     */
    public long clipboardHandle() {
        return nglfwGetClipboardString(handle);
    }

    /**
     * Returns the contents of the clipboard as {@link String}
     *
     * @return the clipboard as {@link String}
     */
    public String clipboard() {
        return glfwGetClipboardString(handle);
    }

    /**
     * Sets the clipboard contents
     *
     * @param clipboard the clipboard contents as {@link String}
     * @return this window
     */
    public Window clipboard(String clipboard) {
        glfwSetClipboardString(handle, clipboard);
        return this;
    }


    /**
     * Sets the clipboard contents
     *
     * @param clipboard the clipboard contents as {@link ByteBuffer}
     * @return this window
     */
    public Window clipboard(ByteBuffer clipboard) {
        glfwSetClipboardString(handle, clipboard);
        return this;
    }

    /**
     * Returns the current display mode.
     *
     * @return the current display mode
     */
    public DisplayMode displayMode() {
        return visible() ? displayMode : DisplayMode.MINIMIZED;
    }

    /**
     * Sets the display mode of this window.
     *
     * @param displayMode the new display mode
     * @return this window
     */
    public Window displayMode(DisplayMode displayMode) {

        switch(displayMode) {
            case MINIMIZED:
                return hide();
            case FULLSCREEN:
                return fullscreen();
            case MAXIMIZED:
                return maximized();
            case WINDOWED:
                return windowed();
        }
        throw new IllegalArgumentException();
    }

    /**
     * Sets the window in fullscreen mode
     *
     * @return this window
     */
    public Window fullscreen() {
        restore();
        displayMode = DisplayMode.FULLSCREEN;
        long monitor = assertNotEquals(glfwGetPrimaryMonitor(), NULL);
        GLFWVidMode vmode = assertNonNull(glfwGetVideoMode(monitor));
        glfwWindowHint(GLFW_RED_BITS, vmode.redBits());
        glfwWindowHint(GLFW_GREEN_BITS, vmode.greenBits());
        glfwWindowHint(GLFW_BLUE_BITS, vmode.blueBits());
        return changeDisplayMode(monitor, vmode.refreshRate(), 0, 0, vmode.width(), vmode.height());
    }

    /**
     * Sets this window in maximized mode
     *
     * @return this window
     */
    public Window maximized() {
        restore();

        if(displayMode == DisplayMode.FULLSCREEN) {
            windowed();
        }

        displayMode = DisplayMode.MAXIMIZED;
        glfwMaximizeWindow(handle);

        return this;
    }

    /**
     * Sets this window in windowed mode
     *
     * @return this window
     */
    public Window windowed() {

        restore();

        displayMode = DisplayMode.WINDOWED;

        GLFWVidMode vmode = assertNonNull(glfwGetVideoMode(glfwGetPrimaryMonitor()));

        return changeDisplayMode(NULL, vmode.refreshRate(),
                centerX(vmode.width(), defaultWidth),
                centerY(vmode.height(), defaultHeight),
                defaultWidth,
                defaultHeight);
    }

    /**
     * Tells whether this window is currently visible or not
     *
     * @return {@code true} this window is visible, {@code false} otherwise
     *
     * */
    public boolean visible() {
        return glfwGetWindowAttrib(handle, GLFW_VISIBLE) == GLFW_TRUE;
    }

    /**
     * Shows this window if it was not visible
     *
     * @return this window
     */
    public Window show() {
        glfwShowWindow(handle);
        return this;
    }

    /**
     * Hides this window if it was visible
     *
     * @return this window
     */
    public Window hide() {
        restore();
        glfwHideWindow(handle);
        return this;
    }

    /**
     * Tells whether this window should stay open or not
     *
     * @return if window should be open
     */
    public boolean open() {
        return !shouldClose();
    }

    /**
     * Restores this window
     *
     * @return this window
     * */
    public Window restore() {
        glfwRestoreWindow(handle);
        return this;
    }

    /**
     * Returns if this window should be closed
     *
     * @return if window should close
     */
    public boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }

    /**
     * Indicates that this window should close
     *
     * @return this window
     */
    public Window close() {
        glfwSetWindowShouldClose(handle, true);
        return this;
    }

    /**
     * Destroys this window
     * */
    public void destroy() {
        callbacks.free();
        glfwDestroyWindow(handle);
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
        restore();
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
