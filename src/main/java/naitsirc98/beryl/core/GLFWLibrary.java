package naitsirc98.beryl.core;

import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwTerminate;

public final class GLFWLibrary extends BerylSystem {

    @Override
    protected void init() {
        if(!glfwInit()) {
            throw new ExceptionInInitializerError("Cannot initialize GLFW");
        }
    }

    @Override
    protected void terminate() {
        glfwTerminate();
    }

}
