package naitsirc98.beryl.graphics.vulkan;

import naitsirc98.beryl.graphics.window.Window;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkInstance;

import java.nio.LongBuffer;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;

public class VulkanSurface {

    static long newVulkanSurface(VkInstance vkInstance) {

        try(MemoryStack stack = stackPush()) {

            LongBuffer pSurface = stack.longs(VK_NULL_HANDLE);

            vkCall(glfwCreateWindowSurface(vkInstance, Window.get().handle(), null, pSurface));

            return pSurface.get(0);
        }
    }

}
