package naitsirc98.beryl.graphics;

import naitsirc98.beryl.util.LongHandle;

import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;

public abstract class WindowDestroyAccessor implements LongHandle {

    protected void destroy() {
        glfwDestroyWindow(handle());
    }

}
