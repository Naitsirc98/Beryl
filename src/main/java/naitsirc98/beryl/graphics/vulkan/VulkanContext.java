package naitsirc98.beryl.graphics.vulkan;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.graphics.GraphicsContext;
import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.rendering.RenderingPath;
import naitsirc98.beryl.graphics.vulkan.commands.VulkanCommandPool;
import naitsirc98.beryl.graphics.vulkan.devices.VulkanLogicalDevice;
import naitsirc98.beryl.graphics.vulkan.devices.VulkanPhysicalDevice;
import naitsirc98.beryl.graphics.vulkan.memory.VulkanAllocator;
import naitsirc98.beryl.graphics.vulkan.rendering.phong.VulkanPhongRenderingPath;
import naitsirc98.beryl.graphics.vulkan.rendering.simple.VulkanSimpleRenderingPath;
import naitsirc98.beryl.graphics.vulkan.swapchain.VulkanSwapchain;
import org.lwjgl.vulkan.VkInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static naitsirc98.beryl.graphics.rendering.RenderingPaths.RPATH_PHONG;
import static naitsirc98.beryl.graphics.rendering.RenderingPaths.RPATH_SIMPLE3D;
import static naitsirc98.beryl.graphics.vulkan.VulkanDebugMessenger.newVulkanDebugMessenger;
import static naitsirc98.beryl.graphics.vulkan.VulkanInstanceFactory.newVkInstance;
import static naitsirc98.beryl.graphics.vulkan.VulkanValidationLayers.defaultValidationLayers;
import static naitsirc98.beryl.util.types.TypeUtils.newInstance;
import static org.lwjgl.vulkan.EXTDescriptorIndexing.VK_EXT_DESCRIPTOR_INDEXING_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.vkDestroyInstance;

public final class VulkanContext implements GraphicsContext {

    // TODO: do logicalDevice.waitIdle() before destroying command buffers

    public static final boolean VULKAN_DEBUG_MESSAGES_ENABLED = BerylConfiguration.VULKAN_ENABLE_DEBUG_MESSAGES.get(Beryl.DEBUG);
    public static final boolean VALIDATION_LAYERS_ENABLED = BerylConfiguration.VULKAN_ENABLE_VALIDATION_LAYERS.get(VULKAN_DEBUG_MESSAGES_ENABLED);
    public static final Set<String> VALIDATION_LAYERS = BerylConfiguration.VULKAN_VALIDATION_LAYERS.get(defaultValidationLayers());
    public static final Set<String> DEVICE_EXTENSIONS = BerylConfiguration.VULKAN_DEVICE_EXTENSIONS.get(defaultDeviceExtensions());

    private static Set<String> defaultDeviceExtensions() {
        return Stream.of(
                VK_KHR_SWAPCHAIN_EXTENSION_NAME,
                "VK_KHR_dedicated_allocation",
                "VK_KHR_get_memory_requirements2",
                VK_EXT_DESCRIPTOR_INDEXING_EXTENSION_NAME
                )
                .collect(toSet());
    }

    private VkInstance vkInstance;
    private VulkanDebugMessenger debugMessenger;
    private VulkanSurface surface;
    private VulkanPhysicalDevice physicalDevice;
    private VulkanLogicalDevice logicalDevice;
    private VulkanAllocator allocator;
    private VulkanCommandPool graphicsCommandPool;
    private VulkanSwapchain swapchain;
    private VulkanGraphicsFactory graphicsFactory;
    private VulkanMapper mapper;

    private VulkanContext() {

    }

    @Override
    public void init() {
        vkInstance = newVkInstance();
        debugMessenger = newVulkanDebugMessenger();
        surface = new VulkanSurface();
        physicalDevice = new VulkanPhysicalDevice();
        logicalDevice = new VulkanLogicalDevice();
        allocator = new VulkanAllocator(vkInstance, physicalDevice, logicalDevice);
        graphicsCommandPool = createGraphicsCommandPool();
        swapchain = new VulkanSwapchain();
        graphicsFactory = new VulkanGraphicsFactory();
        mapper = new VulkanMapper();
    }

    @Override
    public boolean vsync() {
        return swapchain.vsync();
    }

    @Override
    public void vsync(boolean vsync) {
        swapchain.vsync(vsync);
    }

    @Override
    public VulkanMapper mapper() {
        return mapper;
    }

    @Override
    public Map<Integer, RenderingPath> renderingPaths() {

        Map<Integer, RenderingPath> renderingPaths = new HashMap<>();

        // renderingPaths.put(RPATH_SIMPLE3D, newInstance(VulkanSimpleRenderingPath.class));
        renderingPaths.put(RPATH_PHONG, newInstance(VulkanPhongRenderingPath.class));

        return renderingPaths;
    }

    @Override
    public GraphicsFactory graphicsFactory() {
        return graphicsFactory;
    }

    public VkInstance vkInstance() {
        return vkInstance;
    }

    public VulkanDebugMessenger debugMessenger() {
        return debugMessenger;
    }

    public VulkanSurface surface() {
        return surface;
    }

    public VulkanPhysicalDevice physicalDevice() {
        return physicalDevice;
    }

    public VulkanLogicalDevice logicalDevice() {
        return logicalDevice;
    }

    public VulkanAllocator allocator() {
        return allocator;
    }

    public VulkanCommandPool graphicsCommandPool() {
        return graphicsCommandPool;
    }

    public VulkanSwapchain swapchain() {
        return swapchain;
    }

    @Override
    public void release() {

        graphicsCommandPool.release();

        swapchain.release();

        graphicsFactory.release();

        allocator.release();

        logicalDevice.release();

        physicalDevice.release();

        surface.release();

        if(VULKAN_DEBUG_MESSAGES_ENABLED) {
            debugMessenger.release();
        }

        vkDestroyInstance(vkInstance, null);

        vkInstance = null;
        graphicsCommandPool = null;
        swapchain = null;
        logicalDevice = null;
        physicalDevice = null;
        surface = null;
        debugMessenger = null;
    }

    private VulkanCommandPool createGraphicsCommandPool() {
        return new VulkanCommandPool(
                logicalDevice.graphicsQueue(),
                physicalDevice.queueFamilyIndices().graphicsFamily());
    }
}
