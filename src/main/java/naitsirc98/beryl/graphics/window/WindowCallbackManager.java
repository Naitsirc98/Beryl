package naitsirc98.beryl.graphics.window;

import naitsirc98.beryl.core.BerylApplication;
import naitsirc98.beryl.events.Event;
import naitsirc98.beryl.events.EventManager;
import naitsirc98.beryl.events.input.*;
import naitsirc98.beryl.events.window.WindowClosedEvent;
import naitsirc98.beryl.events.window.WindowFocusEvent;
import naitsirc98.beryl.events.window.WindowMovedEvent;
import naitsirc98.beryl.events.window.WindowResizedEvent;
import naitsirc98.beryl.input.Key;
import naitsirc98.beryl.input.Modifier;
import naitsirc98.beryl.input.MouseButton;
import naitsirc98.beryl.util.Destructor;
import org.lwjgl.glfw.*;
import org.lwjgl.system.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static naitsirc98.beryl.events.EventManager.submit;
import static naitsirc98.beryl.util.Asserts.assertNonNull;
import static org.lwjgl.glfw.GLFW.*;

class WindowCallbackManager {

    private final long handle;
    private final Consumer<DisplayMode> onDisplayModeChange;
    private final List<Callback> callbacks;
    private int keyRepeatCount;

    public WindowCallbackManager(long handle, Consumer<DisplayMode> onDisplayModeChange) {
        this.handle = handle;
        this.onDisplayModeChange = assertNonNull(onDisplayModeChange);
        callbacks = new ArrayList<>();
    }

    @Destructor
    void destroy() {
        callbacks.forEach(Callback::free);
        callbacks.clear();
    }

    void setup() {

        glfwSetWindowPosCallback(handle, add(onWindowPos()));

        glfwSetWindowSizeCallback(handle, add(onWindowResize()));

        glfwSetFramebufferSizeCallback(handle, add(onFramebufferResize()));

        glfwSetWindowMaximizeCallback(handle, add(onWindowMaximixe()));

        glfwSetWindowFocusCallback(handle, add(onWindowFocus()));

        EventManager.addEventCallback(WindowClosedEvent.class, e -> BerylApplication.exit());
        glfwSetWindowCloseCallback(handle, add(onWindowClose()));

        glfwSetKeyCallback(handle, add(onKeyEvent()));

        glfwSetCharCallback(handle, add(onUnicodeEvent()));

        glfwSetMouseButtonCallback(handle, add(onMouseButtonEvent()));

        glfwSetScrollCallback(handle, add(onScrollEvent()));

        glfwSetCursorPosCallback(handle, add(onCursorPosEvent()));
    }

    private GLFWCursorPosCallback onCursorPosEvent() {
        return GLFWCursorPosCallback.create((whandle, x, y) -> submit(new MouseMovedEvent((float)x, (float)y)));
    }

    private GLFWScrollCallback onScrollEvent() {
        return GLFWScrollCallback.create((whandle, xoffset, yoffset) -> submit(new MouseScrollEvent((float)xoffset, (float)yoffset)));
    }

    private GLFWMouseButtonCallback onMouseButtonEvent() {
        return GLFWMouseButtonCallback.create((whandle, button, action, mods) -> submit(newMouseButtonEvent(button, action, mods)));
    }

    private GLFWCharCallback onUnicodeEvent() {
        return GLFWCharCallback.create((whandle, codePoint) -> submit(new UnicodeInputEvent(codePoint)));
    }

    private GLFWKeyCallback onKeyEvent() {
        return GLFWKeyCallback.create((whandle, key, scancode, action, mods) -> submit(newKeyEvent(key, scancode, action, mods)));
    }

    private GLFWWindowCloseCallback onWindowClose() {
        return GLFWWindowCloseCallback.create((whandle) -> submit(new WindowClosedEvent()));
    }

    private GLFWWindowFocusCallback onWindowFocus() {
        return GLFWWindowFocusCallback.create((whandle, focused) -> submit(new WindowFocusEvent(focused)));
    }

    private GLFWWindowMaximizeCallback onWindowMaximixe() {
        return GLFWWindowMaximizeCallback.create((whandle, maximized) -> onDisplayModeChange.accept(maximized
                ? DisplayMode.MAXIMIZED
                : DisplayMode.WINDOWED));
    }

    private GLFWFramebufferSizeCallback onFramebufferResize() {
        return GLFWFramebufferSizeCallback.create((whandle, w, h) -> {
            // getGraphics().getContext().getGraphicsPipeline().setViewport(0, 0, w, h);
        });
    }

    private GLFWWindowSizeCallback onWindowResize() {
        return GLFWWindowSizeCallback.create((whandle, w, h) -> submit(new WindowResizedEvent(w, h)));
    }

    private GLFWWindowPosCallback onWindowPos() {
        return GLFWWindowPosCallback.create((whandle, x, y) -> submit(new WindowMovedEvent(x, y)));
    }

    private <T extends Callback> T add(T callback) {
        callbacks.add(callback);
        return callback;
    }

    private Event newMouseButtonEvent(int button, int action, int mods) {

        MouseButtonEvent event = null;

        switch (action) {

            case GLFW_PRESS:
                keyRepeatCount = 0;
                event = new MouseButtonPressedEvent(MouseButton.asMouseButton(button), Modifier.asModifierMask(mods));
                break;
            case GLFW_RELEASE:
                event = new MouseButtonReleasedEvent(MouseButton.asMouseButton(button), Modifier.asModifierMask(mods));
                break;
        }

        return event;
    }

    private Event newKeyEvent(int key, int scancode, int action, int mods) {

        KeyEvent event = null;

        switch(action) {
            case GLFW_PRESS:
                keyRepeatCount = 0;
                event = new KeyPressedEvent(Key.asKey(key), Modifier.asModifierMask(mods));
                break;
            case GLFW_REPEAT:
                event = new KeyRepeatEvent(Key.asKey(key), Modifier.asModifierMask(mods), ++keyRepeatCount);
                break;
            case GLFW_RELEASE:
                event = new KeyReleasedEvent(Key.asKey(key), Modifier.asModifierMask(mods));
                break;
        }

        return event;
    }


}
