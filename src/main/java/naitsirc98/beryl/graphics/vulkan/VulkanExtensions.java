package naitsirc98.beryl.graphics.vulkan;

import naitsirc98.beryl.logging.Log;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import static naitsirc98.beryl.graphics.vulkan.VulkanContext.VULKAN_DEBUG_MESSAGES_ENABLED;
import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME;

public class VulkanExtensions {

    public static PointerBuffer requiredExtensions() {

        PointerBuffer glfwExtensions = glfwGetRequiredInstanceExtensions();

        if(glfwExtensions == null) {
            Log.fatal("Failed to retrieve GLFW required extensions");
            return null;
        }

        if(VULKAN_DEBUG_MESSAGES_ENABLED) {
            return extensionsWithDebugSupport(glfwExtensions);
        }

        return glfwExtensions;
    }

    private static PointerBuffer extensionsWithDebugSupport(PointerBuffer glfwExtensions) {

        MemoryStack stack = stackGet();

        PointerBuffer extensions = stack.mallocPointer(glfwExtensions.capacity() + 1);

        extensions.put(glfwExtensions);
        extensions.put(stack.UTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME));

        return extensions.rewind();
    }

}
