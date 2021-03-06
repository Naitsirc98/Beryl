package naitsirc98.beryl.input;

import naitsirc98.beryl.util.GLFWWrapper;
import org.lwjgl.glfw.GLFWGamepadState;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.EnumMap;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static naitsirc98.beryl.input.Joystick.Axis.asJoystickAxis;
import static naitsirc98.beryl.input.Joystick.Button.asJoystickButton;
import static naitsirc98.beryl.input.State.asState;
import static naitsirc98.beryl.util.Asserts.assertNonNull;
import static naitsirc98.beryl.util.Asserts.assertTrue;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memASCIISafe;

public class Gamepad implements GLFWWrapper {

    private static final float AXIS_THRESHOLD = 0.2f;

    public static Gamepad of(Joystick joystick) {
        return Input.gamepad(joystick);
    }

    private final Joystick joystick;
    private final StateTable<Joystick.Button> buttons;
    private final EnumMap<Joystick.Axis, Float> axes;

    Gamepad(Joystick joystick) {
        assertTrue(joystick.isGamepad());
        this.joystick = joystick;
        buttons = new StateTable<>(Joystick.Button.class);
        axes = new EnumMap<>(Joystick.Axis.class);
        update();
    }

    public Joystick joystick() {
        return joystick;
    }

    @Override
    public int glfwHandle() {
        return joystick().glfwHandle();
    }

    public String name() {
        return glfwGetGamepadName(glfwHandle());
    }

    public String guid() {
        return joystick().guid();
    }

    public Gamepad updateMapping(CharSequence mapping) {
        return updateMapping(memASCIISafe(mapping));
    }

    public Gamepad updateMapping(ByteBuffer mapping) {
        assertTrue(glfwUpdateGamepadMappings(assertNonNull(mapping)));
        return this;
    }

    public boolean isPresent() {
        return joystick().isPresent();
    }

    public StateTable<Joystick.Button> buttons() {
        return buttons;
    }

    public float axis(Joystick.Axis axis) {
        return axes.getOrDefault(axis, 0.0f);
    }

    public boolean moved(Joystick.Axis axis, Joystick.AxisDirection direction) {
        final float state = axis(axis);
        if(state == 0) {
            return false;
        }
        return Math.signum(state) == direction.direction();
    }

    public EnumMap<Joystick.Axis, Float> axes() {
        return axes;
    }

    void update() {
        try(MemoryStack stack = stackPush()) {
            GLFWGamepadState gamepadState = GLFWGamepadState.mallocStack(stack);
            glfwGetGamepadState(glfwHandle(), gamepadState);
            updateButtons(gamepadState);
            updateAxes(gamepadState);
        }
    }

    private void updateButtons(GLFWGamepadState gamepadState) {
        ByteBuffer states = gamepadState.buttons();
        buttons.clear();
        for(int i = 0;i < states.limit();i++) {
            buttons.set(asJoystickButton(i), asState(states.get(i)));
        }
    }

    private void updateAxes(GLFWGamepadState gamepadState) {
        FloatBuffer states = gamepadState.axes();
        axes.clear();
        for(int i = 0;i < states.limit();i++) {

            float state = states.get(i);

            if(abs(state) < AXIS_THRESHOLD) {
                state = 0;
            }

            axes.put(asJoystickAxis(i), state);
        }
    }

}
