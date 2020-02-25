package naitsirc98.beryl.graphics.vulkan;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.graphics.GraphicsContext;
import naitsirc98.beryl.graphics.rendering.Renderer;
import naitsirc98.beryl.graphics.rendering.RenderingPath;
import naitsirc98.beryl.graphics.vulkan.commands.VulkanCommandPool;
import naitsirc98.beryl.graphics.vulkan.devices.VulkanDevice;
import naitsirc98.beryl.graphics.vulkan.rendering.VulkanRenderer;
import naitsirc98.beryl.graphics.vulkan.swapchain.VulkanSwapchain;
import naitsirc98.beryl.util.Destructor;
import org.lwjgl.vulkan.VkInstance;

import java.util.Map;
import java.util.Set;

import static naitsirc98.beryl.graphics.vulkan.VulkanDebugMessenger.newVulkanDebugMessenger;
import static naitsirc98.beryl.graphics.vulkan.VulkanInstanceFactory.newVkInstance;
import static naitsirc98.beryl.graphics.vulkan.VulkanSurface.newVulkanSurface;
import static naitsirc98.beryl.graphics.vulkan.VulkanValidationLayers.defaultValidationLayers;
import static naitsirc98.beryl.graphics.vulkan.devices.VulkanDevice.defaultDeviceExtensions;
import static naitsirc98.beryl.util.TypeUtils.newInstance;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.VK10.vkDestroyInstance;

@Destructor
public final class VulkanContext implements GraphicsContext {

    public static final boolean VULKAN_DEBUG_MESSAGES_ENABLED = BerylConfiguration.VULKAN_ENABLE_DEBUG_MESSAGES.get(Beryl.DEBUG);
    public static final Set<String> VALIDATION_LAYERS = BerylConfiguration.VULKAN_VALIDATION_LAYERS.get(defaultValidationLayers());
    public static final Set<String> DEVICE_EXTENSIONS = BerylConfiguration.VULKAN_DEVICE_EXTENSIONS.get(defaultDeviceExtensions());


    private VkInstance vkInstance;
    private VulkanDebugMessenger debugMessenger;
    private long surface;
    private VulkanDevice device;
    private VulkanCommandPool graphicsCommandPool;
    private VulkanSwapchain swapchain;
    private VulkanRenderer renderer;

    private VulkanContext() {

    }

    @Override
    public void init() {
        vkInstance = newVkInstance();
        debugMessenger = newVulkanDebugMessenger(vkInstance);
        surface = newVulkanSurface(vkInstance);
        device = new VulkanDevice(vkInstance, surface);
        graphicsCommandPool = createGraphicsCommandPool();
        swapchain = new VulkanSwapchain(device);
        renderer = newInstance(VulkanRenderer.class, swapchain, graphicsCommandPool);
    }

    @Override
    public Renderer renderer() {
        return renderer;
    }

    @Override
    public Map<Integer, RenderingPath> renderingPaths() {
        return null;
    }

    public VkInstance vkInstance() {
        return vkInstance;
    }

    public VulkanDebugMessenger debugMessenger() {
        return debugMessenger;
    }

    public long surface() {
        return surface;
    }

    public VulkanDevice device() {
        return device;
    }

    public VulkanCommandPool graphicsCommandPool() {
        return graphicsCommandPool;
    }

    public VulkanSwapchain swapchain() {
        return swapchain;
    }

    @Override
    public void free() {

        device.waitIdle();

        renderer.free();

        graphicsCommandPool.free();

        swapchain.free();

        device.free();

        vkDestroySurfaceKHR(vkInstance, surface, null);

        if(VULKAN_DEBUG_MESSAGES_ENABLED) {
            debugMessenger.free();
        }

        vkDestroyInstance(vkInstance, null);
    }

    private VulkanCommandPool createGraphicsCommandPool() {
        return new VulkanCommandPool(
                device.logicalDevice().vkDevice(),
                device.logicalDevice().graphicsQueue(),
                device.physicalDevice().queueFamilyIndices().graphicsFamily());
    }
}
