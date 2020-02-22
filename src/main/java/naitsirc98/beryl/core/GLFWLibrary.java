package naitsirc98.beryl.core;

import naitsirc98.beryl.logging.Log;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;

final class GLFWLibrary extends BerylSystem {

    private final GLFWErrorCallback errorCallback;

    private GLFWLibrary() {
        errorCallback = GLFWErrorCallback.create(this::onGLFWError);
    }

    @Override
    protected void init() {

        glfwSetErrorCallback(errorCallback);

        if(!glfwInit()) {
            throw new ExceptionInInitializerError("Cannot initialize GLFW");
        }
    }

    @Override
    protected void terminate() {

        errorCallback.free();

        glfwTerminate();
    }

    private void onGLFWError(int error, long pDescription) {
        Log.error(String.format("GLFW ERROR code %s: %s", errorName(error), descriptionMsg(pDescription)));
    }

    private String descriptionMsg(long pDescription) {
        try(MemoryStack stack = stackPush()) {
            return stack.pointers(pDescription).getStringASCII();
        }
    }

    private String errorName(int error) {
        switch(error) {
            case GLFW_NOT_INITIALIZED:
                return "GLFW_NOT_INITIALIZED";
            case GLFW_NO_CURRENT_CONTEXT:
                return "GLFW_NO_CURRENT_CONTEXT";
            case GLFW_INVALID_ENUM:
                return "GLFW_INVALID_ENUM";
            case GLFW_INVALID_VALUE:
                return "GLFW_INVALID_VALUE";
            case GLFW_OUT_OF_MEMORY:
                return "GLFW_OUT_OF_MEMORY";
            case GLFW_API_UNAVAILABLE:
                return "GLFW_API_UNABAILABLE";
            case GLFW_PLATFORM_ERROR:
                return "GLFW_PLATFORM_ERROR";
            case GLFW_FORMAT_UNAVAILABLE:
                return "GLFW_FORMAT_UNAVAILABLE";
        }
        return "UNKNOWN ERROR";
    }

}
