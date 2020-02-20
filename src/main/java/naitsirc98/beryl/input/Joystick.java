package naitsirc98.beryl.input;

import naitsirc98.beryl.util.EnumMapper;
import naitsirc98.beryl.util.GLFWWrapper;

import java.nio.ByteBuffer;

import static naitsirc98.beryl.input.Joystick.Button.asJoystickButton;
import static naitsirc98.beryl.input.Joystick.Hat.asJoystickHat;
import static naitsirc98.beryl.input.State.asState;
import static org.lwjgl.glfw.GLFW.*;

public enum Joystick implements GLFWWrapper {

    JOYSTICK_1(GLFW_JOYSTICK_1),
    JOYSTICK_2(GLFW_JOYSTICK_2),
    JOYSTICK_3(GLFW_JOYSTICK_3),
    JOYSTICK_4(GLFW_JOYSTICK_4),
    JOYSTICK_5(GLFW_JOYSTICK_5),
    JOYSTICK_6(GLFW_JOYSTICK_6),
    JOYSTICK_7(GLFW_JOYSTICK_7),
    JOYSTICK_8(GLFW_JOYSTICK_8),
    JOYSTICK_9(GLFW_JOYSTICK_9),
    JOYSTICK_10(GLFW_JOYSTICK_10),
    JOYSTICK_11(GLFW_JOYSTICK_11),
    JOYSTICK_12(GLFW_JOYSTICK_12),
    JOYSTICK_13(GLFW_JOYSTICK_13),
    JOYSTICK_14(GLFW_JOYSTICK_14),
    JOYSTICK_15(GLFW_JOYSTICK_15),
    JOYSTICK_16(GLFW_JOYSTICK_16),
    JOYSTICK_LAST(GLFW_JOYSTICK_LAST);

    private static final EnumMapper<Joystick, Integer> MAPPER;
    static {
        MAPPER = EnumMapper.of(Joystick.class, GLFWWrapper::glfwHandle);
    }

    static {
        // TODO
        // glfwSetJoystickCallback(CALLBACK);
    }

    public static Joystick asJoystick(int glfwHandle) {
        return MAPPER.keyOf(glfwHandle);
    }

    private final int glfwHandle;

    Joystick(int glfwHandle) {
        this.glfwHandle = glfwHandle;
    }

    @Override
    public int glfwHandle() {
        return glfwHandle;
    }

    public boolean isPresent() {
        return glfwJoystickPresent(glfwHandle);
    }

    public boolean isGamepad() {
        return glfwJoystickIsGamepad(glfwHandle);
    }

    public Gamepad asGamepad() {
        return Gamepad.of(this);
    }

    public StatesArray<Button> buttons() {
        return buttons(new StatesArray<>(Button.class));
    }

    public StatesArray<Button> buttons(StatesArray<Button> statesArray) {

        statesArray.clear();

        ByteBuffer states = glfwGetJoystickButtons(glfwHandle);

        if(states == null) {
            return null;
        }

        for(int i = 0;i < states.limit();i++) {
            statesArray.set(asJoystickButton(i), asState(states.get(i)));
        }

        return statesArray;
    }

    public StatesArray<Hat> hats() {
        return hats(new StatesArray<>(Hat.class));
    }

    public StatesArray<Hat> hats(StatesArray<Hat> statesArray) {

        statesArray.clear();

        ByteBuffer states = glfwGetJoystickHats(glfwHandle);

        if(states == null) {
            return null;
        }

        for(int i = 0;i < states.limit();i++) {
            statesArray.set(asJoystickHat(i), asState(states.get(i)));
        }

        return statesArray;
    }

    public String joystickName() {
        return glfwGetJoystickName(glfwHandle);
    }

    public String guid() {
        return glfwGetJoystickGUID(glfwHandle);
    }

    public enum Button implements GLFWWrapper {

        BUTTON_A(GLFW_GAMEPAD_BUTTON_A),
        BUTTON_B(GLFW_GAMEPAD_BUTTON_B),
        BUTTON_X(GLFW_GAMEPAD_BUTTON_X),
        BUTTON_Y(GLFW_GAMEPAD_BUTTON_Y),
        BUTTON_LEFT_BUMPER(GLFW_GAMEPAD_BUTTON_LEFT_BUMPER),
        BUTTON_RIGHT_BUMPER(GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER),
        BUTTON_BACK(GLFW_GAMEPAD_BUTTON_BACK),
        BUTTON_START(GLFW_GAMEPAD_BUTTON_START),
        BUTTON_GUIDE(GLFW_GAMEPAD_BUTTON_GUIDE),
        BUTTON_LEFT_THUMB(GLFW_GAMEPAD_BUTTON_LEFT_THUMB),
        BUTTON_RIGHT_THUMB(GLFW_GAMEPAD_BUTTON_RIGHT_THUMB),
        BUTTON_DPAD_UP(GLFW_GAMEPAD_BUTTON_DPAD_UP),
        BUTTON_DPAD_RIGHT(GLFW_GAMEPAD_BUTTON_DPAD_RIGHT),
        BUTTON_DPAD_DOWN(GLFW_GAMEPAD_BUTTON_DPAD_DOWN),
        BUTTON_DPAD_LEFT(GLFW_GAMEPAD_BUTTON_DPAD_LEFT),
        BUTTON_LAST(GLFW_GAMEPAD_BUTTON_LAST),
        BUTTON_CROSS(GLFW_GAMEPAD_BUTTON_CROSS),
        BUTTON_CIRCLE(GLFW_GAMEPAD_BUTTON_CIRCLE),
        BUTTON_SQUARE(GLFW_GAMEPAD_BUTTON_SQUARE),
        BUTTON_TRIANGLE(GLFW_GAMEPAD_BUTTON_TRIANGLE);

        private static final EnumMapper<Button, Integer> MAPPER;
        static {
            MAPPER = EnumMapper.of(Button.class, GLFWWrapper::glfwHandle);
        }

        public static Button asJoystickButton(int glfwHandle) {
            return MAPPER.keyOf(glfwHandle);
        }

        private final int glfwHandle;

        Button(int glfwHandle) {
            this.glfwHandle = glfwHandle;
        }

        @Override
        public int glfwHandle() {
            return glfwHandle;
        }
    }

    public enum Hat implements GLFWWrapper {

        HAT_CENTERED(GLFW_HAT_CENTERED),
        HAT_UP(GLFW_HAT_UP),
        HAT_RIGHT(GLFW_HAT_RIGHT),
        HAT_DOWN(GLFW_HAT_DOWN),
        HAT_LEFT(GLFW_HAT_LEFT),
        HAT_RIGHT_UP(GLFW_HAT_RIGHT_UP),
        HAT_RIGHT_DOWN(GLFW_HAT_RIGHT_DOWN),
        HAT_LEFT_UP(GLFW_HAT_LEFT_UP),
        HAT_LEFT_DOWN(GLFW_HAT_LEFT_DOWN);

        private static final EnumMapper<Hat, Integer> MAPPER;
        static {
            MAPPER = EnumMapper.of(Hat.class, GLFWWrapper::glfwHandle);
        }

        public static Hat asJoystickHat(int glfwHandle) {
            return MAPPER.keyOf(glfwHandle);
        }

        private final int glfwHandle;

        Hat(int glfwHandle) {
            this.glfwHandle = glfwHandle;
        }

        @Override
        public int glfwHandle() {
            return 0;
        }
    }

    public enum Axis implements GLFWWrapper {

        AXIS_LEFT_X(GLFW_GAMEPAD_AXIS_LEFT_X),
        AXIS_LEFT_Y(GLFW_GAMEPAD_AXIS_LEFT_Y),
        AXIS_RIGHT_X(GLFW_GAMEPAD_AXIS_RIGHT_X),
        AXIS_RIGHT_Y(GLFW_GAMEPAD_AXIS_RIGHT_Y),
        AXIS_LEFT_TRIGGER(GLFW_GAMEPAD_AXIS_LEFT_TRIGGER),
        AXIS_RIGHT_TRIGGER(GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER),
        AXIS_LAST(GLFW_GAMEPAD_AXIS_LAST);

        private static final EnumMapper<Axis, Integer> MAPPER;
        static {
            MAPPER = EnumMapper.of(Axis.class, GLFWWrapper::glfwHandle);
        }

        public static Axis asJoystickAxis(int id) {
            return MAPPER.keyOf(id);
        }

        private final int glfwHandle;

        Axis(int glfwHandle) {
            this.glfwHandle = glfwHandle;
        }

        @Override
        public int glfwHandle() {
            return glfwHandle;
        }

    }

}
