package naitsirc98.beryl.graphics.vulkan.devices;

import naitsirc98.beryl.util.Destructor;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.VkInstance;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static naitsirc98.beryl.graphics.vulkan.devices.VulkanLogicalDevice.newVulkanLogicalDevice;
import static naitsirc98.beryl.graphics.vulkan.devices.VulkanPhysicalDevice.pickPhysicalDevice;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.vkDeviceWaitIdle;

@Destructor
public class VulkanDevice implements NativeResource {

    public static Set<String> defaultDeviceExtensions() {
        return Stream.of(VK_KHR_SWAPCHAIN_EXTENSION_NAME)
                .collect(toSet());
    }

    private final VkInstance vkInstance;
    private final long surface;
    private final VulkanPhysicalDevice physicalDevice;
    private final VulkanLogicalDevice logicalDevice;

    public VulkanDevice(VkInstance vkInstance, long surface) {
        this.vkInstance = vkInstance;
        this.surface = surface;
        physicalDevice = pickPhysicalDevice(vkInstance, surface);
        logicalDevice = newVulkanLogicalDevice(physicalDevice);
    }

    public VkInstance vkInstance() {
        return vkInstance;
    }

    public long surface() {
        return surface;
    }

    public VulkanPhysicalDevice physicalDevice() {
        return physicalDevice;
    }

    public VulkanLogicalDevice logicalDevice() {
        return logicalDevice;
    }

    @Override
    public void free() {
        logicalDevice.free();
        physicalDevice.free();
    }

    public void waitIdle() {
        vkDeviceWaitIdle(logicalDevice.vkDevice());
    }
}
