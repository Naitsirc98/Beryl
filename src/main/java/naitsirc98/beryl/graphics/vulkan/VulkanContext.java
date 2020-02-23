package naitsirc98.beryl.graphics.vulkan;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.graphics.GraphicsContext;
import naitsirc98.beryl.graphics.vulkan.devices.VulkanDevice;
import naitsirc98.beryl.graphics.vulkan.swapchain.VulkanSwapchain;
import naitsirc98.beryl.util.Destructor;
import org.lwjgl.vulkan.VkInstance;

import java.util.Set;

import static naitsirc98.beryl.graphics.vulkan.VulkanDebugMessenger.newVulkanDebugMessenger;
import static naitsirc98.beryl.graphics.vulkan.devices.VulkanDevice.defaultDeviceExtensions;
import static naitsirc98.beryl.graphics.vulkan.VulkanInstanceFactory.newVkInstance;
import static naitsirc98.beryl.graphics.vulkan.VulkanSurface.newVulkanSurface;
import static naitsirc98.beryl.graphics.vulkan.VulkanValidationLayers.defaultValidationLayers;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.VK10.vkDestroyInstance;

@Destructor
public class VulkanContext implements GraphicsContext {

    public static final boolean VULKAN_DEBUG_MESSAGES_ENABLED = BerylConfiguration.VULKAN_ENABLE_DEBUG_MESSAGES.get(Beryl.DEBUG);
    public static final Set<String> VALIDATION_LAYERS = BerylConfiguration.VULKAN_VALIDATION_LAYERS.get(defaultValidationLayers());
    public static final Set<String> DEVICE_EXTENSIONS = BerylConfiguration.VULKAN_DEVICE_EXTENSIONS.get(defaultDeviceExtensions());


    private final VkInstance vkInstance;
    private final VulkanDebugMessenger debugMessenger;
    private final long surface;
    private final VulkanDevice device;
    private final VulkanSwapchain swapchain;

    private VulkanContext() {
        vkInstance = newVkInstance();
        debugMessenger = newVulkanDebugMessenger(vkInstance);
        surface = newVulkanSurface(vkInstance);
        device = new VulkanDevice(vkInstance, surface);
        swapchain = new VulkanSwapchain(device);
    }

    @Override
    public void free() {

        swapchain.free();

        device.waitIdle();

        device.free();

        vkDestroySurfaceKHR(vkInstance, surface, null);

        if(VULKAN_DEBUG_MESSAGES_ENABLED) {
            debugMessenger.free();
        }

        vkDestroyInstance(vkInstance, null);
    }
}
