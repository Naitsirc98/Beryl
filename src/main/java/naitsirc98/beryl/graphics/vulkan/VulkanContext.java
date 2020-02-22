package naitsirc98.beryl.graphics.vulkan;

import naitsirc98.beryl.graphics.GraphicsContext;
import naitsirc98.beryl.util.Destructor;
import org.lwjgl.vulkan.VkInstance;

import static naitsirc98.beryl.graphics.vulkan.VulkanDebugMessenger.newVulkanDebugMessenger;
import static naitsirc98.beryl.graphics.vulkan.VulkanInstanceFactory.newVkInstance;
import static org.lwjgl.vulkan.VK10.vkDestroyInstance;

@Destructor
public class VulkanContext implements GraphicsContext {

    private final VkInstance vkInstance;
    private final VulkanDevice device;
    private final VulkanDebugMessenger debugMessenger;

    private VulkanContext() {
        vkInstance = newVkInstance();
        debugMessenger = newVulkanDebugMessenger(vkInstance);
        device = new VulkanDevice(vkInstance);
    }

    @Override
    public void free() {

        device.free();

        debugMessenger.free();

        vkDestroyInstance(vkInstance, null);
    }
}
