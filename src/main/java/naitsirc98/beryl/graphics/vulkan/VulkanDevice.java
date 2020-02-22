package naitsirc98.beryl.graphics.vulkan;

import naitsirc98.beryl.util.Destructor;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueue;

@Destructor
public class VulkanDevice implements NativeResource {

    private final VkInstance vkInstance;
    private VkPhysicalDevice physicalDevice;
    private VkDevice device;
    private VkQueue graphicsQueue;
    private VkQueue presentationQueue;

    public VulkanDevice(VkInstance vkInstance) {
        this.vkInstance = vkInstance;
    }

    

    @Override
    public void free() {

    }
}
