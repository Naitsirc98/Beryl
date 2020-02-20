package naitsirc98.beryl.input;

import naitsirc98.beryl.util.EnumMapper;
import naitsirc98.beryl.util.GLFWWrapper;

import static org.lwjgl.glfw.GLFW.*;

public enum State implements GLFWWrapper {

    PRESS(GLFW_PRESS),
    RELEASE(GLFW_RELEASE),
    REPEAT(GLFW_REPEAT),
    TYPE(3),
    CLICK(4);

    private static final EnumMapper<State, Integer> MAPPER;
    static {
        MAPPER = EnumMapper.of(State.class, GLFWWrapper::glfwHandle);
    }

    public static State asState(int id) {
        return MAPPER.keyOf(id);
    }

    private final int glfwHandle;

    State(int glfwHandle) {
        this.glfwHandle = glfwHandle;
    }

    @Override
    public int glfwHandle() {
        return glfwHandle;
    }
}
