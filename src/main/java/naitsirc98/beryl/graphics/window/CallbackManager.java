package naitsirc98.beryl.graphics.window;

import naitsirc98.beryl.core.BerylApplication;
import naitsirc98.beryl.events.Event;
import naitsirc98.beryl.events.EventManager;
import naitsirc98.beryl.events.input.keyboard.KeyEvent;
import naitsirc98.beryl.events.input.keyboard.KeyPressedEvent;
import naitsirc98.beryl.events.input.keyboard.KeyRepeatEvent;
import naitsirc98.beryl.events.input.keyboard.KeyTypedEvent;
import naitsirc98.beryl.events.input.mouse.*;
import naitsirc98.beryl.events.window.*;
import naitsirc98.beryl.input.Key;
import naitsirc98.beryl.input.KeyModifier;
import naitsirc98.beryl.input.MouseButton;
import naitsirc98.beryl.resources.Resource;
import org.lwjgl.glfw.*;
import org.lwjgl.system.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static naitsirc98.beryl.events.EventManager.triggerEvent;
import static naitsirc98.beryl.events.EventManager.triggerEventNow;
import static org.lwjgl.glfw.GLFW.*;

class CallbackManager implements Resource {

    private final List<Callback> callbacks;
    private int keyRepeatCount;

    public CallbackManager() {
        callbacks = new ArrayList<>();
    }

    @Override
    public void release() {
        callbacks.forEach(Callback::free);
        callbacks.clear();
    }

    CallbackManager setup(Window window) {

        final long handle = window.handle();

        glfwSetWindowPosCallback(handle, add(onWindowPos(window)));

        glfwSetWindowSizeCallback(handle, add(onWindowResize(window)));

        glfwSetFramebufferSizeCallback(handle, add(onFramebufferResize(window)));

        glfwSetWindowMaximizeCallback(handle, add(onWindowMaximize(window)));

        glfwSetWindowFocusCallback(handle, add(onWindowFocus(window)));

        EventManager.addEventCallback(WindowClosedEvent.class, e -> BerylApplication.exit());
        glfwSetWindowCloseCallback(handle, add(onWindowClose(window)));

        glfwSetKeyCallback(handle, add(onKeyEvent()));

        glfwSetCharCallback(handle, add(onUnicodeEvent()));

        glfwSetMouseButtonCallback(handle, add(onMouseButtonEvent()));

        glfwSetScrollCallback(handle, add(onScrollEvent()));

        glfwSetCursorPosCallback(handle, add(onCursorPosEvent()));

        glfwSetCursorEnterCallback(handle, add(onCursorEnterEvent()));

        glfwSetWindowIconifyCallback(handle, add(OnWindowIconified(window)));

        return this;
    }

    private GLFWWindowIconifyCallback OnWindowIconified(Window window) {
        return GLFWWindowIconifyCallback.create((handle, iconified) -> {
            triggerEvent(iconified ? new WindowIconifiedEvent() : new WindowRestoredEvent());
            window.update();
        });
    }

    private GLFWCursorEnterCallback onCursorEnterEvent() {
        return GLFWCursorEnterCallback.create((handle, entered) -> triggerEventNow(entered ? new MouseEnterEvent() : new MouseExitEvent()));
    }

    private GLFWCursorPosCallback onCursorPosEvent() {
        return GLFWCursorPosCallback.create((handle, x, y) -> triggerEventNow(new MouseMovedEvent((float)x, (float)y)));
    }

    private GLFWScrollCallback onScrollEvent() {
        return GLFWScrollCallback.create((handle, xoffset, yoffset) -> triggerEvent(new MouseScrollEvent((float)xoffset, (float)yoffset)));
    }

    private GLFWMouseButtonCallback onMouseButtonEvent() {
        return GLFWMouseButtonCallback.create((handle, button, action, mods) -> triggerEvent(newMouseButtonEvent(button, action, mods)));
    }

    private GLFWCharCallback onUnicodeEvent() {
        return GLFWCharCallback.create((handle, codePoint) -> triggerEvent(new UnicodeInputEvent(codePoint)));
    }

    private GLFWKeyCallback onKeyEvent() {
        return GLFWKeyCallback.create((handle, key, scancode, action, mods) -> triggerEvent(newKeyEvent(key, scancode, action, mods)));
    }

    private GLFWWindowCloseCallback onWindowClose(Window window) {
        return GLFWWindowCloseCallback.create((handle) -> {
            triggerEventNow(new WindowClosedEvent());
            window.update();
        });
    }

    private GLFWWindowFocusCallback onWindowFocus(Window window) {
        return GLFWWindowFocusCallback.create((handle, focused) -> {
            triggerEventNow(new WindowFocusEvent(focused));
            window.update();
        });
    }

    private GLFWWindowMaximizeCallback onWindowMaximize(Window window) {
        return GLFWWindowMaximizeCallback.create((handle, maximized) -> {
            triggerEventNow(maximized ? new WindowMaximizedEvent() : new WindowRestoredEvent());
            window.update();
        });
    }

    private GLFWFramebufferSizeCallback onFramebufferResize(Window window) {
        return GLFWFramebufferSizeCallback.create((handle, w, h) -> {
            triggerEventNow(new FramebufferResizeEvent(w, h));
            window.update();
        });
    }

    private GLFWWindowSizeCallback onWindowResize(Window window) {
        return GLFWWindowSizeCallback.create((handle, w, h) -> {
            triggerEventNow(new WindowResizedEvent(w, h));
            window.update();
        });
    }

    private GLFWWindowPosCallback onWindowPos(Window window) {
        return GLFWWindowPosCallback.create((handle, x, y) -> {
            triggerEventNow(new WindowMovedEvent(x, y));
            window.update();
        });
    }

    private <T extends Callback> T add(T callback) {
        callbacks.add(callback);
        return callback;
    }

    private Event newMouseButtonEvent(int buttonID, int action, int mods) {

        MouseButtonEvent event = null;

        MouseButton button = MouseButton.asMouseButton(buttonID);
        Set<KeyModifier> modifiers = KeyModifier.asModifierMask(mods);

        switch (action) {
            case GLFW_PRESS:
                keyRepeatCount = 0;
                event = new MouseButtonPressedEvent(button, modifiers);
                break;
            case GLFW_RELEASE:
                // If a button is released, it will trigger a clicked event at first, and the it will be reset to the release state
                event = new MouseButtonClickedEvent(button, modifiers);
                break;
        }

        return event;
    }

    private Event newKeyEvent(int keyID, int scancode, int action, int mods) {

        KeyEvent event = null;

        Key key = Key.asKey(keyID);
        Set<KeyModifier> modifiers = KeyModifier.asModifierMask(mods);

        switch(action) {
            case GLFW_PRESS:
                keyRepeatCount = 0;
                event = new KeyPressedEvent(key, modifiers);
                break;
            case GLFW_REPEAT:
                event = new KeyRepeatEvent(key, modifiers, ++keyRepeatCount);
                break;
            case GLFW_RELEASE:
                // If a key is released, it will trigger a typed event at first, and the it will be reset to the release state
                event = new KeyTypedEvent(key, modifiers);
                break;
        }

        return event;
    }

}
