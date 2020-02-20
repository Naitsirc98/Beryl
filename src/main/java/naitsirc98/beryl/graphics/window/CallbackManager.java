package naitsirc98.beryl.graphics.window;

import naitsirc98.beryl.core.BerylApplication;
import naitsirc98.beryl.events.Event;
import naitsirc98.beryl.events.EventManager;
import naitsirc98.beryl.events.FramebufferResizeEvent;
import naitsirc98.beryl.events.input.*;
import naitsirc98.beryl.events.window.*;
import naitsirc98.beryl.input.Key;
import naitsirc98.beryl.input.Modifier;
import naitsirc98.beryl.input.MouseButton;
import naitsirc98.beryl.util.Destructor;
import org.lwjgl.glfw.*;
import org.lwjgl.system.Callback;

import java.util.ArrayList;
import java.util.List;

import static naitsirc98.beryl.events.EventManager.submit;
import static org.lwjgl.glfw.GLFW.*;

class CallbackManager {

    private final List<Callback> callbacks;
    private int keyRepeatCount;

    public CallbackManager() {
        callbacks = new ArrayList<>();
    }

    @Destructor
    void destroy() {
        callbacks.forEach(Callback::free);
        callbacks.clear();
    }

    CallbackManager setup(long handle) {

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

        glfwSetCursorEnterCallback(handle, add(onCursorEnterEvent()));

        glfwSetWindowIconifyCallback(handle, add(OnWindowIconified()));

        return this;
    }

    private GLFWWindowIconifyCallback OnWindowIconified() {
        return GLFWWindowIconifyCallback.create((handle, iconified) -> submit(iconified
                ? new WindowIconifiedEvent() : new WindowRestoredEvent()));
    }

    private GLFWCursorEnterCallback onCursorEnterEvent() {
        return GLFWCursorEnterCallback.create((handle, entered) -> submit(entered ? new MouseEnterEvent() : new MouseExitEvent()));
    }

    private GLFWCursorPosCallback onCursorPosEvent() {
        return GLFWCursorPosCallback.create((handle, x, y) -> submit(new MouseMovedEvent((float)x, (float)y)));
    }

    private GLFWScrollCallback onScrollEvent() {
        return GLFWScrollCallback.create((handle, xoffset, yoffset) -> submit(new MouseScrollEvent((float)xoffset, (float)yoffset)));
    }

    private GLFWMouseButtonCallback onMouseButtonEvent() {
        return GLFWMouseButtonCallback.create((handle, button, action, mods) -> submit(newMouseButtonEvent(button, action, mods)));
    }

    private GLFWCharCallback onUnicodeEvent() {
        return GLFWCharCallback.create((handle, codePoint) -> submit(new UnicodeInputEvent(codePoint)));
    }

    private GLFWKeyCallback onKeyEvent() {
        return GLFWKeyCallback.create((handle, key, scancode, action, mods) -> submit(newKeyEvent(key, scancode, action, mods)));
    }

    private GLFWWindowCloseCallback onWindowClose() {
        return GLFWWindowCloseCallback.create((handle) -> submit(new WindowClosedEvent()));
    }

    private GLFWWindowFocusCallback onWindowFocus() {
        return GLFWWindowFocusCallback.create((handle, focused) -> submit(new WindowFocusEvent(focused)));
    }

    private GLFWWindowMaximizeCallback onWindowMaximixe() {
        return GLFWWindowMaximizeCallback.create((handle, maximized) -> submit(maximized
                ? new WindowMaximizedEvent() : new WindowRestoredEvent()));
    }

    private GLFWFramebufferSizeCallback onFramebufferResize() {
        return GLFWFramebufferSizeCallback.create((handle, w, h) -> submit(new FramebufferResizeEvent(w, h)));
    }

    private GLFWWindowSizeCallback onWindowResize() {
        return GLFWWindowSizeCallback.create((handle, w, h) -> submit(new WindowResizedEvent(w, h)));
    }

    private GLFWWindowPosCallback onWindowPos() {
        return GLFWWindowPosCallback.create((handle, x, y) -> submit(new WindowMovedEvent(x, y)));
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
