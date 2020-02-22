package naitsirc98.beryl.graphics.vulkan;

import naitsirc98.beryl.util.Destructor;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.VkInstance;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static naitsirc98.beryl.graphics.vulkan.VulkanLogicalDevice.newVulkanLogicalDevice;
import static naitsirc98.beryl.graphics.vulkan.VulkanPhysicalDevice.pickPhysicalDevice;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;

@Destructor
public class VulkanDevice implements NativeResource {

    static Set<String> defaultDeviceExtensions() {
        return Stream.of(VK_KHR_SWAPCHAIN_EXTENSION_NAME)
                .collect(toSet());
    }

    private final VkInstance vkInstance;
    private final VulkanPhysicalDevice physicalDevice;
    private final VulkanLogicalDevice logicalDevice;

    public VulkanDevice(VkInstance vkInstance, long surface) {
        this.vkInstance = vkInstance;
        physicalDevice = pickPhysicalDevice(vkInstance, surface);
        logicalDevice = newVulkanLogicalDevice(physicalDevice);
    }

    @Override
    public void free() {
        logicalDevice.free();
    }
}
