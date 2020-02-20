package naitsirc98.beryl.input;

import naitsirc98.beryl.util.EnumMapper;
import naitsirc98.beryl.util.GLFWWrapper;

import static org.lwjgl.glfw.GLFW.*;

public enum KeyModifier implements GLFWWrapper {

    MOD_SHIFT(GLFW_MOD_SHIFT),
    MOD_CONTROL(GLFW_MOD_CONTROL),
    MOD_ALT(GLFW_MOD_ALT),
    MOD_SUPER(GLFW_MOD_SUPER),
    MOD_NUM_LOCK(GLFW_MOD_NUM_LOCK);

    private static final EnumMapper<KeyModifier, Integer> MAPPER;
    static {
        MAPPER = EnumMapper.of(KeyModifier.class, GLFWWrapper::glfwHandle);
    }

    public static KeyModifier asKeyModifier(int glfwHandle) {
        return MAPPER.keyOf(glfwHandle);
    }

    private final int glfwHandle;

    KeyModifier(int glfwHandle) {
        this.glfwHandle = glfwHandle;
    }

    @Override
    public int glfwHandle() {
        return 0;
    }
}
