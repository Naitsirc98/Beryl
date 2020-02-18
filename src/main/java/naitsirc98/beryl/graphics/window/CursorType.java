package naitsirc98.beryl.graphics.window;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;

public enum CursorType {

    NORMAL(GLFW_CURSOR_NORMAL),
    HIDDEN(GLFW_CURSOR_HIDDEN),
    DISABLED(GLFW_CURSOR_DISABLED);

    public static CursorType of(int glfwInputMode) {
        switch(glfwInputMode) {
            case GLFW_CURSOR_NORMAL:
                return NORMAL;
            case GLFW_CURSOR_HIDDEN:
                return HIDDEN;
            case GLFW_CURSOR_DISABLED:
                return DISABLED;
        }
        return null;
    }

    private final int glfwInputMode;

    CursorType(int glfwInputMode) {
        this.glfwInputMode = glfwInputMode;
    }

    public int glfwInputMode() {
        return glfwInputMode;
    }

}
