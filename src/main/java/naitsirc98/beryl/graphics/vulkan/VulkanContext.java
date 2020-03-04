package naitsirc98.beryl.graphics.vulkan;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.graphics.GraphicsContext;
import naitsirc98.beryl.graphics.rendering.RenderingPath;
import naitsirc98.beryl.graphics.vulkan.commands.VulkanCommandPool;
import naitsirc98.beryl.graphics.vulkan.devices.VulkanLogicalDevice;
import naitsirc98.beryl.graphics.vulkan.devices.VulkanPhysicalDevice;
import naitsirc98.beryl.graphics.vulkan.rendering.VulkanRenderer;
import naitsirc98.beryl.graphics.vulkan.rendering.VulkanSimpleRenderingPath;
import naitsirc98.beryl.graphics.vulkan.swapchain.VulkanSwapchain;
import naitsirc98.beryl.graphics.vulkan.vertex.VulkanVertexDataBuilder;
import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import naitsirc98.beryl.util.types.Destructor;
import org.lwjgl.vulkan.VkInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static naitsirc98.beryl.graphics.rendering.RenderingPaths.RPATH_SIMPLE3D;
import static naitsirc98.beryl.graphics.vulkan.VulkanDebugMessenger.newVulkanDebugMessenger;
import static naitsirc98.beryl.graphics.vulkan.VulkanInstanceFactory.newVkInstance;
import static naitsirc98.beryl.graphics.vulkan.VulkanValidationLayers.defaultValidationLayers;
import static naitsirc98.beryl.util.types.TypeUtils.newInstance;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.vkDestroyInstance;

@Destructor
public final class VulkanContext implements GraphicsContext {

    public static final boolean VULKAN_DEBUG_MESSAGES_ENABLED = BerylConfiguration.VULKAN_ENABLE_DEBUG_MESSAGES.get(Beryl.DEBUG);
    public static final Set<String> VALIDATION_LAYERS = BerylConfiguration.VULKAN_VALIDATION_LAYERS.get(defaultValidationLayers());
    public static final Set<String> DEVICE_EXTENSIONS = BerylConfiguration.VULKAN_DEVICE_EXTENSIONS.get(defaultDeviceExtensions());

    private static Set<String> defaultDeviceExtensions() {
        return Stream.of(VK_KHR_SWAPCHAIN_EXTENSION_NAME)
                .collect(toSet());
    }

    private VkInstance vkInstance;
    private VulkanDebugMessenger debugMessenger;
    private VulkanSurface surface;
    private VulkanPhysicalDevice physicalDevice;
    private VulkanLogicalDevice logicalDevice;
    private VulkanCommandPool graphicsCommandPool;
    private VulkanSwapchain swapchain;
    private VulkanRenderer renderer;

    private VulkanContext() {

    }

    @Override
    public void init() {
        vkInstance = newVkInstance();
        debugMessenger = newVulkanDebugMessenger();
        surface = new VulkanSurface();
        physicalDevice = new VulkanPhysicalDevice();
        logicalDevice = new VulkanLogicalDevice();
        graphicsCommandPool = createGraphicsCommandPool();
        swapchain = new VulkanSwapchain();
        renderer = newInstance(VulkanRenderer.class);
    }

    @Override
    public VulkanRenderer renderer() {
        return renderer;
    }

    @Override
    public Map<Integer, RenderingPath> renderingPaths() {

        Map<Integer, RenderingPath> renderingPaths = new HashMap<>();

        renderingPaths.put(RPATH_SIMPLE3D, newInstance(VulkanSimpleRenderingPath.class));

        return renderingPaths;
    }

    @Override
    public VertexData.Builder newVertexDataBuilder(VertexLayout layout) {
        return new VulkanVertexDataBuilder(layout);
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

    public VulkanCommandPool graphicsCommandPool() {
        return graphicsCommandPool;
    }

    public VulkanSwapchain swapchain() {
        return swapchain;
    }

    @Override
    public void free() {

        logicalDevice.waitIdle();

        renderer.free();

        graphicsCommandPool.free();

        swapchain.free();

        logicalDevice.free();

        physicalDevice.free();

        surface.free();

        if(VULKAN_DEBUG_MESSAGES_ENABLED) {
            debugMessenger.free();
        }

        vkDestroyInstance(vkInstance, null);

        vkInstance = null;
        renderer = null;
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
