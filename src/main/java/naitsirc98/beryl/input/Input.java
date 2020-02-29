package naitsirc98.beryl.input;

import naitsirc98.beryl.core.BerylSystem;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.events.EventManager;
import naitsirc98.beryl.events.input.joystick.JoystickConnectedEvent;
import naitsirc98.beryl.events.input.joystick.JoystickDisconnectedEvent;
import naitsirc98.beryl.events.input.keyboard.KeyPressedEvent;
import naitsirc98.beryl.events.input.keyboard.KeyReleasedEvent;
import naitsirc98.beryl.events.input.keyboard.KeyRepeatEvent;
import naitsirc98.beryl.events.input.keyboard.KeyTypedEvent;
import naitsirc98.beryl.events.input.mouse.*;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.util.Singleton;
import org.lwjgl.glfw.GLFWJoystickCallback;
import org.lwjgl.system.MemoryStack;

import java.nio.DoubleBuffer;
import java.util.EnumMap;

import static naitsirc98.beryl.input.Joystick.asJoystick;
import static org.lwjgl.glfw.GLFW.*;

/**
 * Class for manage input events
 *
 * */
public final class Input extends BerylSystem {

    @Singleton
    private static Input instance;

    /**
     * Tells whether this key is released or not. This is the default state
     *
     * @param key the key
     * @return return true if this key is released, false otherwise
     */
    public static boolean isKeyReleased(Key key) {
        return instance.keyStates.isReleased(key);
    }

    /**
     * Tells whether this key is pressed or not.
     *
     * @param key the key
     * @return return true if this key is pressed, false otherwise
     */
    public static boolean isKeyPressed(Key key) {
        return instance.keyStates.isPressed(key) || isKeyRepeat(key);
    }

    /**
     * Tells whether this key is repeated or not.
     *
     * @param key the key
     * @return return true if this key is repeated, false otherwise
     */
    public static boolean isKeyRepeat(Key key) {
        return instance.keyStates.isRepeat(key);
    }

    /**
     * Tells whether this key has been pressed and then released or not.
     *
     * @param key the key
     * @return return true if this key is typed, false otherwise
     */
    public static boolean isKeyTyped(Key key) {
        return instance.keyStates.isType(key);
    }

    /**
     * Returns the last known state of this key
     *
     * @param key the key
     * @return the state of this key
     */
    public static State stateOf(Key key) {
        return instance.keyStates.stateOf(key);
    }

    /**
     * Tells whether this mouse button is released or not. This is the default state.
     *
     * @param button the mouse button
     * @return return true if this mouse button is released, false otherwise
     */
    public static boolean isMouseButtonReleased(MouseButton button) {
        return instance.mouseButtonStates.isReleased(button);
    }

    /**
     * Tells whether this mouse button is pressed or not.
     *
     * @param button the mouse button
     * @return return true if this mouse button is pressed, false otherwise
     */
    public static boolean isMouseButtonPressed(MouseButton button) {
        return instance.mouseButtonStates.isPressed(button);
    }

    /**
     * Tells whether this mouse button is repeated or not.
     *
     * @param button the mouse button
     * @return return true if this mouse button is repeated, false otherwise
     */
    public static boolean isMouseButtonRepeat(MouseButton button) {
        return instance.mouseButtonStates.isRepeat(button);
    }

    /**
     * Tells whether this mouse button has been pressed and then released or not.
     *
     * @param button the mouse button
     * @return return true if this mouse button is clicked, false otherwise
     */
    public static boolean isMouseButtonClicked(MouseButton button) {
        return instance.mouseButtonStates.isClick(button);
    }

    /**
     * Returns the last know state of this mouse button.
     *
     * @param button the mouse button
     * @return the state of this mouse button
     */
    public static State stateOf(MouseButton button) {
        return instance.mouseButtonStates.stateOf(button);
    }

    /**
     * Returns the x position of the mouse
     *
     * @return the mouse x position
     */
    public static float mouseX() {
        return instance.mouseX;
    }

    /**
     * Returns the y position of the mouse
     *
     * @return the mouse y position
     */
    public static float mouseY() {
        return instance.mouseY;
    }

    /**
     * Returns the x scroll offset of the mouse
     *
     * @return the mouse x scroll offset
     */
    public static float scrollX() {
        return instance.mouseScrollX;
    }

    /**
     * Returns the y scroll offset of the mouse
     *
     * @return the mouse y scroll offset
     */
    public static float scrollY() {
        return instance.mouseScrollY;
    }

    /**
     * Returns a gamepad view of the given joystick
     *
     * @param joystick the joystick
     * @return the gamepad from the given joystick
     * */
    public static Gamepad gamepad(Joystick joystick) {

        if(!joystick.isPresent() || !joystick.isGamepad()) {
            return null;
        }

        return instance.gamepads.computeIfAbsent(joystick, j -> new Gamepad(joystick));
    }


    private final StatesArray<Key> keyStates;
    private final StatesArray<MouseButton> mouseButtonStates;
    private final EnumMap<Joystick, Gamepad> gamepads;
    private final GLFWJoystickCallback joystickCallback;
    private float mouseX, mouseY;
    private float mouseScrollX, mouseScrollY;

    private Input() {
        keyStates = new StatesArray<>(Key.class);
        mouseButtonStates = new StatesArray<>(MouseButton.class);
        gamepads = new EnumMap<>(Joystick.class);
        joystickCallback = GLFWJoystickCallback.create(this::onJoystickEvent);
    }

    @Override
    protected void init() {
        setEventCallbacks();
    }

    @Override
    protected void terminate() {
        joystickCallback.free();
    }

    public void update() {
        gamepads.values().forEach(Gamepad::update);
    }

    private void cacheMousePosition() {

        final Window window = Window.get();

        try(MemoryStack stack = MemoryStack.stackPush()) {

            DoubleBuffer x = stack.mallocDouble(1);
            DoubleBuffer y = stack.mallocDouble(1);

            glfwGetCursorPos(window.handle(), x, y);

            mouseX = (float) x.get(0);
            mouseY = (float) y.get(0);
        }
    }

    private void setEventCallbacks() {
        setKeyboardEventCallbacks();
        setMouseEventCallbacks();
        setJoystickCallbacks();
    }

    private void setJoystickCallbacks() {

        glfwSetJoystickCallback(joystickCallback);

        EventManager.addEventCallback(JoystickConnectedEvent.class, e -> {
            Log.info(e.joystick() + " has been connected");
            Gamepad.of(e.joystick());
        });

        EventManager.addEventCallback(JoystickConnectedEvent.class, e -> {
            Log.info(e.joystick() + " has been disconnected");
            gamepads.remove(e.joystick());
        });
    }

    private void setKeyboardEventCallbacks() {

        EventManager.pushEventCallback(KeyPressedEvent.class, e -> keyStates.set(e.key(), State.PRESS));

        EventManager.pushEventCallback(KeyRepeatEvent.class, e -> keyStates.set(e.key(), State.REPEAT));

        EventManager.pushEventCallback(KeyReleasedEvent.class, e -> {

            final Key key = e.key();

            // When a release event is triggered, that means that the key was pressed before, so there is also a key typed event
            // When this happens, we need to trigger a key typed event and clear the state to RELEASE afterwards
            if(keyStates.stateOf(key) != State.TYPE) {
                // Trigger a typed event
                keyStates.set(key, State.TYPE);
                EventManager.triggerEvent(new KeyTypedEvent(key, e.modifiers()));
                // Reset state to RELEASE in the next event pass
                EventManager.submitLater(new KeyReleasedEvent(key, e.modifiers()));

            } else {
                keyStates.set(key, State.RELEASE);
            }
        });
    }

    private void setMouseEventCallbacks() {

        EventManager.pushEventCallback(MouseMovedEvent.class, e -> cacheMousePosition());

        EventManager.pushEventCallback(MouseButtonPressedEvent.class, e -> mouseButtonStates.set(e.button(), State.PRESS));

        EventManager.pushEventCallback(MouseButtonReleasedEvent.class, e -> {

            final MouseButton button = e.button();

            // Same as in key release
            if(mouseButtonStates.stateOf(button) != State.CLICK) {
                // Trigger a clicked event
                mouseButtonStates.set(button, State.CLICK);
                EventManager.triggerEvent(new MouseButtonClickedEvent(button, e.modifiers()));
                // Reset in the next event pass
                EventManager.submitLater(new MouseButtonReleasedEvent(button, e.modifiers()));
            } else {
                mouseButtonStates.set(button, State.RELEASE);
            }
        });

        final class ClearMouseScrollEvent extends MouseEvent {}

        EventManager.pushEventCallback(ClearMouseScrollEvent.class, e -> mouseScrollX = mouseScrollY = 0.0f);

        EventManager.pushEventCallback(MouseScrollEvent.class, e -> {

            mouseScrollX = e.getXOffset();
            mouseScrollY = e.getYOffset();

            EventManager.submitLater(new ClearMouseScrollEvent());
        });
    }

    private void onJoystickEvent(int jid, int event) {

        Joystick joystick = asJoystick(jid);

        EventManager.triggerEvent(event == GLFW_CONNECTED ? new JoystickConnectedEvent(joystick) : new JoystickDisconnectedEvent(joystick));
    }

}
