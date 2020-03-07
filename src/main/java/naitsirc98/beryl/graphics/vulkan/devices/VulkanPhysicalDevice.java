package naitsirc98.beryl.graphics.vulkan.devices;

import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import naitsirc98.beryl.logging.Log;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;
import static naitsirc98.beryl.graphics.vulkan.VulkanContext.DEVICE_EXTENSIONS;
import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.BufferUtils.createIntBuffer;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.VK10.*;


public class VulkanPhysicalDevice implements VulkanObject.Custom<VkPhysicalDevice> {

    // TODO: pick best physical device, not the first matching the requirements

    private VkPhysicalDevice vkPhysicalDevice;

    public VulkanPhysicalDevice() {

        vkPhysicalDevice = findSuitablePhysicalDevice();

        Log.trace("[VULKAN]: Physical Device: " + properties().deviceNameString());
    }

    @Override
    public VkPhysicalDevice handle() {
        return vkPhysicalDevice;
    }

    public VkPhysicalDeviceProperties properties() {
        return getPhysicalDeviceProperties(vkPhysicalDevice);
    }

    public VkPhysicalDeviceFeatures features() {
        return getPhysicalDeviceFeatures(vkPhysicalDevice);
    }

    public QueueFamilyIndices queueFamilyIndices() {
        return findQueueFamilies(vkPhysicalDevice);
    }

    public SwapChainSupportDetails swapChainSupportDetails() {
        return new SwapChainSupportDetails(vkPhysicalDevice);
    }

    public int msaaSamples() {
        return getMaxMSAASmaples(vkPhysicalDevice);
    }

    @Override
    public void free() {
        vkPhysicalDevice = null;
    }

    private VkPhysicalDevice findSuitablePhysicalDevice() {

        try(MemoryStack stack = stackPush()) {

            IntBuffer deviceCount = stack.ints(0);

            vkEnumeratePhysicalDevices(vkInstance(), deviceCount, null);

            if(deviceCount.get(0) == 0) {
                Log.fatal("Failed to find GPUs with Vulkan support");
                return null;
            }

            PointerBuffer ppPhysicalDevices = stack.mallocPointer(deviceCount.get(0));

            vkEnumeratePhysicalDevices(vkInstance(), deviceCount, ppPhysicalDevices);

            VkPhysicalDevice physicalDevice = null;
            QueueFamilyIndices queueFamilyIndices = null;

            for(int i = 0;i < ppPhysicalDevices.capacity();i++) {

                physicalDevice = new VkPhysicalDevice(ppPhysicalDevices.get(i), vkInstance());

                queueFamilyIndices = findQueueFamilies(physicalDevice);

                if(isDeviceSuitable(physicalDevice, queueFamilyIndices)) {
                    break;
                }
            }

            if(physicalDevice == null) {
                throw new RuntimeException("Failed to find a suitable GPU");
            }

            return physicalDevice;
        }
    }

    private QueueFamilyIndices findQueueFamilies(VkPhysicalDevice physicalDevice) {

        QueueFamilyIndices indices = new QueueFamilyIndices();

        try(MemoryStack stack = stackPush()) {

            IntBuffer queueFamilyCount = stack.ints(0);

            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, queueFamilyCount, null);

            VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.mallocStack(queueFamilyCount.get(0), stack);

            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, queueFamilyCount, queueFamilies);

            IntBuffer presentSupport = stack.ints(VK_FALSE);

            for(int i = 0;i < queueFamilies.capacity() || !indices.isComplete();i++) {

                if((queueFamilies.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
                    indices.graphicsFamily = i;
                }

                vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, i, surface().handle(), presentSupport);

                if(presentSupport.get(0) == VK_TRUE) {
                    indices.presentationFamily = i;
                }
            }

            return indices;
        }
    }


    private boolean isDeviceSuitable(VkPhysicalDevice physicalDevice, QueueFamilyIndices queueFamilies) {

        boolean extensionsSupported = checkDeviceExtensionSupport(physicalDevice);
        boolean swapChainAdequate = false;
        boolean anisotropySupported = false;

        if(extensionsSupported) {
            try(MemoryStack stack = stackPush()) {

                SwapChainSupportDetails swapChainSupport = new SwapChainSupportDetails(physicalDevice, stack);
                swapChainAdequate = swapChainSupport.formats.hasRemaining() && swapChainSupport.presentModes.hasRemaining();

                VkPhysicalDeviceFeatures supportedFeatures = VkPhysicalDeviceFeatures.mallocStack(stack);
                vkGetPhysicalDeviceFeatures(physicalDevice, supportedFeatures);
                anisotropySupported = supportedFeatures.samplerAnisotropy();
            }
        }

        return queueFamilies.isComplete() && extensionsSupported && swapChainAdequate && anisotropySupported;
    }

    private boolean checkDeviceExtensionSupport(VkPhysicalDevice device) {

        try(MemoryStack stack = stackPush()) {

            IntBuffer extensionCount = stack.ints(0);

            vkEnumerateDeviceExtensionProperties(device, (String)null, extensionCount, null);

            VkExtensionProperties.Buffer availableExtensions = VkExtensionProperties.mallocStack(extensionCount.get(0), stack);

            return availableExtensions.stream().collect(toSet()).containsAll(DEVICE_EXTENSIONS);
        }
    }

    private int getMaxMSAASmaples(VkPhysicalDevice physicalDevice) {

        try(MemoryStack stack = stackPush()) {

            VkPhysicalDeviceProperties physicalDeviceProperties = VkPhysicalDeviceProperties.mallocStack(stack);
            vkGetPhysicalDeviceProperties(physicalDevice, physicalDeviceProperties);

            int sampleCountFlags = physicalDeviceProperties.limits().framebufferColorSampleCounts()
                    & physicalDeviceProperties.limits().framebufferDepthSampleCounts();

            if((sampleCountFlags & VK_SAMPLE_COUNT_64_BIT) != 0) {
                return VK_SAMPLE_COUNT_64_BIT;
            }
            if((sampleCountFlags & VK_SAMPLE_COUNT_32_BIT) != 0) {
                return VK_SAMPLE_COUNT_32_BIT;
            }
            if((sampleCountFlags & VK_SAMPLE_COUNT_16_BIT) != 0) {
                return VK_SAMPLE_COUNT_16_BIT;
            }
            if((sampleCountFlags & VK_SAMPLE_COUNT_8_BIT) != 0) {
                return VK_SAMPLE_COUNT_8_BIT;
            }
            if((sampleCountFlags & VK_SAMPLE_COUNT_4_BIT) != 0) {
                return VK_SAMPLE_COUNT_4_BIT;
            }
            if((sampleCountFlags & VK_SAMPLE_COUNT_2_BIT) != 0) {
                return VK_SAMPLE_COUNT_2_BIT;
            }

            return VK_SAMPLE_COUNT_1_BIT;
        }
    }

    private VkPhysicalDeviceFeatures getPhysicalDeviceFeatures(VkPhysicalDevice physicalDevice) {
        VkPhysicalDeviceFeatures features = VkPhysicalDeviceFeatures.create();
        vkGetPhysicalDeviceFeatures(physicalDevice, features);
        return features;
    }

    private VkPhysicalDeviceProperties getPhysicalDeviceProperties(VkPhysicalDevice physicalDevice) {
        VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.create();
        vkGetPhysicalDeviceProperties(physicalDevice, properties);
        return properties;
    }


    public final class SwapChainSupportDetails {

        private VkSurfaceCapabilitiesKHR capabilities;
        private VkSurfaceFormatKHR.Buffer formats;
        private IntBuffer presentModes;

        /**
         * Initializes a new SwapChainSupportDetails on the stack
         * */
        public SwapChainSupportDetails(VkPhysicalDevice vkPhysicalDevice, MemoryStack stack) {

            capabilities = VkSurfaceCapabilitiesKHR.mallocStack(stack);

            IntBuffer formatsCount = stack.mallocInt(1);
            vkCall(vkGetPhysicalDeviceSurfaceFormatsKHR(vkPhysicalDevice, surface().handle(), formatsCount, null));

            formats = VkSurfaceFormatKHR.mallocStack(formatsCount.get(0), stack);

            IntBuffer presentModesCount = stack.mallocInt(1);
            vkCall(vkGetPhysicalDeviceSurfacePresentModesKHR(vkPhysicalDevice, surface().handle(), presentModesCount, null));

            presentModes = stack.mallocInt(presentModesCount.get(0));

            init(vkPhysicalDevice, formatsCount, presentModesCount);
        }

        /**
         * Initializes a new SwapChainSupportDetails on the heap
         * */
        public SwapChainSupportDetails(VkPhysicalDevice vkPhysicalDevice) {

            capabilities = VkSurfaceCapabilitiesKHR.create();

            try(MemoryStack stack = stackPush()) {

                IntBuffer formatsCount = stack.mallocInt(1);
                vkCall(vkGetPhysicalDeviceSurfaceFormatsKHR(vkPhysicalDevice, surface().handle(), formatsCount, null));

                formats = VkSurfaceFormatKHR.create(formatsCount.get(0));

                IntBuffer presentModesCount = stack.mallocInt(1);
                vkCall(vkGetPhysicalDeviceSurfacePresentModesKHR(vkPhysicalDevice, surface().handle(), presentModesCount, null));

                presentModes = createIntBuffer(presentModesCount.get(0));

                init(vkPhysicalDevice, formatsCount, presentModesCount);
            }
        }

        private void init(VkPhysicalDevice vkPhysicalDevice, IntBuffer formatsCount, IntBuffer presentModesCount) {

            vkCall(vkGetPhysicalDeviceSurfaceCapabilitiesKHR(vkPhysicalDevice, surface().handle(), capabilities));

            vkCall(vkGetPhysicalDeviceSurfaceFormatsKHR(vkPhysicalDevice, surface().handle(), formatsCount, formats));

            vkCall(vkGetPhysicalDeviceSurfacePresentModesKHR(vkPhysicalDevice, surface().handle(), presentModesCount, presentModes));
        }

        public VkSurfaceCapabilitiesKHR capabilities() {
            return capabilities;
        }

        public VkSurfaceFormatKHR.Buffer formats() {
            return formats;
        }

        public IntBuffer presentModes() {
            return presentModes;
        }
    }

    public static class QueueFamilyIndices {

        private Integer graphicsFamily;
        private Integer presentationFamily;

        public int graphicsFamily() {
            return graphicsFamily;
        }

        public int presentationFamily() {
            return presentationFamily;
        }

        public boolean isComplete() {
            return graphicsFamily != null && presentationFamily != null;
        }

        public int[] unique() {
            return IntStream.of(graphicsFamily, presentationFamily).distinct().toArray();
        }

        public int[] array() {
            return new int[] {graphicsFamily, presentationFamily};
        }
    }
}
