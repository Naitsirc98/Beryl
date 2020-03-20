package naitsirc98.beryl.graphics.vulkan;

import naitsirc98.beryl.graphics.window.Window;
import org.lwjgl.system.MemoryStack;

import java.nio.LongBuffer;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;

public class VulkanSurface implements VulkanObject.Long {

    private long vkSurface;

    public VulkanSurface() {
        vkSurface = createVkSurface();
    }

    @Override
    public long handle() {
        return vkSurface;
    }

    @Override
    public void release() {
        vkDestroySurfaceKHR(vkInstance(), vkSurface, null);
        vkSurface = VK_NULL_HANDLE;
    }

    private long createVkSurface() {

        try(MemoryStack stack = stackPush()) {

            LongBuffer pSurface = stack.longs(VK_NULL_HANDLE);

            vkCall(glfwCreateWindowSurface(vkInstance(), Window.get().handle(), null, pSurface));

            return pSurface.get(0);
        }
    }
}
